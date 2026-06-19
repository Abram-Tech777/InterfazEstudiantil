package com.colegio.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.colegio.entity.Asistencia;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AsistenciaRepository extends JpaRepository<Asistencia, Integer> {
    List<Asistencia> findByAlumno_IdAlumnoOrderByFechaDesc(int idAlumno);
    Optional<Asistencia> findByAlumno_IdAlumnoAndHorario_IdHorarioAndFecha(int idAlumno, int idHorario, LocalDate fecha);
    List<Asistencia> findByHorario_IdHorarioAndFecha(int idHorario, LocalDate fecha);
}
