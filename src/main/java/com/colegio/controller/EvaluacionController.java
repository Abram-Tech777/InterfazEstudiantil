package com.colegio.controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.colegio.entity.Alumno;
import com.colegio.entity.Docente;
import com.colegio.entity.EvaluacionNota;
import com.colegio.entity.Horario;
import com.colegio.entity.Usuario;
import com.colegio.repository.AlumnoRepository;
import com.colegio.repository.DocenteRepository;
import com.colegio.repository.EvaluacionNotaRepository;
import com.colegio.service.impl.AulaDocenteService;
import com.colegio.service.impl.HorarioService;

@Controller
@RequestMapping("/docente/evaluaciones")
public class EvaluacionController {

    private final DocenteRepository docenteRepository;
    private final AlumnoRepository alumnoRepository;
    private final HorarioService horarioService;
    private final AulaDocenteService aulaDocenteService;
    private final EvaluacionNotaRepository evaluacionNotaRepository;

    public EvaluacionController(DocenteRepository docenteRepository,
                                AlumnoRepository alumnoRepository,
                                HorarioService horarioService,
                                AulaDocenteService aulaDocenteService,
                                EvaluacionNotaRepository evaluacionNotaRepository) {
        this.docenteRepository = docenteRepository;
        this.alumnoRepository = alumnoRepository;
        this.horarioService = horarioService;
        this.aulaDocenteService = aulaDocenteService;
        this.evaluacionNotaRepository = evaluacionNotaRepository;
    }

    /**
     * GET /docente/evaluaciones - Lista horarios del docente para evaluar
     */
    @GetMapping
    public String listarEvaluaciones(HttpSession session, Model model) {
        Usuario u = (Usuario) session.getAttribute("usuarioLogueado");
        if (u == null || !"DOCENTE".equalsIgnoreCase(u.getRol())) {
            return "redirect:/login";
        }

        Optional<Docente> docenteOpt = docenteRepository.findByUsuario_IdUsuario(u.getIdUsuario());
        if (docenteOpt.isEmpty()) {
            model.addAttribute("horarios", List.of());
            return "docente/evaluaciones";
        }

        Docente d = docenteOpt.get();
        List<Integer> aulasAsignadas = aulaDocenteService.listarAulasDelDocente(d.getIdDocente());
        List<Horario> horarios = horarioService.findHorarioRepository()
            .findByDocenteAndAulasAsignadas(d.getIdDocente(), aulasAsignadas);

        model.addAttribute("horarios", horarios);
        model.addAttribute("docente", d);
        return "docente/evaluaciones";
    }

    /**
     * GET /docente/evaluaciones/abrir/{id}?bimestre=N - Abre formulario de evaluación
     */
    @GetMapping("/abrir/{id}")
    public String abrirEvaluacion(@PathVariable("id") int idHorario,
                                  @RequestParam("bimestre") int bimestre,
                                  HttpSession session,
                                  Model model) {
        Usuario u = (Usuario) session.getAttribute("usuarioLogueado");
        if (u == null || !"DOCENTE".equalsIgnoreCase(u.getRol())) {
            return "redirect:/login";
        }

        Optional<Docente> docenteOpt = docenteRepository.findByUsuario_IdUsuario(u.getIdUsuario());
        if (docenteOpt.isEmpty()) {
            return "redirect:/docente/evaluaciones";
        }

        Docente d = docenteOpt.get();
        Horario h = horarioService.getHorarioById(idHorario);
        
        if (h == null || h.getAula() == null) {
            return "redirect:/docente/evaluaciones";
        }

        List<Alumno> alumnos = alumnoRepository.findByAula_IdAula(h.getAula().getIdAula());

        // Prellenar notas existentes en un mapa
        Map<Integer, EvaluacionNota> notasMap = new HashMap<>();
        for (Alumno a : alumnos) {
            Optional<EvaluacionNota> notaOpt = evaluacionNotaRepository
                .findByAlumno_IdAlumnoAndCurso_IdCursoAndDocente_IdDocenteAndBimestre(
                    a.getIdAlumno(), h.getCurso().getIdCurso(), d.getIdDocente(), bimestre);
            if (notaOpt.isPresent()) {
                notasMap.put(a.getIdAlumno(), notaOpt.get());
            }
        }

        model.addAttribute("horario", h);
        model.addAttribute("docente", d);
        model.addAttribute("alumnos", alumnos);
        model.addAttribute("bimestre", bimestre);
        model.addAttribute("notasMap", notasMap);

        return "docente/evaluacion_detalle";
    }

