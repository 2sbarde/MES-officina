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
    public String lunghezza;
    public String fasi;

    public int quantita;
    public int pezziProdotti = 0;

    public int tempoCicloSec;

    @Enumerated(EnumType.STRING)
    public StatoOrdine stato = StatoOrdine.CREATO;

    public Date dataChiusura;
    public Date dataScadenza;
    public Integer priorita;

    @ManyToOne
    @JoinColumn(name = "machine_id")
    public Machine macchina;

    public Long timestampInizio;
    public Long timestampFine;

    public OrdineProduzione() {}
}