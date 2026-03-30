package com.mes.mes_officina;

import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
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
        return repo.findAll().stream()
                .sorted(Comparator.comparing(Machine::getNome))
                .toList();
    }

    @PostMapping
    public Machine create(@RequestBody Machine m) {
        return repo.save(m);
    }

    // 🔥 INIT MACCHINE
    @GetMapping("/init")
    public List<Machine> init() {

        if (repo.count() == 0) {
            repo.save(crea("T1"));
            repo.save(crea("T2"));
            repo.save(crea("T3"));
        }

        return getAll();
    }

    private Machine crea(String nome) {
        Machine m = new Machine();
        m.setNome(nome);
        return m;
    }
}