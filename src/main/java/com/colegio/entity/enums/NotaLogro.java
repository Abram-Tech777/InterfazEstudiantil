package com.colegio.entity.enums;

import java.math.BigDecimal;

public enum NotaLogro {
    DESTACADO("Destacado", "Demuestra desempeño sobresaliente", 18, 20),
    LOGRO_ESPERADO("Logro Esperado", "Alcanza los aprendizajes esperados", 14, 17),
    PROCESO("Proceso", "En camino de lograr los aprendizajes", 11, 13),
    INICIO("Inicio", "Requiere apoyo para alcanzar los aprendizajes", 0, 10);

    private final String nombre;
    private final String descripcion;
    private final int minNota;
    private final int maxNota;

    NotaLogro(String nombre, String descripcion, int minNota, int maxNota) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.minNota = minNota;
        this.maxNota = maxNota;
    }

    public String getNombre() { return nombre; }
    public String getDescripcion() { return descripcion; }
    public int getMinNota() { return minNota; }
    public int getMaxNota() { return maxNota; }

    public static NotaLogro fromNota(BigDecimal nota) {
        if (nota == null) return null;
        int val = nota.intValue();
        if (val >= 18) return DESTACADO;
        if (val >= 14) return LOGRO_ESPERADO;
        if (val >= 11) return PROCESO;
        return INICIO;
    }

    public static NotaLogro fromLetra(String letra) {
        if (letra == null) return null;
        return switch (letra.toUpperCase()) {
            case "AD" -> DESTACADO;
            case "A" -> LOGRO_ESPERADO;
            case "B" -> PROCESO;
            case "C" -> INICIO;
            default -> null;
        };
    }
}
