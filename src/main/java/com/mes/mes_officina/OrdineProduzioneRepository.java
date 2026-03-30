package com.mes.mes_officina;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OrdineProduzioneRepository extends JpaRepository<OrdineProduzione, Long> {

    // 🔥 per storico
    List<OrdineProduzione> findByStato(String stato);

    // 🔥 per macchina (versione corretta con entity)
    List<OrdineProduzione> findByMacchina(Machine macchina);
}