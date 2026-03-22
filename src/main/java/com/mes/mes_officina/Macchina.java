package com.mes.mes_officina;

import jakarta.persistence.*;

@Entity
public class Macchina {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;

    // COSTRUTTORI
    public Macchina() {}

    public Macchina(String nome) {
        this.nome = nome;
    }

    // GETTER E SETTER
    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }
}