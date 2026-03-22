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

    public String materiale;
    public double diametroBarra;

    public String stato;
    public String macchina;

    public OrdineProduzione() {}
}