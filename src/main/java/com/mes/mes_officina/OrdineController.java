package com.mes.mes_officina;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/ordini")
@CrossOrigin
public class OrdineController {

    private final OrdineRepository repo;

    public OrdineController(OrdineRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public List<OrdineProduzione> lista() {
        return repo.findAll();
    }

    @PostMapping
    public OrdineProduzione crea(@RequestBody OrdineProduzione o) {
        return repo.save(o);
    }

    @PostMapping("/{id}/elimina")
    public void elimina(@PathVariable Long id) {
        repo.deleteById(id);
    }

    @PostMapping("/{id}/versa")
    public void versa(@PathVariable Long id, @RequestParam int pezzi) {
        OrdineProduzione o = repo.findById(id).orElseThrow();
        o.pezziProdotti = pezzi;

        if (o.pezziProdotti >= o.quantita) {
            o.stato = "COMPLETATO";
        }

        repo.save(o);
    }

    @PostMapping("/{id}/chiudi")
    public void chiudi(@PathVariable Long id) {
        OrdineProduzione o = repo.findById(id).orElseThrow();
        o.stato = "COMPLETATO";
        repo.save(o);
    }

    // ⭐ STORICO
    @GetMapping("/storico")
    public List<OrdineProduzione> storico() {
        return repo.findAll().stream()
                .filter(o -> "COMPLETATO".equals(o.stato))
                .toList();
    }
}