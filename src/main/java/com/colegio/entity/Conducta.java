package com.colegio.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "conducta")
public class Conducta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idConducta;

    @ManyToOne
    @JoinColumn(name = "id_alumno")
    private Alumno alumno;

    @ManyToOne
    @JoinColumn(name = "id_docente")
    private Docente docente;

    private String tipo;
    private String titulo;
    private String descripcion;
    private String observaciones;
    private String icono;
    private String colorIcono;
    private Integer bimestre;
    private Integer anio;
    private LocalDate fechaRegistro;

    public Integer getIdConducta() { return idConducta; }
    public void setIdConducta(Integer idConducta) { this.idConducta = idConducta; }
    public Alumno getAlumno() { return alumno; }
    public void setAlumno(Alumno alumno) { this.alumno = alumno; }
    public Docente getDocente() { return docente; }
    public void setDocente(Docente docente) { this.docente = docente; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
    public String getIcono() { return icono; }
    public void setIcono(String icono) { this.icono = icono; }
    public String getColorIcono() { return colorIcono; }
    public void setColorIcono(String colorIcono) { this.colorIcono = colorIcono; }
    public Integer getBimestre() { return bimestre; }
    public void setBimestre(Integer bimestre) { this.bimestre = bimestre; }
    public Integer getAnio() { return anio; }
    public void setAnio(Integer anio) { this.anio = anio; }
    public LocalDate getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDate fechaRegistro) { this.fechaRegistro = fechaRegistro; }
}