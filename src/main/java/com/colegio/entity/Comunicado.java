package com.colegio.entity;

import java.time.LocalDateTime;
import jakarta.persistence.*;

@Entity
@Table(name = "comunicado")
public class Comunicado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_comunicado")
    private int idComunicado;


    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario autor;


    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_aula", nullable = true)
    private Aula aula;

    @Column(name = "grado", nullable = true, length = 50)
    private String grado;

    @Column(name = "titulo", nullable = false, length = 150)
    private String titulo;

    @Column(name = "contenido", nullable = false, columnDefinition = "TEXT")
    private String contenido;

    @Column(name = "fecha_emision")
    private LocalDateTime fechaEmision;

    @Column(name = "ruta_adjunto", nullable = true, length = 500)
    private String rutaAdjunto;

    @Column(name = "archivo_data", columnDefinition = "LONGBLOB")
    private byte[] archivoData;

    @Column(name = "archivo_nombre", nullable = true, length = 255)
    private String archivoNombre;

    @Column(name = "archivo_tipo", nullable = true, length = 100)
    private String archivoTipo;

    public Comunicado() {}

    // Getters / Setters
    public int getIdComunicado() { return idComunicado; }
    public void setIdComunicado(int idComunicado) { this.idComunicado = idComunicado; }

    public Usuario getAutor() { return autor; }
    public void setAutor(Usuario autor) { this.autor = autor; }

    public Aula getAula() { return aula; }
    public void setAula(Aula aula) { this.aula = aula; }

    public String getGrado() { return grado; }
    public void setGrado(String grado) { this.grado = grado; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getContenido() { return contenido; }
    public void setContenido(String contenido) { this.contenido = contenido; }

    public LocalDateTime getFechaEmision() { return fechaEmision; }
    public void setFechaEmision(LocalDateTime fechaEmision) { this.fechaEmision = fechaEmision; }

    public String getRutaAdjunto() { return rutaAdjunto; }
    public void setRutaAdjunto(String rutaAdjunto) { this.rutaAdjunto = rutaAdjunto; }

    public byte[] getArchivoData() { return archivoData; }
    public void setArchivoData(byte[] archivoData) { this.archivoData = archivoData; }

    public String getArchivoNombre() { return archivoNombre; }
    public void setArchivoNombre(String archivoNombre) { this.archivoNombre = archivoNombre; }

    public String getArchivoTipo() { return archivoTipo; }
    public void setArchivoTipo(String archivoTipo) { this.archivoTipo = archivoTipo; }
}
