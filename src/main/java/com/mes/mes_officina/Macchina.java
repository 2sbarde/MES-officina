package com.mes.mes_officina;

public class Macchina {

    public String nome;
    public String stato = "LIBERA"; // LIBERA, SETUP, PRODUZIONE
    public Long ordineId;

    public Macchina(String nome) {
        this.nome = nome;
    }
}