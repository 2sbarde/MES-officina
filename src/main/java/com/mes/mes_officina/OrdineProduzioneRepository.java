package com.mes.mes_officina;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrdineProduzioneRepository extends JpaRepository<OrdineProduzione, Long> {

    // 🔥 per storico
    List<OrdineProduzione> findByStato(String stato);

    // 🔥 per macchina
    List<OrdineProduzione> findByMacchina(Machine macchina);

    // 🔥 EXPORT MENSILE
    @Query("SELECT o FROM OrdineProduzione o WHERE MONTH(o.dataChiusura) = :mese AND YEAR(o.dataChiusura) = :anno")
    List<OrdineProduzione> findByMeseAnno(@Param("mese") int mese, @Param("anno") int anno);
}