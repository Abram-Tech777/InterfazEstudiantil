package com.colegio.dto;

import java.time.LocalTime;

public class SlotDTO {

    private LocalTime inicio;
    private LocalTime fin;
    private String tipo;
    private int numeroPeriodo;

    public SlotDTO() {}

    public SlotDTO(LocalTime inicio, LocalTime fin, String tipo, int numeroPeriodo) {
        this.inicio = inicio;
        this.fin = fin;
        this.tipo = tipo;
        this.numeroPeriodo = numeroPeriodo;
    }

    public LocalTime getInicio() { return inicio; }
    public void setInicio(LocalTime inicio) { this.inicio = inicio; }

    public LocalTime getFin() { return fin; }
    public void setFin(LocalTime fin) { this.fin = fin; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public int getNumeroPeriodo() { return numeroPeriodo; }
    public void setNumeroPeriodo(int numeroPeriodo) { this.numeroPeriodo = numeroPeriodo; }
}
