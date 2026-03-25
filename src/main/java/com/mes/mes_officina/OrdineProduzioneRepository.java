package com.mes.mes_officina;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OrdineProduzioneRepository extends JpaRepository<OrdineProduzione, Long> {

    List<OrdineProduzione> findByMacchina_NomeAndStatoNot(String macchina, String stato);

    List<OrdineProduzione> findByStato(String stato);
}