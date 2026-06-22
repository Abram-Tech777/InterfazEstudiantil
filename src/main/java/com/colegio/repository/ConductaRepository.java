package com.colegio.repository;

import com.colegio.entity.Conducta; // <--- ESTO IMPORTA TU ENTIDAD
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ConductaRepository extends JpaRepository<Conducta, Integer> {
    List<Conducta> findByAlumno_IdAlumno(Integer idAlumno);
}