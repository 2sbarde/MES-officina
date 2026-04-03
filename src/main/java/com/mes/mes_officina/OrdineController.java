package com.mes.mes_officina;

import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/ordini")
@CrossOrigin
public class OrdineController {

    private final OrdineProduzioneRepository repo;
    private final MachineRepository machineRepo;
    private final OrdineService ordineService;

    public OrdineController(OrdineProduzioneRepository repo,
                            MachineRepository machineRepo,
                            OrdineService ordineService) {
        this.repo = repo;
        this.machineRepo = machineRepo;
        this.ordineService = ordineService;
    }

    private Comparator<OrdineProduzione> ordinamento() {
        return Comparator
                .comparing((OrdineProduzione o) ->
                        o.dataScadenza == null ? new Date(9999999999999L) : o.dataScadenza
                )
                .thenComparing(o -> o.id);
    }

    @GetMapping("/dashboard")
    public List<Map<String, Object>> dashboard() {

        List<Map<String, Object>> risultato = new ArrayList<>();

        List<Machine> macchine = machineRepo.findAll().stream()
                .sorted(Comparator.comparing(Machine::getNome))
                .toList();

        for (Machine macchina : macchine) {

            List<OrdineProduzione> lista = repo.findAll().stream()
                    .filter(o -> o.macchina != null &&
                            o.macchina.getId().equals(macchina.getId()) &&
                            o.stato != StatoOrdine.COMPLETATO &&
                            o.stato != StatoOrdine.SOSPESO
                    )
                    .sorted(Comparator.comparing(
                            (OrdineProduzione o) ->
                                    o.dataScadenza == null ? new Date(9999999999999L) : o.dataScadenza
                    ))
                    .toList();

            Map<String, Object> mappa = new HashMap<>();

            mappa.put("macchina", macchina.getNome());

            // 🔥 TROVA ORDINE IN PRODUZIONE
            OrdineProduzione attivo = lista.stream()
                    .filter(o -> o.stato == StatoOrdine.IN_PRODUZIONE)
                    .findFirst()
                    .orElse(null);

            // 🔥 SE NON C'È PRODUZIONE, CERCA SETUP
            if (attivo == null) {
                attivo = lista.stream()
                        .filter(o -> o.stato == StatoOrdine.IN_SETUP)
                        .findFirst()
                        .orElse(null);
            }

            Map<String, Object> attivoMap = new HashMap<>();

            if (attivo != null) {
                attivoMap.put("id", attivo.id);
                attivoMap.put("numeroCommessa", attivo.numeroCommessa);
                attivoMap.put("cliente", attivo.cliente);
                attivoMap.put("codiceParticolare", attivo.codiceParticolare);
                attivoMap.put("pezziProdotti", attivo.pezziProdotti);
                attivoMap.put("quantita", attivo.quantita);
                attivoMap.put("stato", attivo.stato);
                attivoMap.put("tempoCicloSec", attivo.tempoCicloSec);
            }

            mappa.put("attivo", attivoMap);

            // 🔥 CODA = SOLO CREATO
            List<OrdineProduzione> coda = lista.stream()
                    .filter(o -> o.stato == StatoOrdine.CREATO)
                    .toList();

            mappa.put("coda", coda);

            mappa.put("stato", attivo == null ? "FERMA" : attivo.stato.name());

            risultato.add(mappa);
        }

        return risultato;
    }

    @PostMapping("/{id}/setup")
    public void setup(@PathVariable Long id) {
        ordineService.setup(id);
    }

    @PostMapping("/{id}/start")
    public void start(@PathVariable Long id) {
        ordineService.startProduzione(id);
    }

    @PostMapping("/{id}/versa")
    public void versa(@PathVariable Long id, @RequestParam Integer pezzi) {
        ordineService.versa(id, pezzi);
    }

    @PostMapping("/{id}/chiudi")
    public void chiudi(@PathVariable Long id) {
        ordineService.chiudi(id);
    }

    @PostMapping("/{id}/tempo")
    public void aggiornaTempo(@PathVariable Long id, @RequestParam Integer tempo) {

        OrdineProduzione o = repo.findById(id).orElseThrow();

        o.tempoCicloSec = tempo;

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

        o.stato = StatoOrdine.CREATO;

        String data = (String) body.get("dataScadenza");
        if (data != null && !data.isEmpty()) {
            o.dataScadenza = java.sql.Date.valueOf(data);
        }

        Object pr = body.get("priorita");
        o.priorita = (pr == null) ? 0 : Integer.parseInt(pr.toString());

        Map macchinaMap = (Map) body.get("macchina");
        if (macchinaMap != null && macchinaMap.get("id") != null) {

            Long idMacchina = Long.valueOf(macchinaMap.get("id").toString());
            Machine m = machineRepo.findById(idMacchina).orElseThrow();

            o.macchina = m;
        }

        return repo.save(o);
    }

    @GetMapping("/storico")
    public List<OrdineProduzione> storico() {

        return repo.findAll().stream()
                .filter(o -> o.stato == StatoOrdine.COMPLETATO)
                .sorted((a, b) -> {
                    Date da = a.dataChiusura == null ? new Date(0) : a.dataChiusura;
                    Date db = b.dataChiusura == null ? new Date(0) : b.dataChiusura;
                    return db.compareTo(da);
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

        if (o.stato == StatoOrdine.COMPLETATO) return;

        repo.deleteById(id);
    }
    @PostMapping("/{id}/macchina")
    public void cambiaMacchina(@PathVariable Long id, @RequestParam Long macchinaId){

        OrdineProduzione o = repo.findById(id).orElseThrow();

        // sicurezza
        if(o.stato != StatoOrdine.CREATO) return;

        Machine m = machineRepo.findById(macchinaId).orElseThrow();

        o.macchina = m;

        repo.save(o);
    }
    @PostMapping("/{id}/sospendi")
    public void sospendi(@PathVariable Long id){
        OrdineProduzione o = repo.findById(id).orElseThrow();
        o.stato = StatoOrdine.SOSPESO;
        repo.save(o);
    }
    @PostMapping("/{id}/riprendi")
    public void riprendi(@PathVariable Long id){
        OrdineProduzione o = repo.findById(id).orElseThrow();

        // puoi decidere dove torna
        o.stato = StatoOrdine.CREATO;

        repo.save(o);
    }
}