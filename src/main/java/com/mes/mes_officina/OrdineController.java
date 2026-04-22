package com.mes.mes_officina;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.*;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

@RestController
@RequestMapping("/ordini")
@CrossOrigin
public class OrdineController {

    private final OrdineProduzioneRepository repo;
    private final MachineRepository machineRepo;
    private final OrdineService ordineService;

    public OrdineController(OrdineProduzioneRepository repo,
                            MachineRepository machineRepo,
                            OrdineService ordineService) {
        this.repo = repo;
        this.machineRepo = machineRepo;
        this.ordineService = ordineService;
    }

    private Comparator<OrdineProduzione> ordinamento() {
        return Comparator
                .comparing((OrdineProduzione o) ->
                        o.dataScadenza == null ? new Date(9999999999999L) : o.dataScadenza
                )
                .thenComparing(o -> o.id);
    }

    @GetMapping("/dashboard")
    public List<Map<String, Object>> dashboard() {

        List<Map<String, Object>> risultato = new ArrayList<>();

        List<Machine> macchine = machineRepo.findAll().stream()
                .sorted(Comparator.comparing(Machine::getNome))
                .toList();

        for (Machine macchina : macchine) {

            List<OrdineProduzione> lista = repo.findAll().stream()
                    .filter(o -> o.macchina != null &&
                            o.macchina.getId().equals(macchina.getId()) &&
                            o.stato != StatoOrdine.COMPLETATO &&
                            o.stato != StatoOrdine.SOSPESO
                    )
                    .sorted(Comparator.comparing(
                            (OrdineProduzione o) ->
                                    o.dataScadenza == null ? new Date(9999999999999L) : o.dataScadenza
                    ))
                    .toList();

            Map<String, Object> mappa = new HashMap<>();

            mappa.put("macchina", macchina.getNome());

            // 🔥 TROVA ORDINE IN PRODUZIONE
            OrdineProduzione attivo = lista.stream()
                    .filter(o -> o.stato == StatoOrdine.IN_PRODUZIONE)
                    .findFirst()
                    .orElse(null);

            // 🔥 SE NON C'È PRODUZIONE, CERCA SETUP
            if (attivo == null) {
                attivo = lista.stream()
                        .filter(o -> o.stato == StatoOrdine.IN_SETUP)
                        .findFirst()
                        .orElse(null);
            }

            Map<String, Object> attivoMap = new HashMap<>();

            if (attivo != null) {
                attivoMap.put("id", attivo.id);
                attivoMap.put("numeroCommessa", attivo.numeroCommessa);
                attivoMap.put("cliente", attivo.cliente);
                attivoMap.put("codiceParticolare", attivo.codiceParticolare);
                attivoMap.put("pezziProdotti", attivo.pezziProdotti);
                attivoMap.put("quantita", attivo.quantita);
                attivoMap.put("stato", attivo.stato);
                attivoMap.put("tempoCicloSec", attivo.tempoCicloSec);

                // 🔥 CALCOLO BARRE RESTANTI
                int restanti = attivo.quantita - attivo.pezziProdotti;
                int barre = 0;

                if (attivo.lunghezza != null && attivo.macchina != null) {

                    try {
                        double L = Double.parseDouble(attivo.lunghezza.replace(",", "."));
                        double extra = 3.5;

                        double barra = 3000;
                        double scarto = attivo.macchina.getId() == 1 ? 300 : 150;

                        double utile = barra - scarto;
                        double lunghezzaEff = L + extra;

                        int pezziPerBarra = (int) Math.floor(utile / lunghezzaEff);

                        if (pezziPerBarra > 0) {
                            barre = (int) Math.ceil((double) restanti / pezziPerBarra);
                        }
                    } catch (Exception e) {
                        barre = 0;
                    }
                }

            }




            mappa.put("attivo", attivoMap);

            // 🔥 CODA = SOLO CREATO
            List<OrdineProduzione> coda = lista.stream()
                    .filter(o -> o.stato == StatoOrdine.CREATO)
                    .toList();

            mappa.put("coda", coda);

            mappa.put("stato", attivo == null ? "FERMA" : attivo.stato.name());

            risultato.add(mappa);
        }

        return risultato;
    }

    @PostMapping("/{id}/setup")
    public void setup(@PathVariable Long id) {
        ordineService.setup(id);
    }

    @PostMapping("/{id}/start")
    public void start(@PathVariable Long id) {
        ordineService.startProduzione(id);
    }

