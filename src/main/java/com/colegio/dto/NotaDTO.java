package com.colegio.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class NotaDTO {
    private Integer idNota;
    private Integer idAlumno;
    private String alumnoNombre;
    private Integer idCurso;
    private String cursoNombre;
    private Integer idDocente;
    private String docenteNombre;
    private Integer bimestre;
    private BigDecimal nota;
    private String notaLiteral;
    private String notaLogro;
    private String escala;
    private LocalDate fechaRegistro;
    private String estado;

    public NotaDTO() {}

    public Integer getIdNota() { return idNota; }
    public void setIdNota(Integer idNota) { this.idNota = idNota; }
    public Integer getIdAlumno() { return idAlumno; }
    public void setIdAlumno(Integer idAlumno) { this.idAlumno = idAlumno; }
    public String getAlumnoNombre() { return alumnoNombre; }
    public void setAlumnoNombre(String alumnoNombre) { this.alumnoNombre = alumnoNombre; }
    public Integer getIdCurso() { return idCurso; }
    public void setIdCurso(Integer idCurso) { this.idCurso = idCurso; }
    public String getCursoNombre() { return cursoNombre; }
    public void setCursoNombre(String cursoNombre) { this.cursoNombre = cursoNombre; }
    public Integer getIdDocente() { return idDocente; }
    public void setIdDocente(Integer idDocente) { this.idDocente = idDocente; }
    public String getDocenteNombre() { return docenteNombre; }
    public void setDocenteNombre(String docenteNombre) { this.docenteNombre = docenteNombre; }
    public Integer getBimestre() { return bimestre; }
    public void setBimestre(Integer bimestre) { this.bimestre = bimestre; }
    public BigDecimal getNota() { return nota; }
    public void setNota(BigDecimal nota) { this.nota = nota; }
    public String getNotaLiteral() { return notaLiteral; }
    public void setNotaLiteral(String notaLiteral) { this.notaLiteral = notaLiteral; }
    public String getNotaLogro() { return notaLogro; }
    public void setNotaLogro(String notaLogro) { this.notaLogro = notaLogro; }
    public String getEscala() { return escala; }
    public void setEscala(String escala) { this.escala = escala; }
    public LocalDate getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDate fechaRegistro) { this.fechaRegistro = fechaRegistro; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
}
