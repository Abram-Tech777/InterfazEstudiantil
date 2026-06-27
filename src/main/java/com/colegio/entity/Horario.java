package com.colegio.entity;


import java.time.LocalTime;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "horario")
public class Horario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_horario")
    private int idHorario;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_aula", nullable = false)
    private Aula aula;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_docente", nullable = false)
    private Docente docente;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_curso", nullable = false)
    private Curso curso;

    @Column(name = "dia_semana", nullable = false, length = 15)
    private String diaSemana;

    @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
    @Column(name = "hora_inicio", nullable = false)
    private LocalTime horaInicio;

    @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
    @Column(name = "hora_fin", nullable = false)
    private LocalTime horaFin;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @Column(name = "fecha_inicio")
    private LocalDate fechaInicio;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @Column(name = "fecha_fin")
    private LocalDate fechaFin;

    @Column(name = "activo")
    private Boolean activo = true;

    @Column(name = "tipo", nullable = false, length = 20)
    private String tipo = "CLASE";

    public Horario() {}


    public int getIdHorario() { return idHorario; }
    public void setIdHorario(int idHorario) { this.idHorario = idHorario; }

    public Aula getAula() { return aula; }
    public void setAula(Aula aula) { this.aula = aula; }

    public Docente getDocente() { return docente; }
    public void setDocente(Docente docente) { this.docente = docente; }

    public int getIdDocente() { return docente != null ? docente.getIdDocente() : 0; }
    public void setIdDocente(int idDocente) { 
        if (this.docente == null) {
            this.docente = new Docente();
        }
        this.docente.setIdDocente(idDocente);
    }

    public Curso getCurso() { return curso; }
    public void setCurso(Curso curso) { this.curso = curso; }

    public int getIdCurso() { return curso != null ? curso.getIdCurso() : 0; }
    public void setIdCurso(int idCurso) {
        if (this.curso == null) {
            this.curso = new Curso();
        }
        this.curso.setIdCurso(idCurso);
    }

    public String getDiaSemana() { return diaSemana; }
    public void setDiaSemana(String diaSemana) { this.diaSemana = diaSemana; }

    public LocalTime getHoraInicio() { return horaInicio; }
    public void setHoraInicio(LocalTime horaInicio) { this.horaInicio = horaInicio; }

    public LocalTime getHoraFin() { return horaFin; }
    public void setHoraFin(LocalTime horaFin) { this.horaFin = horaFin; }

    public LocalDate getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDate fechaInicio) { this.fechaInicio = fechaInicio; }

    public LocalDate getFechaFin() { return fechaFin; }
    public void setFechaFin(LocalDate fechaFin) { this.fechaFin = fechaFin; }

    public Boolean getActivo() { return activo != null ? activo : true; }
    public void setActivo(Boolean activo) { this.activo = activo; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
}