    @PostMapping("/{id}/versa")
    public void versa(@PathVariable Long id, @RequestParam Integer pezzi) {
        ordineService.versa(id, pezzi);
    }

    @PostMapping("/{id}/chiudi")
    public void chiudi(@PathVariable Long id) {
        ordineService.chiudi(id);
    }

    @PostMapping("/{id}/tempo")
    public void aggiornaTempo(@PathVariable Long id, @RequestParam Integer tempo) {

        OrdineProduzione o = repo.findById(id).orElseThrow();

        o.tempoCicloSec = tempo;

        repo.save(o);
    }

    @PostMapping("/{id}/quantita")
    public void aggiornaQuantita(@PathVariable Long id, @RequestParam Integer quantita) {

        OrdineProduzione o = repo.findById(id).orElseThrow();

        if (quantita < o.pezziProdotti) {
            throw new RuntimeException("Quantità inferiore ai pezzi già prodotti");
        }

        o.quantita = quantita;

        repo.save(o);
    }

    @PostMapping
    public OrdineProduzione crea(@RequestBody Map<String, Object> body) {

        OrdineProduzione o = new OrdineProduzione();

        o.numeroCommessa = (String) body.get("numeroCommessa");
        o.codiceParticolare = (String) body.get("codiceParticolare");
        o.cliente = (String) body.get("cliente");

        o.materiale = (String) body.get("materiale");
        o.diametroBarra = (String) body.get("diametroBarra");
        o.lunghezza = (String) body.get("lunghezza");

        o.quantita = Integer.parseInt(body.get("quantita").toString());
        o.tempoCicloSec = Integer.parseInt(body.get("tempoCicloSec").toString());

        o.stato = StatoOrdine.CREATO;

        String data = (String) body.get("dataScadenza");
        if (data != null && !data.isEmpty()) {
            o.dataScadenza = java.sql.Date.valueOf(data);
        }

        Object pr = body.get("priorita");
        o.priorita = (pr == null) ? 0 : Integer.parseInt(pr.toString());

        Map macchinaMap = (Map) body.get("macchina");
        if (macchinaMap != null && macchinaMap.get("id") != null) {

            Long idMacchina = Long.valueOf(macchinaMap.get("id").toString());
            Machine m = machineRepo.findById(idMacchina).orElseThrow();

            o.macchina = m;
        }

        return repo.save(o);
    }

    @GetMapping("/storico")
    public List<OrdineProduzione> storico(
            @RequestParam(required = false) Integer mese,
            @RequestParam(required = false) Integer anno) {

        List<OrdineProduzione> lista;

        if (mese != null && anno != null) {
            lista = repo.findByMeseAnno(mese, anno);
        } else {
            lista = repo.findAll().stream()
                    .filter(o -> o.stato == StatoOrdine.COMPLETATO)
                    .toList();
        }

        return lista.stream()
                .sorted((a, b) -> {
                    Date da = a.dataChiusura == null ? new Date(0) : a.dataChiusura;
                    Date db = b.dataChiusura == null ? new Date(0) : b.dataChiusura;
                    return db.compareTo(da);
                })
                .toList();
    }

    @GetMapping
    public List<OrdineProduzione> lista() {
        return repo.findAll();
    }

    @PostMapping("/{id}/elimina")
    public void elimina(@PathVariable Long id) {

        OrdineProduzione o = repo.findById(id).orElseThrow();

        if (o.stato == StatoOrdine.COMPLETATO) return;

        repo.deleteById(id);
    }
    @PostMapping("/{id}/macchina")
    public void cambiaMacchina(@PathVariable Long id, @RequestParam Long macchinaId){

        OrdineProduzione o = repo.findById(id).orElseThrow();

        // sicurezza
        if(o.stato != StatoOrdine.CREATO) return;

        Machine m = machineRepo.findById(macchinaId).orElseThrow();

        o.macchina = m;

        repo.save(o);
    }
    @PostMapping("/{id}/sospendi")
    public void sospendi(@PathVariable Long id){
        OrdineProduzione o = repo.findById(id).orElseThrow();
        o.stato = StatoOrdine.SOSPESO;
        repo.save(o);
    }
    @PostMapping("/{id}/riprendi")
    public void riprendi(@PathVariable Long id){
        OrdineProduzione o = repo.findById(id).orElseThrow();

        // puoi decidere dove torna
        o.stato = StatoOrdine.CREATO;

        repo.save(o);
    }



