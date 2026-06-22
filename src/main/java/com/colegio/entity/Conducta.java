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

    private String titulo;
    private String descripcion;
    private String icono;
    private String colorIcono;
    private LocalDate fechaRegistro;

    // Getters y Setters
    public Integer getIdConducta() { return idConducta; }
    public void setIdConducta(Integer idConducta) { this.idConducta = idConducta; }
    public Alumno getAlumno() { return alumno; }
    public void setAlumno(Alumno alumno) { this.alumno = alumno; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public String getIcono() { return icono; }
    public void setIcono(String icono) { this.icono = icono; }
    public String getColorIcono() { return colorIcono; }
    public void setColorIcono(String colorIcono) { this.colorIcono = colorIcono; }
}