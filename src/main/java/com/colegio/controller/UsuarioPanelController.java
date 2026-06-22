package com.colegio.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

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

    @GetMapping("/docente/asistencia")
    public String controlAsistenciaDocente(HttpSession session, Model model) {
        Usuario u = (Usuario) session.getAttribute("usuarioLogueado");
        if (u == null || !"DOCENTE".equalsIgnoreCase(u.getRol())) return "redirect:/login";
        var docenteOpt = docenteRepository.findByUsuario_IdUsuario(u.getIdUsuario());
        if (docenteOpt.isEmpty()) {
            model.addAttribute("horarios", List.of());
            return "docente/asistencia";
        }
        Docente d = docenteOpt.get();
        
        // Obtener horarios del docente
        // Si hay aulas asignadas, filtrar por esas aulas; si no, traer todos los horarios del docente
        List<Horario> horarios;
        List<Integer> aulasAsignadas = aulaDocenteService.listarAulasDelDocente(d.getIdDocente());
        
        if (aulasAsignadas != null && !aulasAsignadas.isEmpty()) {
            horarios = horarioService.findHorarioRepository().findByDocenteAndAulasAsignadas(d.getIdDocente(), aulasAsignadas);
        } else {
            // Si no hay aulas asignadas en aula_docente, traer todos los horarios donde aparece como docente
            horarios = horarioService.findHorarioRepository().findByIdDocente(d.getIdDocente());
        }
        
        model.addAttribute("horarios", horarios);
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
        model.addAttribute("horario", h);
        model.addAttribute("alumnos", alumnos);
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
                String horaLlegadaStr = request.getParameter("horaLlegada_" + a.getIdAlumno());
                
                if (estado == null || estado.isBlank()) {
                    estado = "AUSENCIA"; // Por defecto, si no se marca algo, es ausencia
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
                
                // Si se proporciona hora de llegada, parsearla y determinar estado automáticamente
                if (horaLlegadaStr != null && !horaLlegadaStr.isBlank()) {
                    try {
                        asistencia.setHoraLlegada(java.time.LocalTime.parse(horaLlegadaStr));
                        asistencia.determinarEstado(); // Calcula automáticamente si fue presente, retardo o ausencia
                    } catch (java.time.format.DateTimeParseException e) {
                        // Si la hora no es válida, usar el estado manual
                        asistencia.setEstado(estado);
                    }
                } else {
                    // Si no hay hora de llegada, usar el estado manual
                    asistencia.setEstado(estado);
                }
                
                asistenciaRepository.save(asistencia);
            }

            redirectAttrs.addFlashAttribute("mensajeExito", "Asistencias registradas correctamente.");
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("mensajeError", "Error al guardar asistencias: " + e.getMessage());
        }
        return "redirect:/docente/asistencia";
    }
}
