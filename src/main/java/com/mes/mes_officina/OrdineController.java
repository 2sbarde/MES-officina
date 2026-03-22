package com.mes.mes_officina;

import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/ordini")
@CrossOrigin
public class OrdineController {

    private List<OrdineProduzione> ordini = new ArrayList<>();
    private long counter = 1;

    private List<String> macchine = Arrays.asList("T1", "T2", "T3");

    // DASHBOARD MACCHINE
    @GetMapping("/dashboard")
    public List<Map<String, Object>> dashboard() {

        List<Map<String, Object>> risultato = new ArrayList<>();

        for (String nomeMacchina : macchine) {

            Map<String, Object> mappa = new HashMap<>();
            mappa.put("macchina", nomeMacchina);

            Optional<OrdineProduzione> ordine = ordini.stream()
                    .filter(o -> nomeMacchina.equals(o.macchina) &&
                            ("IN_SETUP".equals(o.stato) || "IN_PRODUZIONE".equals(o.stato)))
                    .findFirst();

            if (ordine.isPresent()) {
                mappa.put("stato", ordine.get().stato);
                mappa.put("ordine", ordine.get());
            } else {
                mappa.put("stato", "FERMA");
                mappa.put("ordine", null);
            }

            risultato.add(mappa);
        }

        return risultato;
    }

    // LISTA ORDINI
    @GetMapping
    public List<OrdineProduzione> lista() {
        return ordini;
    }

    // CREA ORDINE
    @PostMapping
    public OrdineProduzione crea(@RequestBody OrdineProduzione o) {
        o.id = counter++;
        o.stato = "CREATO";
        ordini.add(o);
        return o;
    }

    // SETUP
    @PostMapping("/{id}/setup")
    public void setup(@PathVariable Long id) {
        for (OrdineProduzione o : ordini) {
            if (o.id.equals(id)) {
                o.stato = "IN_SETUP";
            }
        }
    }

    // START
    @PostMapping("/{id}/start")
    public void start(@PathVariable Long id) {
        for (OrdineProduzione o : ordini) {
            if (o.id.equals(id)) {
                o.stato = "IN_PRODUZIONE";
            }
        }
    }

    // VERSA
    @PostMapping("/{id}/versa")
    public void versa(@PathVariable Long id, @RequestParam int pezzi) {
        for (OrdineProduzione o : ordini) {
            if (o.id.equals(id)) {
                o.pezziProdotti = pezzi;

                if (o.pezziProdotti >= o.quantita) {
                    o.stato = "COMPLETATO";
                }
            }
        }
    }

    // CHIUDI
    @PostMapping("/{id}/chiudi")
    public void chiudi(@PathVariable Long id) {
        for (OrdineProduzione o : ordini) {
            if (o.id.equals(id)) {
                o.stato = "COMPLETATO";
            }
        }
    }

    // STORICO
    @GetMapping("/storico")
    public List<OrdineProduzione> storico() {
        List<OrdineProduzione> completati = new ArrayList<>();

        for (OrdineProduzione o : ordini) {
            if ("COMPLETATO".equals(o.stato)) {
                completati.add(o);
            }
        }

        return completati;
    }
}