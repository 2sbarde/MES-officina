package com.mes.mes_officina;

import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@RestController
@RequestMapping("/ordini")
@CrossOrigin
public class OrdineController {

    private Map<String, Macchina> macchine = new HashMap<>();
    private List<OrdineProduzione> ordini = new ArrayList<>();
    private AtomicLong counter = new AtomicLong();

    public OrdineController() {
        macchine.put("T1", new Macchina("T1"));
        macchine.put("T2", new Macchina("T2"));
        macchine.put("T3", new Macchina("T3"));
    }

    @GetMapping
    public List<OrdineProduzione> lista() {
        return ordini;
    }

    @PostMapping
    public OrdineProduzione crea(@RequestBody OrdineProduzione o) {
        o.id = counter.incrementAndGet();
        ordini.add(o);
        return o;
    }

    // ELIMINA ORDINE
    @PostMapping("/{id}/elimina")
    public void elimina(@PathVariable Long id) {
        ordini.removeIf(o -> o.id.equals(id));
    }

    // SETUP
    @PostMapping("/{id}/setup")
    public void setup(@PathVariable Long id, @RequestParam String macchina) {

        Macchina m = macchine.get(macchina);

        if (!m.stato.equals("LIBERA")) return;

        for (OrdineProduzione o : ordini) {
            if (o.id.equals(id)) {
                o.stato = "IN_SETUP";
                m.stato = "SETUP";
                m.ordineId = id;
            }
        }
    }

    // START
    @PostMapping("/{id}/start")
    public void start(@PathVariable Long id, @RequestParam String macchina) {

        Macchina m = macchine.get(macchina);

        if (!id.equals(m.ordineId)) return;

        m.stato = "PRODUZIONE";

        for (OrdineProduzione o : ordini) {
            if (o.id.equals(id)) {
                o.stato = "IN_PRODUZIONE";
            }
        }
    }

    // VERSA (solo se PRODUZIONE)
    @PostMapping("/{id}/versa")
    public void versa(@PathVariable Long id, @RequestParam int pezzi) {

        for (OrdineProduzione o : ordini) {
            if (o.id.equals(id)) {

                for (Macchina m : macchine.values()) {
                    if (id.equals(m.ordineId) && !m.stato.equals("PRODUZIONE")) {
                        return; // blocca versamento
                    }
                }

                o.pezziProdotti = pezzi;

                if (o.pezziProdotti >= o.quantita) {
                    chiudiOrdine(o);
                }
            }
        }
    }

    // CHIUSURA
    @PostMapping("/{id}/chiudi")
    public void chiudi(@PathVariable Long id) {
        for (OrdineProduzione o : ordini) {
            if (o.id.equals(id)) {
                chiudiOrdine(o);
            }
        }
    }

    private void chiudiOrdine(OrdineProduzione o) {

        o.stato = "COMPLETATO";

        macchine.values().forEach(m -> {
            if (o.id.equals(m.ordineId)) {
                m.stato = "FERMA";
                m.ordineId = null;
            }
        });
    }
}