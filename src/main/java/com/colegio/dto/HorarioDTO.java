package com.colegio.dto;

import java.time.LocalTime;

public class HorarioDTO {
    private int idHorario;
    private String cursoNombre;
    private String docenteNombre;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private String diaSemana;

    public HorarioDTO() {}

    public HorarioDTO(int idHorario, String cursoNombre, String docenteNombre, 
                      LocalTime horaInicio, LocalTime horaFin, String diaSemana) {
        this.idHorario = idHorario;
        this.cursoNombre = cursoNombre;
        this.docenteNombre = docenteNombre;
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
        this.diaSemana = diaSemana;
    }

    public int getIdHorario() { return idHorario; }
    public void setIdHorario(int idHorario) { this.idHorario = idHorario; }

    public String getCursoNombre() { return cursoNombre; }
    public void setCursoNombre(String cursoNombre) { this.cursoNombre = cursoNombre; }

    public String getDocenteNombre() { return docenteNombre; }
    public void setDocenteNombre(String docenteNombre) { this.docenteNombre = docenteNombre; }

    public LocalTime getHoraInicio() { return horaInicio; }
    public void setHoraInicio(LocalTime horaInicio) { this.horaInicio = horaInicio; }

    public LocalTime getHoraFin() { return horaFin; }
    public void setHoraFin(LocalTime horaFin) { this.horaFin = horaFin; }

    public String getDiaSemana() { return diaSemana; }
    public void setDiaSemana(String diaSemana) { this.diaSemana = diaSemana; }
}
