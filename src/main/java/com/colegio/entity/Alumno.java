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

    // Note: Alumno.estado removed — database column must be dropped separately after deploy

    // Helpers for templates that may use nombre / apellido
    public String getNombre() {
        if (nombreCompleto == null || nombreCompleto.isBlank()) return "";
        String[] parts = nombreCompleto.trim().split("\\s+", 2);
        return parts[0];
    }

    public void setNombre(String nombre) {
        String apellido = getApellido();
        if (apellido == null || apellido.isEmpty()) {
            this.nombreCompleto = nombre;
        } else {
            this.nombreCompleto = nombre + " " + apellido;
        }
    }

    public String getApellido() {
        if (nombreCompleto == null || nombreCompleto.isBlank()) return "";
        String[] parts = nombreCompleto.trim().split("\\s+", 2);
        return parts.length > 1 ? parts[1] : "";
    }

    public void setApellido(String apellido) {
        String nombre = getNombre();
        if (nombre == null || nombre.isEmpty()) {
            this.nombreCompleto = apellido;
        } else {
            this.nombreCompleto = nombre + " " + apellido;
        }
    }


   
}
