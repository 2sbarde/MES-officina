package com.mes.mes_officina;

import jakarta.persistence.*;

@Entity
public class OrdineProduzione {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    public String numeroCommessa;
    public String codiceParticolare;

    public int quantita;
    public int pezziProdotti = 0;

    public int tempoCicloSec;

    public String stato = "CREATO"; // CREATO, IN_SETUP, IN_PRODUZIONE, COMPLETATO

    // 🔥 relazione macchina
    @ManyToOne
    @JoinColumn(name = "macchina_id")
    public Macchina macchina;

    public OrdineProduzione() {
    }
}