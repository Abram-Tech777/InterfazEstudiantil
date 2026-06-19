package com.colegio.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "alumno")
public class Alumno {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_alumno")
    private int idAlumno;

    @Column(name = "nombre_completo", length = 100, nullable = true)
    private String nombreCompleto;

    @Column(name = "estado", length = 15, nullable = false)
    private String estado = "ACTIVO";

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "id_aula", nullable = true)
	private Aula aula;

	@OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_usuario", unique = true)
    private Usuario usuario;

    public Alumno() {}

	public int getIdAlumno() {
		return idAlumno;
	}

	public void setIdAlumno(int idAlumno) {
		this.idAlumno = idAlumno;
	}

	public String getNombreCompleto() {
		return nombreCompleto;
	}

	public void setNombreCompleto(String nombreCompleto) {
		this.nombreCompleto = nombreCompleto;
	}

	public Aula getAula() {
		return aula;
	}

	public void setAula(Aula aula) {
		this.aula = aula;
	}

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }


   
}
