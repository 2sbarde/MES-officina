package com.mes.mes_officina;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/machines")
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
}