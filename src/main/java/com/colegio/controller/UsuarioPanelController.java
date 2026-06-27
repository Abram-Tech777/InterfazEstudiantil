package com.colegio.controller;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.colegio.service.impl.AulaDocenteService;

import com.colegio.entity.*;
import com.colegio.repository.*;
import com.colegio.service.impl.HorarioService;

@Controller
public class UsuarioPanelController {

    @Autowired
    private AlumnoRepository alumnoRepository;
    @Autowired
    private DocenteRepository docenteRepository;
    @Autowired
    private HorarioService horarioService;
    @Autowired
    private AulaDocenteService aulaDocenteService;
    @Autowired
    private AsistenciaRepository asistenciaRepository;
    @Autowired
    private AulaRepository aulaRepository;
    @Autowired
    private CursoRepository cursoRepository;

    @GetMapping("/docente/asistencia")
    public String controlAsistenciaDocente(HttpSession session, Model model,
                                           @RequestParam(required = false) Integer idAula,
                                           @RequestParam(required = false) Integer idCurso,
                                           @RequestParam(required = false) String diaSemana) {
        Usuario u = (Usuario) session.getAttribute("usuarioLogueado");
        if (u == null || !"DOCENTE".equalsIgnoreCase(u.getRol())) return "redirect:/login";
        var docenteOpt = docenteRepository.findByUsuario_IdUsuario(u.getIdUsuario());
        if (docenteOpt.isEmpty()) {
            model.addAttribute("horarios", List.of());
            return "docente/asistencia";
        }
        Docente d = docenteOpt.get();

        // Obtener horarios del docente
        List<Horario> horarios;
        List<Integer> aulasAsignadas = aulaDocenteService.listarAulasDelDocente(d.getIdDocente());

        if (aulasAsignadas != null && !aulasAsignadas.isEmpty()) {
            horarios = horarioService.findHorarioRepository().findByDocenteAndAulasAsignadas(d.getIdDocente(), aulasAsignadas);
        } else {
            horarios = horarioService.findHorarioRepository().findByIdDocente(d.getIdDocente());
        }

        // Aulas disponibles para el docente
        List<Aula> aulas;
        if (aulasAsignadas != null && !aulasAsignadas.isEmpty()) {
            aulas = aulaRepository.findAllById(aulasAsignadas);
        } else {
            aulas = horarios.stream()
                .map(Horario::getAula)
                .filter(a -> a != null)
                .distinct()
                .toList();
        }

        // Cursos disponibles en los horarios del docente
        List<Curso> cursos = horarios.stream()
            .map(Horario::getCurso)
            .filter(c -> c != null)
            .distinct()
            .toList();

        // Días disponibles
        List<String> dias = Arrays.asList("LUNES", "MARTES", "MIERCOLES", "JUEVES", "VIERNES", "SABADO");

        // Aplicar filtros
        List<Horario> horariosFiltrados = horarios;
        if (idAula != null && idAula > 0) {
            horariosFiltrados = horariosFiltrados.stream()
                .filter(h -> h.getAula() != null && h.getAula().getIdAula() == idAula)
                .toList();
        }
        if (idCurso != null && idCurso > 0) {
            horariosFiltrados = horariosFiltrados.stream()
                .filter(h -> h.getCurso() != null && h.getCurso().getIdCurso() == idCurso)
                .toList();
        }
        if (diaSemana != null && !diaSemana.isEmpty()) {
            String diaFiltro = diaSemana;
            horariosFiltrados = horariosFiltrados.stream()
                .filter(h -> h.getDiaSemana() != null && h.getDiaSemana().equalsIgnoreCase(diaFiltro))
                .toList();
        }

        model.addAttribute("horarios", horariosFiltrados);
        model.addAttribute("aulas", aulas);
        model.addAttribute("cursos", cursos);
        model.addAttribute("dias", dias);
        model.addAttribute("idAulaSel", idAula != null ? idAula : 0);
        model.addAttribute("idCursoSel", idCurso != null ? idCurso : 0);
        model.addAttribute("diaSel", diaSemana != null ? diaSemana : "");
        return "docente/asistencia";
    }

    @GetMapping("/docente/asistencia/abrir/{id}")
    public String abrirAsistencia(@PathVariable("id") int idHorario, HttpSession session, Model model) {
        Usuario u = (Usuario) session.getAttribute("usuarioLogueado");
        if (u == null || !"DOCENTE".equalsIgnoreCase(u.getRol())) return "redirect:/login";
        Horario h = horarioService.getHorarioById(idHorario);
        List<Alumno> alumnos = List.of();
        if (h.getAula() != null) {
            alumnos = alumnoRepository.findByAula_IdAula(h.getAula().getIdAula());
        }

        // Cargar asistencias ya registradas hoy para pre-seleccionar los radio buttons
        java.util.Map<Integer, String> estadosGuardados = new java.util.HashMap<>();
        LocalDate fecha = LocalDate.now();
        if (h != null) {
            for (Alumno a : alumnos) {
                Optional<Asistencia> existente = asistenciaRepository
                    .findByAlumno_IdAlumnoAndHorario_IdHorarioAndFecha(a.getIdAlumno(), idHorario, fecha);
                if (existente.isPresent()) {
                    estadosGuardados.put(a.getIdAlumno(), existente.get().getEstado());
                }
            }
        }

        model.addAttribute("horario", h);
        model.addAttribute("alumnos", alumnos);
        model.addAttribute("estadosGuardados", estadosGuardados);
        return "docente/asistencia_detalle";
    }

    @PostMapping("/docente/asistencia/guardar")
    public String guardarAsistencia(@RequestParam("idHorario") int idHorario,
                                    HttpServletRequest request,
                                    HttpSession session,
                                    RedirectAttributes redirectAttrs) {
        Usuario u = (Usuario) session.getAttribute("usuarioLogueado");
        if (u == null || !"DOCENTE".equalsIgnoreCase(u.getRol())) return "redirect:/login";

        try {
            Horario h = horarioService.getHorarioById(idHorario);
            if (h == null || h.getAula() == null) {
                redirectAttrs.addFlashAttribute("mensajeError", "Horario inválido o sin aula.");
                return "redirect:/docente/asistencia";
            }

            List<Alumno> alumnos = alumnoRepository.findByAula_IdAula(h.getAula().getIdAula());
            LocalDate fecha = LocalDate.now();
            Curso curso = h.getCurso();
            if (curso == null) {
                redirectAttrs.addFlashAttribute("mensajeError", "El horario no tiene curso asignado.");
                return "redirect:/docente/asistencia";
            }

            for (Alumno a : alumnos) {
                String estado = request.getParameter("estado_" + a.getIdAlumno());
                
                if (estado == null || estado.isBlank()) {
                    estado = "AUSENCIA";
                }

                Optional<Asistencia> existing = asistenciaRepository
                    .findByAlumno_IdAlumnoAndHorario_IdHorarioAndFecha(a.getIdAlumno(), idHorario, fecha);

                Asistencia asistencia;
                if (existing.isPresent()) {
                    asistencia = existing.get();
                } else {
                    asistencia = new Asistencia();
                    asistencia.setAlumno(a);
                    asistencia.setHorario(h);
                    asistencia.setCurso(curso);
                    asistencia.setFecha(fecha);
                }
                
                asistencia.setEstado(estado);
                asistenciaRepository.save(asistencia);
            }

            redirectAttrs.addFlashAttribute("mensajeExito", "Asistencias registradas correctamente.");
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("mensajeError", "Error al guardar asistencias: " + e.getMessage());
        }
        return "redirect:/docente/asistencia";
    }
}
