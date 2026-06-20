package com.colegio.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.colegio.entity.EvaluacionNota;
import java.util.List;
import java.util.Optional;

@Repository
public interface EvaluacionNotaRepository extends JpaRepository<EvaluacionNota, Integer> {
    List<EvaluacionNota> findByAlumno_IdAlumnoOrderByFechaRegistroDesc(int idAlumno);
    List<EvaluacionNota> findByAlumno_IdAlumnoAndCurso_IdCurso(int idAlumno, int idCurso);
    Optional<EvaluacionNota> findByAlumno_IdAlumnoAndCurso_IdCursoAndDocente_IdDocenteAndBimestre(
        int idAlumno, int idCurso, int idDocente, int bimestre);
}
