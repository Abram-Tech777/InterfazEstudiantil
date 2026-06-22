package com.colegio.dto;

import java.time.LocalTime;
import java.util.Map;
import java.util.TreeMap;

/**
 * DTO que representa el horario en formato matriz (horas x dias)
 * Estructura: Map<hora_inicio, Map<dia_semana, HorarioDTO>>
 */
public class HorarioMatrizDTO {
    private Map<LocalTime, Map<String, HorarioDTO>> matriz;
    private String aulaNombre;
    private String aulaGrado;

    public HorarioMatrizDTO() {
        this.matriz = new TreeMap<>();
    }

    public HorarioMatrizDTO(String aulaNombre, String aulaGrado) {
        this();
        this.aulaNombre = aulaNombre;
        this.aulaGrado = aulaGrado;
    }

    /**
     * Agregar un horario a la matriz
     */
    public void agregarHorario(LocalTime horaInicio, String diaSemana, HorarioDTO horario) {
        matriz.computeIfAbsent(horaInicio, k -> new TreeMap<>())
               .put(diaSemana, horario);
    }

    /**
     * Obtener el horario para una hora y dia especificos
     */
    public HorarioDTO obtenerHorario(LocalTime horaInicio, String diaSemana) {
        if (matriz.containsKey(horaInicio)) {
            return matriz.get(horaInicio).get(diaSemana);
        }
        return null;
    }

    /**
     * Verificar si existe un horario para una hora y dia
     */
    public boolean existeHorario(LocalTime horaInicio, String diaSemana) {
        return obtenerHorario(horaInicio, diaSemana) != null;
    }

    public Map<LocalTime, Map<String, HorarioDTO>> getMatriz() { return matriz; }
    public void setMatriz(Map<LocalTime, Map<String, HorarioDTO>> matriz) { this.matriz = matriz; }

    public String getAulaNombre() { return aulaNombre; }
    public void setAulaNombre(String aulaNombre) { this.aulaNombre = aulaNombre; }

    public String getAulaGrado() { return aulaGrado; }
    public void setAulaGrado(String aulaGrado) { this.aulaGrado = aulaGrado; }

    /**
     * Obtener lista de horas unicas en la matriz (ordenadas)
     */
    public java.util.List<LocalTime> getHoras() {
        return new java.util.ArrayList<>(matriz.keySet());
    }

    /**
     * Obtener lista de dias de la semana
     */
    public java.util.List<String> getDias() {
        return java.util.List.of("Lunes", "Martes", "Miercoles", "Jueves", "Viernes");
    }
}
