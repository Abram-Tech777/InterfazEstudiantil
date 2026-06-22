package com.colegio.repository;

import java.time.LocalTime;
import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.colegio.entity.Horario;

public interface HorarioRepository extends JpaRepository<Horario, Integer>{

	
	@Query("SELECT h FROM Horario h WHERE h.aula.idAula = :idAula AND h.diaSemana = :dia AND h.horaInicio < :fin AND h.horaFin > :inicio")
    List<Horario> buscarChoqueAula(@Param("idAula") int idAula, @Param("dia") String dia, @Param("inicio") LocalTime inicio, @Param("fin") LocalTime fin);

    @Query("SELECT h FROM Horario h WHERE h.docente.idDocente = :idDocente AND h.diaSemana = :dia AND h.horaInicio < :fin AND h.horaFin > :inicio")
    List<Horario> buscarChoqueDocente(@Param("idDocente") int idDocente, @Param("dia") String dia, @Param("inicio") LocalTime inicio, @Param("fin") LocalTime fin);

    @Query("SELECT h FROM Horario h ORDER BY h.diaSemana ASC, h.horaInicio ASC")
    List<Horario> listarTodosOrdenados();

    @Query("SELECT h FROM Horario h WHERE h.docente.idDocente = :idDocente AND h.aula.idAula IN :aulasIds ORDER BY h.diaSemana ASC, h.horaInicio ASC")
    List<Horario> findByDocenteAndAulasAsignadas(@Param("idDocente") int idDocente, @Param("aulasIds") List<Integer> aulasIds);

    @Query("SELECT h FROM Horario h WHERE h.docente.idDocente = :idDocente ORDER BY h.diaSemana ASC, h.horaInicio ASC")
    List<Horario> findByIdDocente(@Param("idDocente") int idDocente);

    @Query("SELECT h FROM Horario h WHERE h.aula.idAula = :idAula ORDER BY h.horaInicio ASC, h.diaSemana ASC")
    List<Horario> findByAula(@Param("idAula") int idAula);

    @Query("SELECT h FROM Horario h WHERE h.aula.idAula = :idAula AND h.activo = true AND (h.fechaInicio IS NULL OR h.fechaInicio <= :fecha) AND (h.fechaFin IS NULL OR h.fechaFin >= :fecha) ORDER BY h.horaInicio ASC, h.diaSemana ASC")
    List<Horario> findHorariosActivos(@Param("idAula") int idAula, @Param("fecha") LocalDate fecha);
}
