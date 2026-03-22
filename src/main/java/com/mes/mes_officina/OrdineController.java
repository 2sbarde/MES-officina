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

    // =========================
    // DASHBOARD
    // =========================
    @GetMapping("/dashboard")
    public List<Map<String, Object>> dashboard() {

        List<Map<String, Object>> risultato = new ArrayList<>();

        for (String macchina : macchine) {

            Map<String, Object> mappa = new HashMap<>();
            mappa.put("macchina", macchina);

            List<OrdineProduzione> lista = ordini.stream()
                    .filter(o -> macchina.equals(o.macchina) && !"COMPLETATO".equals(o.stato))
                    .collect(Collectors.toList());

            OrdineProduzione attivo = lista.isEmpty() ? null : lista.get(0);

            mappa.put("attivo", attivo);
            mappa.put("coda", lista.stream().skip(1).toList());

            mappa.put("stato", attivo == null ? "FERMA" : attivo.stato);

            risultato.add(mappa);
        }

        return risultato;
    }

    // =========================
    // LISTA ORDINI
    // =========================
    @GetMapping
    public List<OrdineProduzione> lista() {
        return ordini;
    }

    // =========================
    // CREA ORDINE
    // =========================
    @PostMapping
    public OrdineProduzione crea(@RequestBody OrdineProduzione o) {
        o.id = counter++;
        o.stato = "CREATO";
        ordini.add(o);
        return o;
    }

    // =========================
    // SELEZIONA ORDINE (porta in cima)
    // =========================
    @PostMapping("/{id}/seleziona")
    public void seleziona(@PathVariable Long id) {

        OrdineProduzione selezionato = trova(id);

        // rimuove
        ordini.remove(selezionato);

        // trova posizione prima macchina uguale
        int index = 0;

        for (int i = 0; i < ordini.size(); i++) {
            if (ordini.get(i).macchina.equals(selezionato.macchina)) {
                index = i;
                break;
            }
        }

        // inserisce in cima alla macchina
        ordini.add(index, selezionato);

        selezionato.stato = "CREATO";
    }

    // =========================
    // SETUP
    // =========================
    @PostMapping("/{id}/setup")
    public void setup(@PathVariable Long id) {
        OrdineProduzione o = trova(id);
        o.stato = "IN_SETUP";
    }

    // =========================
    // START PRODUZIONE
    // =========================
    @PostMapping("/{id}/start")
    public void start(@PathVariable Long id) {
        OrdineProduzione o = trova(id);
        o.stato = "IN_PRODUZIONE";
    }

    // =========================
    // VERSA PEZZI
    // =========================
    @PostMapping("/{id}/versa")
    public void versa(@PathVariable Long id, @RequestParam int pezzi) {

        OrdineProduzione o = trova(id);

        o.pezziProdotti = pezzi;

        if (o.pezziProdotti >= o.quantita) {
            o.stato = "COMPLETATO";
        }
    }

    // =========================
    // CHIUDI
    // =========================
    @PostMapping("/{id}/chiudi")
    public void chiudi(@PathVariable Long id) {
        OrdineProduzione o = trova(id);
        o.stato = "COMPLETATO";
    }

    // =========================
    // ELIMINA (BLOCCO SICURO)
    // =========================
    @PostMapping("/{id}/elimina")
    public void elimina(@PathVariable Long id) {

        OrdineProduzione o = trova(id);

        if ("IN_PRODUZIONE".equals(o.stato)) {
            throw new RuntimeException("Ordine in produzione!");
        }

        ordini.remove(o);
    }

    // =========================
    // MODIFICA ORDINE
    // =========================
    @PostMapping("/{id}/modifica")
    public void modifica(@PathVariable Long id, @RequestBody OrdineProduzione nuovo) {

        OrdineProduzione o = trova(id);

        o.numeroCommessa = nuovo.numeroCommessa;
        o.codiceParticolare = nuovo.codiceParticolare;
        o.quantita = nuovo.quantita;
        o.tempoCicloSec = nuovo.tempoCicloSec;
        o.materiale = nuovo.materiale;
        o.diametroBarra = nuovo.diametroBarra;
        o.macchina = nuovo.macchina;
    }

    // =========================
    // STORICO
    // =========================
    @GetMapping("/storico")
    public List<OrdineProduzione> storico() {
        return ordini.stream()
                .filter(o -> "COMPLETATO".equals(o.stato))
                .toList();
    }

    // =========================
    // UTILITY
    // =========================
    private OrdineProduzione trova(Long id) {
        return ordini.stream()
                .filter(o -> o.id.equals(id))
                .findFirst()
                .orElseThrow();
    }
}