package com.mes.mes_officina;

import java.time.LocalDate;

public class OrdineProduzione {

    public Long id;

    public String numeroCommessa;
    public String codiceParticolare;

    public int quantita;
    public int tempoCicloSec;

    public double diametroBarra;
    public String materiale;

    public String macchina;

    public int pezziProdotti = 0;

    public String stato = "IN_ATTESA";

    public LocalDate dataConsegna;
}