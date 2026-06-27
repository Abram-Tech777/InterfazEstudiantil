package com.colegio.entity;

import java.time.LocalDateTime;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "mensaje")
public class Mensaje {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_mensaje")
    private Integer idMensaje;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_remitente", nullable = false)
    private Usuario remitente;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_destinatario", nullable = false)
    private Usuario destinatario;

    @Column(name = "asunto", length = 150)
    private String asunto;

    @Column(name = "contenido", columnDefinition = "TEXT")
    private String contenido;

    @Column(name = "fecha_envio")
    private LocalDateTime fechaEnvio;

    @Column(name = "leido")
    private Boolean leido = false;

    @OneToMany(mappedBy = "mensaje", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<MensajeArchivo> archivos;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_comunicado_referencia")
    @JsonIgnoreProperties({"archivoData", "contenido", "aula", "grado", "rutaAdjunto"})
    private Comunicado comunicadoReferencia;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_mensaje_padre")
    @JsonIgnoreProperties({"respuestas", "mensajePadre", "archivos"})
    private Mensaje mensajePadre;

    @OneToMany(mappedBy = "mensajePadre", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnoreProperties({"mensajePadre", "mensajePadre"})
    @com.fasterxml.jackson.annotation.JsonIgnore
    private List<Mensaje> respuestas;

    public Mensaje() {}

    public Integer getIdMensaje() { return idMensaje; }
    public void setIdMensaje(Integer idMensaje) { this.idMensaje = idMensaje; }

    public Usuario getRemitente() { return remitente; }
    public void setRemitente(Usuario remitente) { this.remitente = remitente; }

    public Usuario getDestinatario() { return destinatario; }
    public void setDestinatario(Usuario destinatario) { this.destinatario = destinatario; }

    public String getAsunto() { return asunto; }
    public void setAsunto(String asunto) { this.asunto = asunto; }

    public String getContenido() { return contenido; }
    public void setContenido(String contenido) { this.contenido = contenido; }

    public LocalDateTime getFechaEnvio() { return fechaEnvio; }
    public void setFechaEnvio(LocalDateTime fechaEnvio) { this.fechaEnvio = fechaEnvio; }

    public Boolean getLeido() { return leido != null ? leido : false; }
    public void setLeido(Boolean leido) { this.leido = leido; }

    public List<MensajeArchivo> getArchivos() { return archivos; }
    public void setArchivos(List<MensajeArchivo> archivos) { this.archivos = archivos; }

    public Comunicado getComunicadoReferencia() { return comunicadoReferencia; }
    public void setComunicadoReferencia(Comunicado comunicadoReferencia) { this.comunicadoReferencia = comunicadoReferencia; }

    public Mensaje getMensajePadre() { return mensajePadre; }
    public void setMensajePadre(Mensaje mensajePadre) { this.mensajePadre = mensajePadre; }

    public List<Mensaje> getRespuestas() { return respuestas; }
    public void setRespuestas(List<Mensaje> respuestas) { this.respuestas = respuestas; }

    @com.fasterxml.jackson.annotation.JsonIgnore
    public boolean isEsRespuesta() { return mensajePadre != null; }
}
