package com.colegio.dto;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class HorarioMatrizDTO {

    private List<FilaHorarioDTO> filas;
    private String aulaNombre;
    private String aulaGrado;

    public HorarioMatrizDTO() {
        this.filas = new ArrayList<>();
    }

    public HorarioMatrizDTO(String aulaNombre, String aulaGrado) {
        this();
        this.aulaNombre = aulaNombre;
        this.aulaGrado = aulaGrado;
    }

    public void agregarFila(FilaHorarioDTO fila) {
        filas.add(fila);
    }

    /**
     * Backward-compatible: agregar horario by start time and day
     */
    @Deprecated
    public void agregarHorario(LocalTime horaInicio, String diaSemana, HorarioDTO horario) {
        for (FilaHorarioDTO fila : filas) {
            if (!fila.isRecreo() && fila.getHoraInicio().equals(horaInicio)) {
                fila.asignarHorario(diaSemana, horario);
                return;
            }
        }
        FilaHorarioDTO fila = new FilaHorarioDTO(horaInicio, horario.getHoraFin(), false);
        fila.asignarHorario(diaSemana, horario);
        filas.add(fila);
    }

    /**
     * Backward-compatible: returns start times of all non-recreo rows
     */
    public List<LocalTime> getHoras() {
        List<LocalTime> horas = new ArrayList<>();
        for (FilaHorarioDTO fila : filas) {
            if (!fila.isRecreo()) {
                horas.add(fila.getHoraInicio());
            }
        }
        return horas;
    }

    /**
     * Backward-compatible: find horario by start time and day
     */
    public HorarioDTO obtenerHorario(LocalTime horaInicio, String diaSemana) {
        for (FilaHorarioDTO fila : filas) {
            if (!fila.isRecreo() && fila.getHoraInicio().equals(horaInicio)) {
                return fila.getHorario(diaSemana);
            }
        }
        return null;
    }

    public List<FilaHorarioDTO> getFilas() { return filas; }
    public void setFilas(List<FilaHorarioDTO> filas) { this.filas = filas; }

    public String getAulaNombre() { return aulaNombre; }
    public void setAulaNombre(String aulaNombre) { this.aulaNombre = aulaNombre; }

    public String getAulaGrado() { return aulaGrado; }
    public void setAulaGrado(String aulaGrado) { this.aulaGrado = aulaGrado; }

    public List<String> getDias() {
        return List.of("Lunes", "Martes", "Miercoles", "Jueves", "Viernes", "Sabado");
    }
}
