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

    public String cliente;

    public String materiale;
    public String diametroBarra;

    public int quantita;
    public int pezziProdotti = 0;

    public int tempoCicloSec;

    public String stato = "CREATO";

    public Date dataChiusura;

    // 🔥 NUOVA RELAZIONE
    @ManyToOne
    @JoinColumn(name = "machine_id")
    public Machine macchina;

    public OrdineProduzione() {
    }
}