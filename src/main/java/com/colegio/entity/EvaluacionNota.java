package com.colegio.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "evaluacion_nota")
public class EvaluacionNota {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_nota")
    private Integer idNota;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_alumno", nullable = false)
    private Alumno alumno;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_curso", nullable = false)
    private Curso curso;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_docente", nullable = false)
    private Docente docente;

    @Column(name = "bimestre", nullable = false)
    private Integer bimestre;

    @Column(name = "nota", nullable = false, precision = 4, scale = 2)
    private BigDecimal nota;

    @Column(name = "fecha_registro", nullable = false)
    private LocalDate fechaRegistro;

    public EvaluacionNota() {}

    public EvaluacionNota(Alumno alumno, Curso curso, Docente docente, Integer bimestre, BigDecimal nota, LocalDate fechaRegistro) {
        this.alumno = alumno;
        this.curso = curso;
        this.docente = docente;
        this.bimestre = bimestre;
        this.nota = nota;
        this.fechaRegistro = fechaRegistro;
    }

    // Getters y Setters
    public Integer getIdNota() {
        return idNota;
    }

    public void setIdNota(Integer idNota) {
        this.idNota = idNota;
    }

    public Alumno getAlumno() {
        return alumno;
    }

    public void setAlumno(Alumno alumno) {
        this.alumno = alumno;
    }

    public Curso getCurso() {
        return curso;
    }

    public void setCurso(Curso curso) {
        this.curso = curso;
    }

    public Docente getDocente() {
        return docente;
    }

    public void setDocente(Docente docente) {
        this.docente = docente;
    }

    public Integer getBimestre() {
        return bimestre;
    }

    public void setBimestre(Integer bimestre) {
        this.bimestre = bimestre;
    }

    public BigDecimal getNota() {
        return nota;
    }

    public void setNota(BigDecimal nota) {
        this.nota = nota;
    }

    public LocalDate getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDate fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    // Transient helpers para compatibilidad con plantillas
    @Transient
    public String getTipo() {
        return "Bimestre " + (bimestre != null ? bimestre : "?");
    }

    @Transient
    public Integer getPeso() {
        return 100; // Peso por defecto
    }

    @Transient
    public String getValor() {
        return nota != null ? nota.toPlainString() : "N/A";
    }
}