    @GetMapping("/export")
    public ResponseEntity<byte[]> exportExcel(
            @RequestParam int mese,
            @RequestParam int anno) throws IOException {

        List<OrdineProduzione> ordini = repo.findByMeseAnno(mese, anno);

        Workbook workbook = new XSSFWorkbook();

        Sheet sheet = workbook.createSheet("Report");

        // HEADER (grigio + grassetto)
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

// NUMERI centrati
        CellStyle centerStyle = workbook.createCellStyle();
        centerStyle.setAlignment(HorizontalAlignment.CENTER);

// DATA vera Excel
        CellStyle dateStyle = workbook.createCellStyle();
        CreationHelper createHelper = workbook.getCreationHelper();
        dateStyle.setDataFormat(createHelper.createDataFormat().getFormat("dd/MM/yyyy"));

        // 🧾 TITOLO
        Row titolo = sheet.createRow(0);
        titolo.createCell(0).setCellValue("REPORT PRODUZIONE");

        Row info = sheet.createRow(1);
        info.createCell(0).setCellValue("Mese: " + mese + " / " + anno);

        // 📊 KPI
        int totalePezzi = ordini.stream().mapToInt(o -> o.pezziProdotti).sum();
        int totaleTempo = ordini.stream()
                .mapToInt(o -> o.tempoCicloSec * o.pezziProdotti)
                .sum();

        Row kpi = sheet.createRow(2);
        kpi.createCell(0).setCellValue("Totale pezzi");
        kpi.createCell(1).setCellValue(totalePezzi);

        kpi.createCell(2).setCellValue("Ore totali");
        kpi.createCell(3).setCellValue(totaleTempo / 3600.0);

        // 📋 HEADER
        Row header = sheet.createRow(5);

        String[] columns = {
                "Commessa", "Codice", "Cliente", "Materiale",
                "Diametro", "Quantità", "Prodotti",
                "Tempo ciclo (s)", "Macchina", "Data"
        };

        for (int i = 0; i < columns.length; i++) {
            Cell cell = header.createCell(i);
            cell.setCellValue(columns[i]);
            cell.setCellStyle(headerStyle);
        }

        // 📄 DATI
        int rowIdx = 6;

        for (OrdineProduzione o : ordini) {
            Row row = sheet.createRow(rowIdx++);

            row.createCell(0).setCellValue(o.numeroCommessa != null ? o.numeroCommessa : "");
            row.createCell(1).setCellValue(o.codiceParticolare != null ? o.codiceParticolare : "");
            row.createCell(2).setCellValue(o.cliente != null ? o.cliente : "");
            row.createCell(3).setCellValue(o.materiale != null ? o.materiale : "");
            row.createCell(4).setCellValue(o.diametroBarra != null ? o.diametroBarra : "");

            Cell c5 = row.createCell(5);
            c5.setCellValue(o.quantita);
            c5.setCellStyle(centerStyle);

            Cell c6 = row.createCell(6);
            c6.setCellValue(o.pezziProdotti);
            c6.setCellStyle(centerStyle);

            Cell c7 = row.createCell(7);
            c7.setCellValue(o.tempoCicloSec);
            c7.setCellStyle(centerStyle);

            row.createCell(8).setCellValue(
                    o.macchina != null ? o.macchina.getNome() : "");

            Cell dataCell = row.createCell(9);

            if (o.dataChiusura != null) {
                dataCell.setCellValue(o.dataChiusura);
                dataCell.setCellStyle(dateStyle);
            } else {
                dataCell.setCellValue("");
            }
        }

        // 📏 AUTO SIZE
        for (int i = 0; i < columns.length; i++) {
            sheet.autoSizeColumn(i);

            int currentWidth = sheet.getColumnWidth(i);

            // larghezza minima (circa 20 caratteri)
            int minWidth = 20 * 256;

            if (currentWidth < minWidth) {
                sheet.setColumnWidth(i, minWidth);
            } else {
                // aggiunge un po' di spazio extra
                sheet.setColumnWidth(i, currentWidth + 500);
            }
            sheet.setColumnWidth(0, 4000); // Commessa
            sheet.setColumnWidth(1, 5000); // Codice
            sheet.setColumnWidth(2, 6000); // Cliente
            sheet.setColumnWidth(3, 5000); // Materiale
            sheet.setColumnWidth(4, 3000); // Diametro
            sheet.setColumnWidth(8, 3000); // Macchina
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        workbook.write(out);
        workbook.close();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(
                ContentDisposition.attachment()
                        .filename("report_mes_" + mese + "_" + anno + ".xlsx")
                        .build()
        );

        return ResponseEntity.ok()
                .headers(headers)
                .body(out.toByteArray());
    }
}