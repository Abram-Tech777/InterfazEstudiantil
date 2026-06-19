package com.colegio.entity;

import java.time.LocalDateTime;
import jakarta.persistence.*;

@Entity
@Table(name = "aula_docente")
public class AulaDocente {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_aula_docente")
    private int idAulaDocente;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_docente", nullable = false)
    private Docente docente;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_aula", nullable = false)
    private Aula aula;

    @Column(name = "rol", length = 30)
    private String rol = "DOCENTE";

    @Column(name = "activo")
    private boolean activo = true;

    @Column(name = "creado_en", insertable = false, updatable = false)
    private LocalDateTime creadoEn;

    public AulaDocente() {}

    public AulaDocente(Docente docente, Aula aula, String rol) {
        this.docente = docente;
        this.aula = aula;
        this.rol = rol;
        this.activo = true;
    }

    public int getIdAulaDocente() {
        return idAulaDocente;
    }

    public void setIdAulaDocente(int idAulaDocente) {
        this.idAulaDocente = idAulaDocente;
    }

    public Docente getDocente() {
        return docente;
    }

    public void setDocente(Docente docente) {
        this.docente = docente;
    }

    public Aula getAula() {
        return aula;
    }

    public void setAula(Aula aula) {
        this.aula = aula;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public LocalDateTime getCreadoEn() {
        return creadoEn;
    }

    public void setCreadoEn(LocalDateTime creadoEn) {
        this.creadoEn = creadoEn;
    }
}
