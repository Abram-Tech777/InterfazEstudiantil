package com.colegio.entity.enums;

public enum EscalaCalificacion {
    DECIMAL("Decimal", "0 - 20"),
    LETRA("Letra", "AD - A - B - C"),
    LOGRO("Logro", "Destacado - Logro Esperado - Proceso - Inicio");

    private final String nombre;
    private final String rango;

    EscalaCalificacion(String nombre, String rango) {
        this.nombre = nombre;
        this.rango = rango;
    }

    public String getNombre() { return nombre; }
    public String getRango() { return rango; }
}
