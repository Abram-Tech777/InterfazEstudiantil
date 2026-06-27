package com.colegio.entity.enums;

import java.math.BigDecimal;

public enum NotaLetra {
    AD("AD", "Logro Destacado", 18, 20),
    A("A", "Logro Esperado", 14, 17),
    B("B", "En Proceso", 11, 13),
    C("C", "En Inicio", 0, 10);

    private final String codigo;
    private final String descripcion;
    private final int minNota;
    private final int maxNota;

    NotaLetra(String codigo, String descripcion, int minNota, int maxNota) {
        this.codigo = codigo;
        this.descripcion = descripcion;
        this.minNota = minNota;
        this.maxNota = maxNota;
    }

    public String getCodigo() { return codigo; }
    public String getDescripcion() { return descripcion; }
    public int getMinNota() { return minNota; }
    public int getMaxNota() { return maxNota; }

    public static NotaLetra fromNota(BigDecimal nota) {
        if (nota == null) return null;
        int val = nota.intValue();
        if (val >= 18) return AD;
        if (val >= 14) return A;
        if (val >= 11) return B;
        return C;
    }
}
