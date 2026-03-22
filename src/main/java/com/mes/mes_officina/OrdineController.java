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

    // DASHBOARD
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

    // LISTA ORDINI
    @GetMapping
    public List<OrdineProduzione> lista() {
        return ordini;
    }

    // CREA
    @PostMapping
    public OrdineProduzione crea(@RequestBody OrdineProduzione o) {
        o.id = counter++;
        o.stato = "CREATO";
        ordini.add(o);
        return o;
    }

    // 🔥 SELEZIONA ORDINE (NUOVO)
    @PostMapping("/{id}/seleziona")
    public void seleziona(@PathVariable Long id) {

        OrdineProduzione selezionato = trova(id);

        // porta questo ordine in cima alla lista della macchina
        ordini.remove(selezionato);

        List<OrdineProduzione> stessaMacchina = ordini.stream()
                .filter(o -> selezionato.macchina.equals(o.macchina))
                .collect(Collectors.toList());

        int index = 0;
        for (OrdineProduzione o : stessaMacchina) {
            index = ordini.indexOf(o);
            break;
        }

        ordini.add(index, selezionato);

        selezionato.stato = "CREATO";
    }

    // SETUP
    @PostMapping("/{id}/setup")
    public void setup(@PathVariable Long id) {
        OrdineProduzione o = trova(id);
        o.stato = "IN_SETUP";
    }

    // START
    @PostMapping("/{id}/start")
    public void start(@PathVariable Long id) {
        OrdineProduzione o = trova(id);
        o.stato = "IN_PRODUZIONE";
    }

    // VERSA
    @PostMapping("/{id}/versa")
    public void versa(@PathVariable Long id, @RequestParam int pezzi) {
        OrdineProduzione o = trova(id);
        o.pezziProdotti = pezzi;

        if (o.pezziProdotti >= o.quantita) {
            o.stato = "COMPLETATO";
        }
    }

    // STORICO
    @GetMapping("/storico")
    public List<OrdineProduzione> storico() {
        return ordini.stream()
                .filter(o -> "COMPLETATO".equals(o.stato))
                .toList();
    }

    private OrdineProduzione trova(Long id) {
        return ordini.stream()
                .filter(o -> o.id.equals(id))
                .findFirst()
                .orElseThrow();
    }
    @PostMapping("/{id}/elimina")
    public void elimina(@PathVariable Long id) {
        ordini.removeIf(o -> o.id.equals(id));
    }
}