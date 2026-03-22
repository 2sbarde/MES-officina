package com.mes.mes_officina;

import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/ordini")
@CrossOrigin
public class OrdineController {

    private List<OrdineProduzione> ordini = new ArrayList<>();
    private long counter = 1;

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

    // START PRODUZIONE
    @PostMapping("/{id}/start")
    public void start(@PathVariable Long id) {
        for (OrdineProduzione o : ordini) {
            if (o.id.equals(id)) {
                o.stato = "IN_PRODUZIONE";
            }
        }
    }

    // VERSA PEZZI
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
}