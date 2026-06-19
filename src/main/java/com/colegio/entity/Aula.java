package com.colegio.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;


@Entity
@Table(
    name = "aula",
    uniqueConstraints = {@UniqueConstraint(name = "uk_aula_grado_seccion", columnNames = { "grado", "seccion" })
    }
)
public class Aula {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_aula")
    private int idAula;


    @Column(nullable = false, length = 20)
    private String grado;


    @Column(nullable = false,length = 5)
    private String seccion;

    
    @Column(nullable = false)
    private int capacidad;

    public Aula() {
    }

	public Aula(int idAula, String grado, String seccion, int capacidad) {
		super();
		this.idAula = idAula;
		this.grado = grado;
		this.seccion = seccion;
		this.capacidad = capacidad;
	}

	public int getIdAula() {
		return idAula;
	}

	public void setIdAula(int idAula) {
		this.idAula = idAula;
	}

	public String getGrado() {
		return grado;
	}

	public void setGrado(String grado) {
		this.grado = grado;
	}

	public String getSeccion() {
		return seccion;
	}

	public void setSeccion(String seccion) {
		this.seccion = seccion;
	}

	public int getCapacidad() {
		return capacidad;
	}

	public void setCapacidad(int capacidad) {
		this.capacidad = capacidad;
	}

	public String getNombre() {
		// Combina grado y sección en un "nombre" legible
		if (this.grado == null) return (this.seccion == null) ? "" : this.seccion;
		if (this.seccion == null || this.seccion.isBlank()) return this.grado;
		return this.grado + " " + this.seccion;
	}
    
}