    /**
     * POST /docente/evaluaciones/guardar - Guarda/actualiza notas
     */
    @PostMapping("/guardar")
    public String guardarEvaluacion(@RequestParam("idHorario") int idHorario,
                                    @RequestParam("bimestre") int bimestre,
                                    @RequestParam Map<String, String> allParams,
                                    HttpSession session,
                                    RedirectAttributes redirectAttrs) {
        Usuario u = (Usuario) session.getAttribute("usuarioLogueado");
        if (u == null || !"DOCENTE".equalsIgnoreCase(u.getRol())) {
            return "redirect:/login";
        }

        Optional<Docente> docenteOpt = docenteRepository.findByUsuario_IdUsuario(u.getIdUsuario());
        if (docenteOpt.isEmpty()) {
            redirectAttrs.addFlashAttribute("mensajeError", "Docente no encontrado.");
            return "redirect:/docente/evaluaciones";
        }

        try {
            Docente d = docenteOpt.get();
            Horario h = horarioService.getHorarioById(idHorario);

            if (h == null || h.getCurso() == null) {
                redirectAttrs.addFlashAttribute("mensajeError", "Horario o curso inválido.");
                return "redirect:/docente/evaluaciones";
            }

            List<Alumno> alumnos = alumnoRepository.findByAula_IdAula(h.getAula().getIdAula());

            for (Alumno a : alumnos) {
                String notaStr = allParams.get("nota_" + a.getIdAlumno());

                if (notaStr != null && !notaStr.isBlank()) {
                    try {
                        BigDecimal nota = new BigDecimal(notaStr.trim());

                        // Validar rango 0-20
                        if (nota.compareTo(BigDecimal.ZERO) < 0 || nota.compareTo(new BigDecimal("20")) > 0) {
                            redirectAttrs.addFlashAttribute("mensajeError",
                                "Nota para " + a.getNombreCompleto() + " debe estar entre 0 y 20.");
                            return "redirect:/docente/evaluaciones/abrir/" + idHorario + "?bimestre=" + bimestre;
                        }

                        Optional<EvaluacionNota> existingOpt = evaluacionNotaRepository
                            .findByAlumno_IdAlumnoAndCurso_IdCursoAndDocente_IdDocenteAndBimestre(
                                a.getIdAlumno(), h.getCurso().getIdCurso(), d.getIdDocente(), bimestre);

                        EvaluacionNota evaluacion;
                        if (existingOpt.isPresent()) {
                            evaluacion = existingOpt.get();
                            // Validar que no haya pasado >3 días desde fecha_registro
                            long diasTranscurridos = ChronoUnit.DAYS.between(
                                evaluacion.getFechaRegistro(), LocalDate.now());
                            if (diasTranscurridos > 3) {
                                redirectAttrs.addFlashAttribute("mensajeError",
                                    "No puede editar la nota de " + a.getNombreCompleto()
                                    + " (más de 3 días desde el registro).");
                                return "redirect:/docente/evaluaciones/abrir/" + idHorario + "?bimestre=" + bimestre;
                            }
                            evaluacion.setNota(nota);
                        } else {
                            evaluacion = new EvaluacionNota();
                            evaluacion.setAlumno(a);
                            evaluacion.setCurso(h.getCurso());
                            evaluacion.setDocente(d);
                            evaluacion.setBimestre(bimestre);
                            evaluacion.setNota(nota);
                            evaluacion.setFechaRegistro(LocalDate.now());
                        }

                        evaluacionNotaRepository.save(evaluacion);
                    } catch (NumberFormatException e) {
                        redirectAttrs.addFlashAttribute("mensajeError",
                            "Formato de nota inválido para " + a.getNombreCompleto() + ".");
                        return "redirect:/docente/evaluaciones/abrir/" + idHorario + "?bimestre=" + bimestre;
                    }
                }
            }

            redirectAttrs.addFlashAttribute("mensajeExito",
                "Notas del bimestre " + bimestre + " registradas correctamente.");
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("mensajeError",
                "Error al guardar evaluaciones: " + e.getMessage());
        }

        return "redirect:/docente/evaluaciones";
    }
}
