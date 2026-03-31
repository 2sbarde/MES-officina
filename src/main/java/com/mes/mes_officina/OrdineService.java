package com.mes.mes_officina;

import org.springframework.stereotype.Service;

@Service
public class OrdineService {

    private final OrdineProduzioneRepository repo;

    public OrdineService(OrdineProduzioneRepository repo) {
        this.repo = repo;
    }

    public void startProduzione(Long id) {

        OrdineProduzione o = repo.findById(id).orElseThrow();

        // 🔥 REGOLA: puoi partire SOLO da SETUP
        if (o.stato != StatoOrdine.IN_SETUP) {
            throw new BusinessException("Devi fare SETUP prima di avviare");
        }

        o.stato = StatoOrdine.IN_PRODUZIONE;
        o.timestampInizio = System.currentTimeMillis();

        repo.save(o);
    }

    public void setup(Long id) {

        OrdineProduzione o = repo.findById(id).orElseThrow();

        if (o.macchina == null) return;

        boolean occupata = repo.findAll().stream()
                .anyMatch(x -> x.macchina != null &&
                        x.macchina.getId().equals(o.macchina.getId()) &&
                        (x.stato == StatoOrdine.IN_SETUP || x.stato == StatoOrdine.IN_PRODUZIONE));

        if (occupata) return;

        o.stato = StatoOrdine.IN_SETUP;
        repo.save(o);
    }
    public void versa(Long id, Integer pezzi) {

        if (pezzi == null || pezzi <= 0) return;

        OrdineProduzione o = repo.findById(id).orElseThrow();

        // 🔥 REGOLA: puoi versare SOLO in produzione
        if (o.stato != StatoOrdine.IN_PRODUZIONE) {
            throw new BusinessException("Puoi versare solo in PRODUZIONE");
        }

        o.pezziProdotti = pezzi;

        if (o.pezziProdotti >= o.quantita) {
            o.stato = StatoOrdine.COMPLETATO;
            o.dataChiusura = new java.util.Date();
        }

        repo.save(o);
    }
    public void chiudi(Long id) {

        OrdineProduzione o = repo.findById(id).orElseThrow();

        // 🔥 REGOLA: non puoi chiudere se non hai prodotto nulla
        if (o.pezziProdotti == 0) {
            throw new BusinessException("Non puoi chiudere senza produzione");
        }

        o.stato = StatoOrdine.COMPLETATO;
        o.dataChiusura = new java.util.Date();

        repo.save(o);
    }
}