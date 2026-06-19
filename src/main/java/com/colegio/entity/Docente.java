package com.colegio.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;



@Entity
@Table(name = "docente")
public class Docente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_docente")
    private int idDocente;


    @Column(name = "nombre_completo", length = 100, nullable = true)
    private String nombreCompleto;

  

	@OneToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "id_usuario", unique = true)
	private Usuario usuario;

	public Docente() {}


	

    public Docente(String nombreCompleto, Usuario usuario) {
		super();
		this.nombreCompleto = nombreCompleto;
		this.usuario = usuario;
	}

    
    
	public Docente(int idDocente, String nombreCompleto, Usuario usuario) {
		super();
		this.idDocente = idDocente;
		this.nombreCompleto = nombreCompleto;
		this.usuario = usuario;
	}




	public int getIdDocente() {
		return idDocente;
	}




	public void setIdDocente(int idDocente) {
		this.idDocente = idDocente;
	}




	public String getNombreCompleto() {
		return nombreCompleto;
	}




	public void setNombreCompleto(String nombreCompleto) {
		this.nombreCompleto = nombreCompleto;
	}


	public String getNombre() {
		if (nombreCompleto == null || nombreCompleto.isBlank()) return "";
		String[] parts = nombreCompleto.trim().split("\\s+", 2);
		return parts[0];
	}

	public String getApellido() {
		if (nombreCompleto == null || nombreCompleto.isBlank()) return "";
		String[] parts = nombreCompleto.trim().split("\\s+", 2);
		return parts.length > 1 ? parts[1] : "";
	}

	public Usuario getUsuario() {
		return usuario;
	}




	public void setUsuario(Usuario usuario) {
		this.usuario = usuario;
	}




}
