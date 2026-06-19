package com.colegio.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.colegio.entity.Curso;
import java.util.Optional;

public interface CursoRepository extends JpaRepository<Curso, Integer> {
    Optional<Curso> findByNombreCurso(String nombreCurso);
}