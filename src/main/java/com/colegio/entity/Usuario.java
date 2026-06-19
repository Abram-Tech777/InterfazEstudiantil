package com.colegio.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "usuario")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private int idUsuario;

    @Column(name = "nombre_completo",nullable = false,length = 50)
    private String nombreCompleto;
    
    @Column(name = "cod_usuario", nullable = false, length = 50, unique = true)
    private String codUsuario; 

    @Column(nullable = false,length = 255)
    private String password;

    @Column(nullable = false,length = 30)
    private String rol;
    
	@Column(nullable = false, length = 15)
	private String estado = "ACTIVO";



	public Usuario() {
	}


	public Usuario(int idUsuario, String nombreCompleto, String codUsuario, String password, String rol,
			String estado) {
		super();
		this.idUsuario = idUsuario;
		this.nombreCompleto = nombreCompleto;
		this.codUsuario = codUsuario;
		this.password = password;
		this.rol = rol;
		this.estado = estado;
	}


	public int getIdUsuario() {
		return idUsuario;
	}


	public void setIdUsuario(int idUsuario) {
		this.idUsuario = idUsuario;
	}


	public String getNombreCompleto() {
		return nombreCompleto;
	}


	public void setNombreCompleto(String nombreCompleto) {
		this.nombreCompleto = nombreCompleto;
	}


	public String getCodUsuario() {
		return codUsuario;
	}


	public void setCodUsuario(String codUsuario) {
		this.codUsuario = codUsuario;
	}


	public String getPassword() {
		return password;
	}


	public void setPassword(String password) {
		this.password = password;
	}


	public String getRol() {
		return rol;
	}


	public void setRol(String rol) {
		this.rol = rol;
	}


	public String getEstado() {
		return estado;
	}


	public void setEstado(String estado) {
		this.estado = estado;
	}

    // correo eliminado: getters/setters removidos

}
