package com.colegio.util;

import com.colegio.entity.enums.NotaLetra;
import com.colegio.entity.enums.NotaLogro;
import java.math.BigDecimal;

public class ConversionNotas {

    public static String notaLiteral(BigDecimal nota) {
        NotaLetra nl = NotaLetra.fromNota(nota);
        return nl != null ? nl.getCodigo() : null;
    }

    public static String notaLiteralDescripcion(BigDecimal nota) {
        NotaLetra nl = NotaLetra.fromNota(nota);
        return nl != null ? nl.getDescripcion() : null;
    }

    public static String notaLegible(BigDecimal nota) {
        if (nota == null) return "N/A";
        return nota.stripTrailingZeros().toPlainString();
    }

    public static String notaLogro(BigDecimal nota) {
        NotaLogro nl = NotaLogro.fromNota(nota);
        return nl != null ? nl.getNombre() : null;
    }

    public static String notaLogroFromLetra(String letra) {
        NotaLogro nl = NotaLogro.fromLetra(letra);
        return nl != null ? nl.getNombre() : null;
    }

    public static String convertirEscala(BigDecimal nota, String escalaDestino) {
        if (nota == null) return "";
        return switch (escalaDestino.toUpperCase()) {
            case "LETRA" -> notaLiteral(nota);
            case "LOGRO" -> notaLogro(nota);
            default -> notaLegible(nota);
        };
    }
}
