package com.colegio.service.impl;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;

import com.colegio.entity.Aula;
import com.colegio.entity.Alumno;
import com.colegio.repository.AulaRepository;
import com.colegio.repository.AlumnoRepository;

@Service
@Transactional
public class AulaAlumnoServiceImpl implements AulaAlumnoService {

    private final AulaRepository aulaRepository;
    private final AlumnoRepository alumnoRepository;

    public AulaAlumnoServiceImpl(AulaRepository aulaRepository, AlumnoRepository alumnoRepository) {
        this.aulaRepository = aulaRepository;
        this.alumnoRepository = alumnoRepository;
    }

    @Override
    public void asignarAlumnosAula(int idAula, List<Integer> idsAlumnos) {
        Aula aula = aulaRepository.findById(idAula)
                .orElseThrow(() -> new EntityNotFoundException("Aula no encontrada"));

        if (idsAlumnos == null) idsAlumnos = List.of();
        if (idsAlumnos.size() > aula.getCapacidad()) {
            throw new IllegalArgumentException("La cantidad de alumnos seleccionados excede la capacidad del aula.");
        }

        List<Alumno> actuales = alumnoRepository.findByAula_IdAula(idAula);

        for (Alumno a : actuales) {
            if (!idsAlumnos.contains(a.getIdAlumno())) {
                a.setAula(null);
                alumnoRepository.save(a);
            }
        }

        for (Integer idAlumno : idsAlumnos) {
            Alumno alumno = alumnoRepository.findById(idAlumno)
                    .orElseThrow(() -> new EntityNotFoundException("Alumno no encontrado: " + idAlumno));
            alumno.setAula(aula);
            alumnoRepository.save(alumno);
        }
    }
}
