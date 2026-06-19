package com.colegio.controller;

import java.util.List;
import java.util.Optional;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.colegio.entity.Alumno;
import com.colegio.entity.Asistencia;
import com.colegio.entity.EvaluacionNota;
import com.colegio.entity.Horario;
import com.colegio.entity.Usuario;
import com.colegio.repository.AlumnoRepository;
import com.colegio.repository.AsistenciaRepository;
import com.colegio.repository.EvaluacionNotaRepository;
import com.colegio.service.impl.HorarioService;

@Controller
public class EstudianteController {

    private final AlumnoRepository alumnoRepository;
    private final HorarioService horarioService;
    private final AsistenciaRepository asistenciaRepository;
    private final EvaluacionNotaRepository evaluacionNotaRepository;

    public EstudianteController(AlumnoRepository alumnoRepository, 
                                HorarioService horarioService,
                                AsistenciaRepository asistenciaRepository,
                                EvaluacionNotaRepository evaluacionNotaRepository) {
        this.alumnoRepository = alumnoRepository;
        this.horarioService = horarioService;
        this.asistenciaRepository = asistenciaRepository;
        this.evaluacionNotaRepository = evaluacionNotaRepository;
    }

    @GetMapping("/estudiante/horario")
    public String verHorario(HttpSession session, Model model) {
        Usuario u = (Usuario) session.getAttribute("usuarioLogueado");
        if (u == null || !"ESTUDIANTE".equalsIgnoreCase(u.getRol())) return "redirect:/login";
        Optional<Alumno> opt = alumnoRepository.findByUsuario_IdUsuario(u.getIdUsuario());
        if (opt.isEmpty()) {
            model.addAttribute("horarios", List.of());
            return "estudiante/horario";
        }
        Alumno alumno = opt.get();
        if (alumno.getAula() == null) {
            model.addAttribute("horarios", List.of());
            return "estudiante/horario";
        }
        List<Horario> todos = horarioService.findAll();
        List<Horario> horarios = todos.stream().filter(h -> h.getAula() != null && h.getAula().getIdAula() == alumno.getAula().getIdAula()).toList();
        model.addAttribute("horarios", horarios);
        return "estudiante/horario";
    }

    @GetMapping("/estudiante/notas")
    public String verNotas(HttpSession session, Model model) {
        Usuario u = (Usuario) session.getAttribute("usuarioLogueado");
        if (u == null || !"ESTUDIANTE".equalsIgnoreCase(u.getRol())) return "redirect:/login";
        
        Optional<Alumno> opt = alumnoRepository.findByUsuario_IdUsuario(u.getIdUsuario());
        if (opt.isEmpty()) {
            model.addAttribute("notas", List.of());
            return "estudiante/notas";
        }
        
        Alumno alumno = opt.get();
        List<EvaluacionNota> notas = evaluacionNotaRepository.findByAlumno_IdAlumnoOrderByFechaRegistroDesc(alumno.getIdAlumno());
        model.addAttribute("notas", notas);
        return "estudiante/notas";
    }

    @GetMapping("/estudiante/asistencia")
    public String verAsistencia(HttpSession session, Model model) {
        Usuario u = (Usuario) session.getAttribute("usuarioLogueado");
        if (u == null || !"ESTUDIANTE".equalsIgnoreCase(u.getRol())) return "redirect:/login";
        
        Optional<Alumno> opt = alumnoRepository.findByUsuario_IdUsuario(u.getIdUsuario());
        if (opt.isEmpty()) {
            model.addAttribute("asistencias", List.of());
            return "estudiante/asistencia";
        }
        
        Alumno alumno = opt.get();
        List<Asistencia> asistencias = asistenciaRepository.findByAlumno_IdAlumnoOrderByFechaDesc(alumno.getIdAlumno());
        model.addAttribute("asistencias", asistencias);
        return "estudiante/asistencia";
    }
}

