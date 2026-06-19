package com.colegio.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.colegio.entity.Docente;

public interface DocenteRepository extends JpaRepository<Docente, Integer> {
	Optional<Docente> findByUsuario_IdUsuario(int idUsuario);
}