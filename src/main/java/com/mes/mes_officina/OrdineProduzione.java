package com.mes.mes_officina;

import jakarta.persistence.*;
import java.util.Date;

@Entity
public class OrdineProduzione {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    public String numeroCommessa;
    public String codiceParticolare;

    public String materiale;
    public String diametroBarra;

    public int quantita;
    public int pezziProdotti = 0;

    public int tempoCicloSec;

    // ✅ SOLO UNA VOLTA
    public String stato = "CREATO";

    public Date dataChiusura;

    // relazione macchina (se usi stringa semplice lascia così)
    public String macchina;

    public OrdineProduzione() {
    }
}