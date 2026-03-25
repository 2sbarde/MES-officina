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

    @GetMapping("/")
    public String root() {
        return "MES OK";
    }

    @GetMapping("/dashboard")
    public List<Map<String, Object>> dashboard() {

        List<Map<String, Object>> risultato = new ArrayList<>();

        List<Machine> macchine = machineRepo.findAll();

        for (Machine macchina : macchine) {

            List<OrdineProduzione> lista = repo.findAll().stream()
                    .filter(o -> o.macchina != null &&
                            o.macchina.id.equals(macchina.id) &&
                            !"COMPLETATO".equals(o.stato))
                    .toList();

            Map<String, Object> mappa = new HashMap<>();

            mappa.put("macchina", macchina.nome);

            OrdineProduzione attivo = lista.isEmpty() ? null : lista.get(0);

            mappa.put("attivo", attivo);
            mappa.put("coda", lista.size() > 1 ? lista.subList(1, lista.size()) : new ArrayList<>());
            mappa.put("stato", attivo == null ? "FERMA" : attivo.stato);

            risultato.add(mappa);
        }

        return risultato;
    }

    @GetMapping
    public List<OrdineProduzione> lista() {
        return repo.findAll();
    }

    // 🔥 CREA ORDINE (FIX COMPLETO)
    @PostMapping
    public OrdineProduzione crea(@RequestBody Map<String, Object> body) {

        OrdineProduzione o = new OrdineProduzione();

        o.numeroCommessa = (String) body.get("numeroCommessa");
        o.codiceParticolare = (String) body.get("codiceParticolare");
        o.cliente = (String) body.get("cliente");

        o.materiale = (String) body.get("materiale");
        o.diametroBarra = (String) body.get("diametroBarra");

        if (o.materiale == null || o.materiale.isEmpty()) o.materiale = "ND";
        if (o.diametroBarra == null || o.diametroBarra.isEmpty()) o.diametroBarra = "0";

        o.quantita = Integer.parseInt(body.get("quantita").toString());
        o.tempoCicloSec = Integer.parseInt(body.get("tempoCicloSec").toString());

        o.stato = "CREATO";

        // 🔥 MACHINE
        Map macchinaMap = (Map) body.get("macchina");

        if (macchinaMap != null && macchinaMap.get("id") != null) {

            Long id = Long.valueOf(macchinaMap.get("id").toString());

            Machine m = machineRepo.findById(id).orElseThrow();

            // 🔥 BLOCCO ORDINI ATTIVI
            boolean esiste = repo.findAll().stream()
                    .anyMatch(ord ->
                            ord.macchina != null &&
                                    ord.macchina.id.equals(id) &&
                                    ("IN_SETUP".equals(ord.stato) || "IN_PRODUZIONE".equals(ord.stato))
                    );

            if (esiste) {
                throw new RuntimeException("Macchina già occupata da un ordine");
            }

            o.macchina = m;
        }

        return repo.save(o);
    }

    @PostMapping("/{id}/setup")
    public void setup(@PathVariable Long id) {
        OrdineProduzione o = repo.findById(id).orElseThrow();

        o.stato = "IN_SETUP";

        if (o.macchina != null) {
            Machine m = o.macchina;
            m.stato = "IN_SETUP";
            machineRepo.save(m);
        }

        repo.save(o);
    }

    @PostMapping("/{id}/start")
    public void start(@PathVariable Long id) {
        OrdineProduzione o = repo.findById(id).orElseThrow();

        o.stato = "IN_PRODUZIONE";

        if (o.macchina != null) {
            Machine m = o.macchina;
            m.stato = "IN_PRODUZIONE";
            machineRepo.save(m);
        }

        repo.save(o);
    }

    @PostMapping("/{id}/versa")
    public void versa(@PathVariable Long id, @RequestParam int pezzi) {
        OrdineProduzione o = repo.findById(id).orElseThrow();

        o.pezziProdotti = pezzi;

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

        if (o.macchina != null) {
            Machine m = o.macchina;
            m.stato = "FERMA";
            machineRepo.save(m);
        }

        repo.save(o);
    }

    @PostMapping("/{id}/elimina")
    public void elimina(@PathVariable Long id) {

        OrdineProduzione o = repo.findById(id).orElseThrow();

        if ("COMPLETATO".equals(o.stato)) {
            throw new RuntimeException("Non puoi eliminare un ordine completato");
        }

        repo.deleteById(id);
    }

    @GetMapping("/storico")
    public List<OrdineProduzione> storico(
            @RequestParam(required = false) String cliente,
            @RequestParam(required = false) String codice,
            @RequestParam(required = false) String data
    ) {

        List<OrdineProduzione> lista = repo.findByStato("COMPLETATO");

        return lista.stream()
                .filter(o -> cliente == null || o.cliente != null && o.cliente.toLowerCase().contains(cliente.toLowerCase()))
                .filter(o -> codice == null || o.codiceParticolare != null && o.codiceParticolare.toLowerCase().contains(codice.toLowerCase()))
                .filter(o -> {
                    if (data == null || o.dataChiusura == null) return true;
                    return o.dataChiusura.toString().contains(data);
                })
                .toList();
    }

    @GetMapping("/pianificazione")
    public List<Map<String, Object>> pianificazione() {

        List<Map<String, Object>> risultato = new ArrayList<>();

        long now = System.currentTimeMillis();

        List<Machine> macchine = machineRepo.findAll();

        for (Machine macchina : macchine) {

            List<OrdineProduzione> lista =
                    repo.findByMacchina_NomeAndStatoNot(macchina.nome, "COMPLETATO");

            long tempoCumulato = 0;

            List<Map<String, Object>> dettagli = new ArrayList<>();

            for (OrdineProduzione o : lista) {

                long tempo = (long) o.tempoCicloSec * o.quantita * 1000;

                tempoCumulato += tempo;

                long fine = now + tempoCumulato;

                Date fineDate = new Date(fine);

                Map<String, Object> det = new HashMap<>();
                det.put("commessa", o.numeroCommessa);
                det.put("fine", fineDate.toString());

                dettagli.add(det);
            }

            Map<String, Object> mappa = new HashMap<>();
            mappa.put("macchina", macchina.nome);
            mappa.put("ordini", dettagli);

            risultato.add(mappa);
        }

        return risultato;
    }
    @PostMapping("/clear-storico")
    public void clearStorico() {
        List<OrdineProduzione> completati = repo.findByStato("COMPLETATO");
        repo.deleteAll(completati);
    }
}