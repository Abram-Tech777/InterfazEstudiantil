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

        List<Integer> activeSelectedIds = idsAlumnos.stream().filter(id -> {
            Alumno a = alumnoRepository.findById(id).orElse(null);
            return a != null && (a.getEstado() == null || "ACTIVO".equalsIgnoreCase(a.getEstado()));
        }).collect(Collectors.toList());

        List<Alumno> actuales = alumnoRepository.findByAula_IdAula(idAula);

        long inactivosAsignados = actuales.stream()
                .filter(a -> a.getEstado() != null && !"ACTIVO".equalsIgnoreCase(a.getEstado()))
                .count();

        if (activeSelectedIds.size() + inactivosAsignados > aula.getCapacidad()) {
            throw new IllegalArgumentException("La cantidad de alumnos seleccionados excede la capacidad del aula.");
        }

        for (Alumno a : actuales) {
            if (!activeSelectedIds.contains(a.getIdAlumno())) {
                if (a.getEstado() == null || "ACTIVO".equalsIgnoreCase(a.getEstado())) {
                    a.setAula(null);
                    alumnoRepository.save(a);
                }
            }
        }

        for (Integer idAlumno : activeSelectedIds) {
            Alumno alumno = alumnoRepository.findById(idAlumno)
                    .orElseThrow(() -> new EntityNotFoundException("Alumno no encontrado: " + idAlumno));
            alumno.setAula(aula);
            alumnoRepository.save(alumno);
        }
    }
}
