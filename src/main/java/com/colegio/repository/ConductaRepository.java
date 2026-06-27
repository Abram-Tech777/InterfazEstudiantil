package com.colegio.repository;

import com.colegio.entity.Conducta;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ConductaRepository extends JpaRepository<Conducta, Integer> {
    List<Conducta> findByAlumno_IdAlumno(Integer idAlumno);
    List<Conducta> findByAlumno_IdAlumnoAndBimestre(Integer idAlumno, Integer bimestre);
    long countByAlumno_IdAlumnoAndTipo(Integer idAlumno, String tipo);
    long countByAlumno_IdAlumnoAndTipoAndBimestre(Integer idAlumno, String tipo, Integer bimestre);
    List<Conducta> findByDocente_IdDocenteOrderByFechaRegistroDesc(Integer idDocente);
}