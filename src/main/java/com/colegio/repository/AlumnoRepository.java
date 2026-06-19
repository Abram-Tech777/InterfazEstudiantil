package com.colegio.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.colegio.entity.Alumno;

public interface AlumnoRepository extends JpaRepository<Alumno, Integer> {

    Optional<Alumno> findByUsuario_IdUsuario(int idUsuario);

    List<Alumno> findByAula_IdAula(int idAula);
}
