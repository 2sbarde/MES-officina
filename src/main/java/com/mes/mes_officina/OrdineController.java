package com.mes.mes_officina;

import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/ordini")
@CrossOrigin
public class OrdineController {

    private final OrdineProduzioneRepository repo;
    private final MachineRepository machineRepo;

    public OrdineController(OrdineProduzioneRepository repo, MachineRepository machineRepo) {
        this.repo = repo;
        this.machineRepo = machineRepo;
    }

    private Comparator<OrdineProduzione> ordinamento() {
        return Comparator
                .comparing((OrdineProduzione o) -> o.priorita == null ? 999 : o.priorita)
                .thenComparing(o -> o.dataScadenza == null ? new Date(9999999999999L) : o.dataScadenza)
                .thenComparing(o -> o.id);
    }

    @GetMapping("/dashboard")
    public List<Map<String, Object>> dashboard() {

        List<Map<String, Object>> risultato = new ArrayList<>();
        List<Machine> macchine = machineRepo.findAll().stream()
                .sorted(Comparator.comparing(m -> m.nome))
                .toList();

        for (Machine macchina : macchine) {

            List<OrdineProduzione> lista = repo.findAll().stream()
                    .filter(o -> o.macchina != null &&
                            o.macchina.id.equals(macchina.id) &&
                            !"COMPLETATO".equals(o.stato))
                    .sorted(ordinamento())
                    .toList();

            Map<String, Object> mappa = new HashMap<>();

            mappa.put("macchina", macchina.nome);

            OrdineProduzione attivo = lista.isEmpty() ? null : lista.get(0);

            Map<String, Object> attivoMap = new HashMap<>();

            if (attivo != null) {
                attivoMap.put("id", attivo.id);
                attivoMap.put("numeroCommessa", attivo.numeroCommessa);
                attivoMap.put("cliente", attivo.cliente);
                attivoMap.put("codiceParticolare", attivo.codiceParticolare);
                attivoMap.put("pezziProdotti", attivo.pezziProdotti);
                attivoMap.put("quantita", attivo.quantita);
                attivoMap.put("stato", attivo.stato);
            }

            mappa.put("attivo", attivoMap);
            mappa.put("coda", lista.size() > 1 ? lista.subList(1, lista.size()) : new ArrayList<>());
            mappa.put("stato", attivo == null ? "FERMA" : attivo.stato);

            risultato.add(mappa);
        }

        return risultato;

    }

    @PostMapping("/{id}/setup")
    public void setup(@PathVariable Long id) {

        OrdineProduzione o = repo.findById(id).orElseThrow();

        if (o.macchina == null) return;

        boolean occupata = repo.findAll().stream()
                .anyMatch(x -> x.macchina != null &&
                        x.macchina.id.equals(o.macchina.id) &&
                        ("IN_SETUP".equals(x.stato) || "IN_PRODUZIONE".equals(x.stato)));

        if (occupata) return;

        o.stato = "IN_SETUP";
        repo.save(o);
    }

    @PostMapping("/{id}/start")
    public void start(@PathVariable Long id) {

        OrdineProduzione o = repo.findById(id).orElseThrow();

        o.stato = "IN_PRODUZIONE";
        o.timestampInizio = System.currentTimeMillis();

        repo.save(o);
    }

    @PostMapping("/{id}/versa")
    public void versa(@PathVariable Long id, @RequestParam Integer pezzi) {

        if (pezzi <= 0) return;

        OrdineProduzione o = repo.findById(id).orElseThrow();

        // 🔥 VALORE REALE (NON SOMMA)
        o.pezziProdotti = pezzi;

        // 🔥 AUTO-CHIUSURA
        if (o.pezziProdotti >= o.quantita) {
            o.stato = "COMPLETATO";
            o.dataChiusura = new Date();
        }

        repo.save(o);
    }

    @PostMapping("/{id}/chiudi")
    public void chiudi(@PathVariable Long id) {

        OrdineProduzione o = repo.findById(id).orElseThrow();

        o.stato = "COMPLETATO";
        o.dataChiusura = new Date();

        repo.save(o);
    }
    @PostMapping
    public OrdineProduzione crea(@RequestBody Map<String, Object> body) {

        OrdineProduzione o = new OrdineProduzione();

        o.numeroCommessa = (String) body.get("numeroCommessa");
        o.codiceParticolare = (String) body.get("codiceParticolare");
        o.cliente = (String) body.get("cliente");

        o.materiale = (String) body.get("materiale");
        o.diametroBarra = (String) body.get("diametroBarra");

        o.quantita = Integer.parseInt(body.get("quantita").toString());
        o.tempoCicloSec = Integer.parseInt(body.get("tempoCicloSec").toString());

        o.stato = "CREATO";

        // 🔥 DATA SCADENZA
        String data = (String) body.get("dataScadenza");
        if (data != null && !data.isEmpty()) {
            o.dataScadenza = java.sql.Date.valueOf(data);
        }

        // 🔥 PRIORITÀ
        Object pr = body.get("priorita");
        o.priorita = (pr == null) ? 0 : Integer.parseInt(pr.toString());

        // 🔥 MACCHINA
        Map macchinaMap = (Map) body.get("macchina");
        if (macchinaMap != null && macchinaMap.get("id") != null) {

            Long id = Long.valueOf(macchinaMap.get("id").toString());
            Machine m = machineRepo.findById(id).orElseThrow();

            o.macchina = m;
        }

        return repo.save(o);
    }
    @GetMapping("/storico")
    public List<OrdineProduzione> storico() {

        return repo.findAll().stream()
                .filter(o -> "COMPLETATO".equals(o.stato))
                .sorted((a, b) -> {
                    Date da = a.dataChiusura == null ? new Date(0) : a.dataChiusura;
                    Date db = b.dataChiusura == null ? new Date(0) : b.dataChiusura;
                    return db.compareTo(da); // più recenti prima
                })
                .toList();
    }
    @GetMapping
    public List<OrdineProduzione> lista() {
        return repo.findAll();
    }
    @PostMapping("/{id}/elimina")
    public void elimina(@PathVariable Long id) {

        OrdineProduzione o = repo.findById(id).orElseThrow();

        // opzionale: blocca eliminazione se completato
        if ("COMPLETATO".equals(o.stato)) {
            return;
        }


        repo.deleteById(id);
    }

}