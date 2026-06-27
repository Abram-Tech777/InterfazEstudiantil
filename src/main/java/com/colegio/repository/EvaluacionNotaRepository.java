package com.colegio.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.colegio.entity.EvaluacionNota;
import java.util.List;
import java.util.Optional;

@Repository
public interface EvaluacionNotaRepository extends JpaRepository<EvaluacionNota, Integer> {
    List<EvaluacionNota> findByAlumno_IdAlumnoOrderByFechaRegistroDesc(int idAlumno);
    List<EvaluacionNota> findByAlumno_IdAlumnoAndCurso_IdCurso(int idAlumno, int idCurso);
    List<EvaluacionNota> findByAlumno_IdAlumnoAndCurso_IdCursoOrderByBimestreAsc(int idAlumno, int idCurso);
    List<EvaluacionNota> findByAlumno_IdAlumnoAndCurso_IdCursoAndBimestre(int idAlumno, int idCurso, int bimestre);
    List<EvaluacionNota> findByDocente_IdDocenteAndCurso_IdCursoAndBimestre(int idDocente, int idCurso, int bimestre);
    Optional<EvaluacionNota> findByAlumno_IdAlumnoAndCurso_IdCursoAndDocente_IdDocenteAndBimestre(
        int idAlumno, int idCurso, int idDocente, int bimestre);

    @Query("SELECT DISTINCT en.curso FROM EvaluacionNota en WHERE en.alumno.idAlumno = :idAlumno ORDER BY en.curso.nombreCurso")
    List<com.colegio.entity.Curso> findCursosByAlumnoId(@Param("idAlumno") int idAlumno);
}
