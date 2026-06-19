package com.colegio.service.impl;

import java.util.List;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.colegio.entity.Horario;
import com.colegio.repository.HorarioRepository;


@Service
@Transactional
public class HorarioServiceImpl implements HorarioService {

    private final HorarioRepository horarioRepository;

    public HorarioServiceImpl(HorarioRepository horarioRepository) {
        this.horarioRepository = horarioRepository;
    }

    @Override
    public Horario crearHorario(Horario horario) {
        if (horario.getHoraInicio() == null || horario.getHoraFin() == null || !horario.getHoraInicio().isBefore(horario.getHoraFin())) {
            throw new IllegalArgumentException("La hora de inicio debe ser anterior a la hora de fin.");
        }
        var choqueAula = horarioRepository.buscarChoqueAula(horario.getAula().getIdAula(), horario.getDiaSemana(), horario.getHoraInicio(), horario.getHoraFin());
        if (!choqueAula.isEmpty()) {
            throw new IllegalArgumentException("Ya existe un horario para esa aula en ese día y hora.");
        }
        var choqueDocente = horarioRepository.buscarChoqueDocente(horario.getIdDocente(), horario.getDiaSemana(), horario.getHoraInicio(), horario.getHoraFin());
        if (!choqueDocente.isEmpty()) {
            throw new IllegalArgumentException("El docente ya tiene un horario asignado en ese día y hora.");
        }

        return horarioRepository.save(horario);
    }

    @Override
    @Transactional(readOnly = true)
    public Horario getHorarioById(int id) {
        return horarioRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Horario no encontrado con id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Horario> findAll() {
        return horarioRepository.listarTodosOrdenados();
    }

    @Override
    public Horario updateHorario(int id, Horario horario) {
        Horario existing = horarioRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Horario no encontrado con id: " + id));

        if (horario.getHoraInicio() == null || horario.getHoraFin() == null || !horario.getHoraInicio().isBefore(horario.getHoraFin())) {
            throw new IllegalArgumentException("La hora de inicio debe ser anterior a la hora de fin.");
        }


        var choqueAula = horarioRepository.buscarChoqueAula(horario.getAula().getIdAula(), horario.getDiaSemana(), horario.getHoraInicio(), horario.getHoraFin());
        if (!choqueAula.isEmpty() && choqueAula.get(0).getIdHorario() != id) {
            throw new IllegalArgumentException("Otra entrada de horario ya usa esa aula en ese momento.");
        }

        var choqueDocente = horarioRepository.buscarChoqueDocente(horario.getIdDocente(), horario.getDiaSemana(), horario.getHoraInicio(), horario.getHoraFin());
        if (!choqueDocente.isEmpty() && choqueDocente.get(0).getIdHorario() != id) {
            throw new IllegalArgumentException("El docente ya tiene otro horario en ese día y hora.");
        }

        existing.setAula(horario.getAula());
        existing.setIdDocente(horario.getIdDocente());
        existing.setIdCurso(horario.getIdCurso());
        existing.setDiaSemana(horario.getDiaSemana());
        existing.setHoraInicio(horario.getHoraInicio());
        existing.setHoraFin(horario.getHoraFin());

        return horarioRepository.save(existing);
    }

    @Override
    public void deleteHorario(int id) {
        if (!horarioRepository.existsById(id)) {
            throw new EntityNotFoundException("Horario no encontrado con id: " + id);
        }
        horarioRepository.deleteById(id);
    }

    @Override
    public HorarioRepository findHorarioRepository() {
        return horarioRepository;
    }
}