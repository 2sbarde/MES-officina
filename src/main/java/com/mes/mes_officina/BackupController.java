package com.mes.mes_officina;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

// 👉 IMPORT CORRETTI
import com.mes.mes_officina.MachineRepository;
import com.mes.mes_officina.OrdineProduzioneRepository;

@RestController
public class BackupController {

    private final MachineRepository machineRepository;
    private final OrdineProduzioneRepository ordineProduzioneRepository;

    public BackupController(MachineRepository machineRepository,
                            OrdineProduzioneRepository ordineProduzioneRepository) {
        this.machineRepository = machineRepository;
        this.ordineProduzioneRepository = ordineProduzioneRepository;
    }

    @GetMapping("/backup")
    public ResponseEntity<Map<String, Object>> backup() {

        Map<String, Object> backup = new HashMap<>();

        // 👉 BACKUP COMPLETO MES
        backup.put("macchine", machineRepository.findAll());
        backup.put("ordiniProduzione", ordineProduzioneRepository.findAll());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=backup.json")
                .body(backup);
    }
}