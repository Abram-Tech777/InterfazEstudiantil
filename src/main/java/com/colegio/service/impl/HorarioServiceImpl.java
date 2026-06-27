package com.colegio.service.impl;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.colegio.entity.AulaDocente;
import com.colegio.entity.Horario;
import com.colegio.repository.AulaDocenteRepository;
import com.colegio.repository.HorarioRepository;
import com.colegio.util.JornadaConfig;


@Service
@Transactional
public class HorarioServiceImpl implements HorarioService {

    private static final Logger logger = LoggerFactory.getLogger(HorarioServiceImpl.class);

    private final HorarioRepository horarioRepository;
    private final AulaDocenteRepository aulaDocenteRepository;
    private final JornadaConfig jornadaConfig;

    public HorarioServiceImpl(HorarioRepository horarioRepository, AulaDocenteRepository aulaDocenteRepository, JornadaConfig jornadaConfig) {
        this.horarioRepository = horarioRepository;
        this.aulaDocenteRepository = aulaDocenteRepository;
        this.jornadaConfig = jornadaConfig;
    }

    @Override
    public void validarDuracionClase(Horario horario) {
        if (horario.getHoraInicio() != null && horario.getHoraFin() != null) {
            long diff = ChronoUnit.MINUTES.between(horario.getHoraInicio(), horario.getHoraFin());
            if (diff < 45 || diff > 48) {
                throw new IllegalArgumentException("La duración de la clase debe ser entre 45 y 48 minutos (actual: " + diff + " min).");
            }
        }
    }

    @Override
    public void validarNoChoqueConRecreo(Horario horario) {
        if (horario.getHoraInicio() == null || horario.getHoraFin() == null) return;
        LocalTime recreoInicio = jornadaConfig.getRecreoInicio();
        LocalTime recreoFin = recreoInicio.plusMinutes(jornadaConfig.getRecreoDuracion());
        if (horario.getHoraInicio().isBefore(recreoFin) && horario.getHoraFin().isAfter(recreoInicio)) {
            throw new IllegalArgumentException(
                "El horario no puede coincidir con el recreo (" +
                recreoInicio + " - " + recreoFin + ").");
        }
    }

    @Override
    public Horario crearHorario(Horario horario) {
        if (horario.getHoraInicio() == null || horario.getHoraFin() == null || !horario.getHoraInicio().isBefore(horario.getHoraFin())) {
            throw new IllegalArgumentException("La hora de inicio debe ser anterior a la hora de fin.");
        }
        validarDuracionClase(horario);
        validarNoChoqueConRecreo(horario);
        if (horario.getAula() == null || horario.getAula().getIdAula() <= 0) {
            throw new IllegalArgumentException("Debe especificar un aula valida.");
        }
        if (horario.getDocente() == null || horario.getDocente().getIdDocente() <= 0) {
            throw new IllegalArgumentException("Debe especificar un docente valido.");
        }
        if (horario.getFechaInicio() == null) {
            horario.setFechaInicio(LocalDate.now());
        }

        var choqueAula = horarioRepository.buscarChoqueAula(horario.getAula().getIdAula(), horario.getDiaSemana(), horario.getHoraInicio(), horario.getHoraFin());
        if (!choqueAula.isEmpty()) {
            throw new IllegalArgumentException("Ya existe un horario para esa aula en ese dia y hora.");
        }
        var choqueDocente = horarioRepository.buscarChoqueDocente(horario.getDocente().getIdDocente(), horario.getDiaSemana(), horario.getHoraInicio(), horario.getHoraFin());
        if (!choqueDocente.isEmpty()) {
            throw new IllegalArgumentException("El docente ya tiene un horario asignado en ese dia y hora.");
        }

        Horario saved = horarioRepository.save(horario);

        if (aulaDocenteRepository.findByDocente_IdDocenteAndAula_IdAula(
                horario.getDocente().getIdDocente(), horario.getAula().getIdAula()).isEmpty()) {
            AulaDocente asignacion = new AulaDocente(horario.getDocente(), horario.getAula(), "DOCENTE");
            aulaDocenteRepository.save(asignacion);
        }

        return saved;
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
        validarDuracionClase(horario);
        validarNoChoqueConRecreo(horario);

        if (horario.getAula() == null || horario.getAula().getIdAula() <= 0) {
            throw new IllegalArgumentException("Debe especificar un aula valida.");
        }
        if (horario.getDocente() == null || horario.getDocente().getIdDocente() <= 0) {
            throw new IllegalArgumentException("Debe especificar un docente valido.");
        }

        var choqueAula = horarioRepository.buscarChoqueAula(horario.getAula().getIdAula(), horario.getDiaSemana(), horario.getHoraInicio(), horario.getHoraFin());
        if (!choqueAula.isEmpty() && choqueAula.get(0).getIdHorario() != id) {
            throw new IllegalArgumentException("Otra entrada de horario ya usa esa aula en ese momento.");
        }

        var choqueDocente = horarioRepository.buscarChoqueDocente(horario.getDocente().getIdDocente(), horario.getDiaSemana(), horario.getHoraInicio(), horario.getHoraFin());
        if (!choqueDocente.isEmpty() && choqueDocente.get(0).getIdHorario() != id) {
            throw new IllegalArgumentException("El docente ya tiene otro horario en ese dia y hora.");
        }

        existing.setAula(horario.getAula());
        existing.setDocente(horario.getDocente());
        existing.setCurso(horario.getCurso());
        existing.setDiaSemana(horario.getDiaSemana());
        existing.setHoraInicio(horario.getHoraInicio());
        existing.setHoraFin(horario.getHoraFin());
        existing.setTipo(horario.getTipo());
        if (horario.getFechaInicio() != null) {
            existing.setFechaInicio(horario.getFechaInicio());
        }

        Horario updated = horarioRepository.save(existing);

        if (aulaDocenteRepository.findByDocente_IdDocenteAndAula_IdAula(
                horario.getDocente().getIdDocente(), horario.getAula().getIdAula()).isEmpty()) {
            AulaDocente asignacion = new AulaDocente(horario.getDocente(), horario.getAula(), "DOCENTE");
            aulaDocenteRepository.save(asignacion);
        }

        return updated;
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

    @Override
    @Transactional(readOnly = true)
    public List<Horario> findHorariosActivos(int idAula, LocalDate fecha) {
        return horarioRepository.findHorariosActivos(idAula, fecha);
    }

    @Override
    @Scheduled(cron = "0 0 3 * * ?")
    public void limpiarRefuerzoExpirados() {
        LocalDate fechaLimite = LocalDate.now().minusDays(7);
        logger.info("Limpiando horarios REFUERZO anteriores a {}", fechaLimite);
        List<Horario> expirados = horarioRepository.findRefuerzoExpirados(fechaLimite);
        for (Horario h : expirados) {
            logger.info("Eliminando REFUERZO expirado: ID={}, FechaInicio={}, Curso={}", h.getIdHorario(), h.getFechaInicio(), h.getCurso().getNombreCurso());
            horarioRepository.delete(h);
        }
        if (!expirados.isEmpty()) {
            logger.info("Se eliminaron {} horarios REFUERZO expirados", expirados.size());
        }
    }
}