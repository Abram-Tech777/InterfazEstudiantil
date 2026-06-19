package com.colegio.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.colegio.entity.EvaluacionNota;
import java.util.List;

@Repository
public interface EvaluacionNotaRepository extends JpaRepository<EvaluacionNota, Integer> {
    List<EvaluacionNota> findByAlumno_IdAlumnoOrderByFechaRegistroDesc(int idAlumno);
    List<EvaluacionNota> findByAlumno_IdAlumnoAndCurso_IdCurso(int idAlumno, int idCurso);
}
