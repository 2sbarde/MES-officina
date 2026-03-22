package com.mes.mes_officina;

import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/ordini")
@CrossOrigin
public class OrdineController {

    private final OrdineProduzioneRepository repo;

    public OrdineController(OrdineProduzioneRepository repo) {
        this.repo = repo;
    }

    private List<String> macchine = Arrays.asList("T1", "T2", "T3");

    // DASHBOARD
    @GetMapping("/dashboard")
    public List<Map<String, Object>> dashboard() {

        List<Map<String, Object>> risultato = new ArrayList<>();

        for (String macchina : macchine) {

            List<OrdineProduzione> lista =
                    repo.findByMacchinaAndStatoNot(macchina, "COMPLETATO");

            Map<String, Object> mappa = new HashMap<>();

            mappa.put("macchina", macchina);

            OrdineProduzione attivo = lista.isEmpty() ? null : lista.get(0);

            mappa.put("attivo", attivo);
            mappa.put("coda", lista.size() > 1 ? lista.subList(1, lista.size()) : new ArrayList<>());
            mappa.put("stato", attivo == null ? "FERMA" : attivo.stato);

            risultato.add(mappa);
        }

        return risultato;
    }

    // LISTA
    @GetMapping
    public List<OrdineProduzione> lista() {
        return repo.findAll();
    }

    // CREA
    @PostMapping
    public OrdineProduzione crea(@RequestBody OrdineProduzione o) {
        o.stato = "CREATO";
        return repo.save(o);
    }

    // SETUP
    @PostMapping("/{id}/setup")
    public void setup(@PathVariable Long id) {
        OrdineProduzione o = repo.findById(id).orElseThrow();
        o.stato = "IN_SETUP";
        repo.save(o);
    }

    // START
    @PostMapping("/{id}/start")
    public void start(@PathVariable Long id) {
        OrdineProduzione o = repo.findById(id).orElseThrow();
        o.stato = "IN_PRODUZIONE";
        repo.save(o);
    }

    // VERSA
    @PostMapping("/{id}/versa")
    public void versa(@PathVariable Long id, @RequestParam int pezzi) {
        OrdineProduzione o = repo.findById(id).orElseThrow();

        o.pezziProdotti = pezzi;

        if (o.pezziProdotti >= o.quantita) {
            o.stato = "COMPLETATO";
        }

        repo.save(o);
    }

    // ELIMINA
    @PostMapping("/{id}/elimina")
    public void elimina(@PathVariable Long id) {
        repo.deleteById(id);
    }

    // STORICO
    @GetMapping("/storico")
    public List<OrdineProduzione> storico() {
        return repo.findByStato("COMPLETATO");
    }
}