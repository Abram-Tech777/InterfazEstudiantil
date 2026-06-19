package com.colegio.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.colegio.entity.AulaDocente;

public interface AulaDocenteRepository extends JpaRepository<AulaDocente, Integer> {

    @Query("SELECT ad.aula.idAula FROM AulaDocente ad WHERE ad.docente.idDocente = :idDocente AND ad.activo = true")
    List<Integer> findAulaIdsByDocenteId(@Param("idDocente") int idDocente);

    List<AulaDocente> findByDocente_IdDocenteAndActivoTrue(int idDocente);

    List<AulaDocente> findByAula_IdAulaAndActivoTrue(int idAula);

    Optional<AulaDocente> findByDocente_IdDocenteAndAula_IdAula(int idDocente, int idAula);

    List<AulaDocente> findByAula_IdAula(int idAula);
}
