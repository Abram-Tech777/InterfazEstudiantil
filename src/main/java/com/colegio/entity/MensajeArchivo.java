package com.colegio.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
@Table(name = "mensaje_archivo")
public class MensajeArchivo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_archivo")
    private Integer idArchivo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_mensaje", nullable = false)
    @JsonIgnore
    private Mensaje mensaje;

    @Column(name = "archivo_data", columnDefinition = "LONGBLOB")
    @JsonIgnore
    private byte[] archivoData;

    @Column(name = "archivo_nombre", length = 255)
    private String archivoNombre;

    @Column(name = "archivo_tipo", length = 100)
    private String archivoTipo;

    public MensajeArchivo() {}

    public Integer getIdArchivo() { return idArchivo; }
    public void setIdArchivo(Integer idArchivo) { this.idArchivo = idArchivo; }

    public Mensaje getMensaje() { return mensaje; }
    public void setMensaje(Mensaje mensaje) { this.mensaje = mensaje; }

    public byte[] getArchivoData() { return archivoData; }
    public void setArchivoData(byte[] archivoData) { this.archivoData = archivoData; }

    public String getArchivoNombre() { return archivoNombre; }
    public void setArchivoNombre(String archivoNombre) { this.archivoNombre = archivoNombre; }

    public String getArchivoTipo() { return archivoTipo; }
    public void setArchivoTipo(String archivoTipo) { this.archivoTipo = archivoTipo; }
}
