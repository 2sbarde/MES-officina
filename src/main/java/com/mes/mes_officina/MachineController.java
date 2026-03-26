package com.mes.mes_officina;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/macchine")
@CrossOrigin
public class MachineController {

    private final MachineRepository repo;

    public MachineController(MachineRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public List<Machine> getAll() {
        return repo.findAll();
    }

    @PostMapping
    public Machine create(@RequestBody Machine m) {
        m.stato = "FERMA";
        return repo.save(m);
    }

    // 🔥 NUOVO ENDPOINT VELOCE
    @GetMapping("/init")
    public List<Machine> init() {

        if (repo.count() == 0) {
            repo.save(crea("T1"));
            repo.save(crea("T2"));
            repo.save(crea("T3"));
        }

        return repo.findAll();
    }

    private Machine crea(String nome) {
        Machine m = new Machine();
        m.nome = nome;
        m.stato = "FERMA";
        return m;
    }
}