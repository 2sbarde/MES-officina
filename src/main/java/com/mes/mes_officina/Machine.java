package com.mes.mes_officina;

import jakarta.persistence.*;

@Entity
public class Machine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    public String nome;

    public String stato; // FERMA, IN_SETUP, IN_PRODUZIONE

    public Machine() {
    }
}