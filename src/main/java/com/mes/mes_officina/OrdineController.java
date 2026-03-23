package com.mes.mes_officina;

import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/ordini")
@CrossOrigin
public class OrdineController {

    private final OrdineProduzioneRepository repo;

    public OrdineController(OrdineProduzioneRepository repo) {
        this.repo = repo;
    }

    private List<String> macchine = Arrays.asList("T1", "T2", "T3");

    // ================= DASHBOARD =================
    @GetMapping("/dashboard")
    public List<Map<String, Object>> dashboard() {

        List<Map<String, Object>> risultato = new ArrayList<>();

        for (String macchina : macchine) {

            List<OrdineProduzione> lista =
                    repo.findByMacchinaAndStatoNot(macchina, "COMPLETATO");

            Map<String, Object> mappa = new HashMap<>();

            OrdineProduzione attivo = lista.isEmpty() ? null : lista.get(0);

            mappa.put("macchina", macchina);
            mappa.put("attivo", attivo);
            mappa.put("stato", attivo == null ? "FERMA" : attivo.stato);

            if (attivo != null) {

                int prodotti = attivo.pezziProdotti;
                int totale = attivo.quantita;

                int percentuale = totale == 0 ? 0 : (prodotti * 100) / totale;

                int rimanenti = totale - prodotti;

                long tempoResiduo = (long) rimanenti * attivo.tempoCicloSec;

                mappa.put("percentuale", percentuale);
                mappa.put("tempoResiduo", tempoResiduo);
            }

            risultato.add(mappa);
        }

        return risultato;
    }

    // ================= STORICO =================
    @GetMapping("/storico")
    public List<OrdineProduzione> storico() {
        return repo.findByStato("COMPLETATO");
    }

    // ================= KPI =================
    @GetMapping("/kpi")
    public List<Map<String, Object>> kpi() {

        List<Map<String, Object>> risultato = new ArrayList<>();

        for (String macchina : macchine) {

            List<OrdineProduzione> lista =
                    repo.findByMacchinaAndStatoNot(macchina, "CREATO");

            int totalePezzi = 0;
            int totaleOrdini = 0;
            long tempoTotale = 0;

            for (OrdineProduzione o : lista) {

                totalePezzi += o.pezziProdotti;
                totaleOrdini++;

                tempoTotale += (long) o.tempoCicloSec * o.pezziProdotti;
            }

            int media = totaleOrdini == 0 ? 0 : totalePezzi / totaleOrdini;

            Map<String, Object> mappa = new HashMap<>();
            mappa.put("macchina", macchina);
            mappa.put("pezzi", totalePezzi);
            mappa.put("ordini", totaleOrdini);
            mappa.put("tempo", tempoTotale);
            mappa.put("media", media);

            risultato.add(mappa);
        }

        return risultato;
    }

    // ================= PIANIFICAZIONE =================
    @GetMapping("/pianificazione")
    public List<Map<String, Object>> pianificazione() {

        List<Map<String, Object>> risultato = new ArrayList<>();

        for (String macchina : macchine) {

            List<OrdineProduzione> lista =
                    repo.findByMacchinaAndStatoNot(macchina, "COMPLETATO");

            List<Map<String, Object>> dettagli = new ArrayList<>();

            for (OrdineProduzione o : lista) {

                long durata = (long) o.tempoCicloSec * o.quantita;

                Map<String, Object> det = new HashMap<>();
                det.put("commessa", o.numeroCommessa);
                det.put("cliente", o.cliente);
                det.put("durata", durata);

                dettagli.add(det);
            }

            Map<String, Object> mappa = new HashMap<>();
            mappa.put("macchina", macchina);
            mappa.put("ordini", dettagli);

            risultato.add(mappa);
        }

        return risultato;
    }

    // ================= CREA =================
    @PostMapping
    public OrdineProduzione crea(@RequestBody OrdineProduzione o) {
        o.stato = "CREATO";
        return repo.save(o);
    }

    // ================= SETUP =================
    @PostMapping("/{id}/setup")
    public void setup(@PathVariable Long id) {
        OrdineProduzione o = repo.findById(id).orElseThrow();
        o.stato = "IN_SETUP";
        repo.save(o);
    }

    // ================= START =================
    @PostMapping("/{id}/start")
    public void start(@PathVariable Long id) {
        OrdineProduzione o = repo.findById(id).orElseThrow();
        o.stato = "IN_PRODUZIONE";
        repo.save(o);
    }

    // ================= VERSA =================
    @PostMapping("/{id}/versa")
    public void versa(@PathVariable Long id, @RequestParam int pezzi) {

        OrdineProduzione o = repo.findById(id).orElseThrow();

        o.pezziProdotti = pezzi;

        if (o.pezziProdotti >= o.quantita) {
            o.stato = "COMPLETATO";
            o.dataChiusura = new Date();
        }

        repo.save(o);
    }

    // ================= CHIUDI =================
    @PostMapping("/{id}/chiudi")
    public void chiudi(@PathVariable Long id) {
        OrdineProduzione o = repo.findById(id).orElseThrow();
        o.stato = "COMPLETATO";
        o.dataChiusura = new Date();
        repo.save(o);
    }
}