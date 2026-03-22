package com.mes.mes_officina;

import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/ordini")
@CrossOrigin
public class OrdineController {

    private List<OrdineProduzione> ordini = new ArrayList<>();
    private long counter = 1;

    private List<String> macchine = Arrays.asList("T1", "T2", "T3");

    // DASHBOARD CON CODA
    @GetMapping("/dashboard")
    public List<Map<String, Object>> dashboard() {

        List<Map<String, Object>> risultato = new ArrayList<>();

        for (String macchina : macchine) {

            Map<String, Object> mappa = new HashMap<>();
            mappa.put("macchina", macchina);

            List<OrdineProduzione> lista = ordini.stream()
                    .filter(o -> macchina.equals(o.macchina) && !"COMPLETATO".equals(o.stato))
                    .collect(Collectors.toList());

            // primo = attivo
            OrdineProduzione attivo = null;
            if (!lista.isEmpty()) {
                attivo = lista.get(0);
            }

            mappa.put("attivo", attivo);
            mappa.put("coda", lista.stream().skip(1).toList());

            if (attivo == null) {
                mappa.put("stato", "FERMA");
            } else {
                mappa.put("stato", attivo.stato);
            }

            risultato.add(mappa);
        }

        return risultato;
    }

    // CREA ORDINE
    @PostMapping
    public OrdineProduzione crea(@RequestBody OrdineProduzione o) {

        o.id = counter++;

        // se macchina libera → attivo
        boolean occupata = ordini.stream().anyMatch(x ->
                o.macchina.equals(x.macchina) &&
                        ("IN_SETUP".equals(x.stato) || "IN_PRODUZIONE".equals(x.stato))
        );

        if (occupata) {
            o.stato = "IN_CODA";
        } else {
            o.stato = "CREATO";
        }

        ordini.add(o);
        return o;
    }

    // SETUP
    @PostMapping("/{id}/setup")
    public void setup(@PathVariable Long id) {

        OrdineProduzione o = trova(id);

        if (!"CREATO".equals(o.stato)) {
            throw new RuntimeException("Non pronto per setup");
        }

        o.stato = "IN_SETUP";
    }

    // START
    @PostMapping("/{id}/start")
    public void start(@PathVariable Long id) {

        OrdineProduzione o = trova(id);

        if (!"IN_SETUP".equals(o.stato)) {
            throw new RuntimeException("Prima setup!");
        }

        o.stato = "IN_PRODUZIONE";
    }

    // VERSA
    @PostMapping("/{id}/versa")
    public void versa(@PathVariable Long id, @RequestParam int pezzi) {

        OrdineProduzione o = trova(id);

        if (!"IN_PRODUZIONE".equals(o.stato)) {
            throw new RuntimeException("Non in produzione");
        }

        o.pezziProdotti = pezzi;

        if (o.pezziProdotti >= o.quantita) {
            o.stato = "COMPLETATO";

            // 🔥 AVVIO AUTOMATICO PROSSIMO
            avviaProssimo(o.macchina);
        }
    }

    // CHIUDI
    @PostMapping("/{id}/chiudi")
    public void chiudi(@PathVariable Long id) {

        OrdineProduzione o = trova(id);
        o.stato = "COMPLETATO";

        avviaProssimo(o.macchina);
    }

    // STORICO
    @GetMapping("/storico")
    public List<OrdineProduzione> storico() {
        return ordini.stream()
                .filter(o -> "COMPLETATO".equals(o.stato))
                .toList();
    }

    // 🔥 LOGICA CODA
    private void avviaProssimo(String macchina) {

        Optional<OrdineProduzione> prossimo = ordini.stream()
                .filter(o -> macchina.equals(o.macchina) && "IN_CODA".equals(o.stato))
                .findFirst();

        prossimo.ifPresent(o -> o.stato = "CREATO");
    }

    private OrdineProduzione trova(Long id) {
        return ordini.stream()
                .filter(o -> o.id.equals(id))
                .findFirst()
                .orElseThrow();
    }
}