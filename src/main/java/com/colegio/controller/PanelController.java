package com.colegio.controller;

import com.colegio.entity.*;
import com.colegio.repository.*;
import com.colegio.service.impl.AulaDocenteService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Controller
public class PanelController {

    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private AlumnoRepository alumnoRepository;
    @Autowired private DocenteRepository docenteRepository;
    @Autowired private AulaRepository aulaRepository;
    @Autowired private CursoRepository cursoRepository;
    @Autowired private HorarioRepository horarioRepository;
    @Autowired private ComunicadoRepository comunicadoRepository;
    @Autowired private AulaDocenteService aulaDocenteService;

    @GetMapping("/administrador/panel")
    public String administradorPanel(HttpSession session, Model model, RedirectAttributes redirectAttrs) {
        Usuario u = (Usuario) session.getAttribute("usuarioLogueado");
        if (u == null || !"ADMINISTRADOR".equalsIgnoreCase(u.getRol())) {
            redirectAttrs.addFlashAttribute("mensajeError", "No tienes permiso para acceder a esta pagina.");
            return "redirect:/login";
        }

        long totalUsuarios = usuarioRepository.count();
        long totalDocentes = docenteRepository.count();
        long totalAlumnos = alumnoRepository.count();
        long totalAulas = aulaRepository.count();
        long totalCursos = cursoRepository.count();
        long totalHorarios = horarioRepository.count();
        long totalComunicados = comunicadoRepository.count();

        model.addAttribute("totalUsuarios", totalUsuarios);
        model.addAttribute("totalDocentes", totalDocentes);
        model.addAttribute("totalAlumnos", totalAlumnos);
        model.addAttribute("totalAulas", totalAulas);
        model.addAttribute("totalCursos", totalCursos);
        model.addAttribute("totalHorarios", totalHorarios);
        model.addAttribute("totalComunicados", totalComunicados);

        List<Comunicado> ultimosComunicados = comunicadoRepository.findAll();
        if (ultimosComunicados.size() > 5) ultimosComunicados = ultimosComunicados.subList(0, 5);
        model.addAttribute("ultimosComunicados", ultimosComunicados);

        return "administrador/panel";
    }

    @GetMapping("/docente/panel")
    public String docentePanel(HttpSession session, Model model, RedirectAttributes redirectAttrs) {
        Usuario u = (Usuario) session.getAttribute("usuarioLogueado");
        if (u == null || !"DOCENTE".equalsIgnoreCase(u.getRol())) {
            redirectAttrs.addFlashAttribute("mensajeError", "No tienes permiso para acceder a esta pagina.");
            return "redirect:/login";
        }

        Optional<Docente> optDoc = docenteRepository.findByUsuario_IdUsuario(u.getIdUsuario());
        if (optDoc.isPresent()) {
            Docente d = optDoc.get();
            model.addAttribute("docente", d);

            List<Integer> aulasIds = aulaDocenteService.listarAulasDelDocente(d.getIdDocente());
            long totalAulasAsignadas = aulasIds != null ? aulasIds.size() : 0;
            long totalAlumnos = 0;
            if (aulasIds != null) {
                for (Integer id : aulasIds) {
                    List<Alumno> alumnos = alumnoRepository.findByAula_IdAula(id);
                    totalAlumnos += alumnos.size();
                }
            }
            model.addAttribute("totalAulas", totalAulasAsignadas);
            model.addAttribute("totalAlumnos", totalAlumnos);

            String diaHoy = LocalDate.now().getDayOfWeek().name();
            List<Horario> horariosDocente = horarioRepository.findByIdDocente(d.getIdDocente());
            long clasesHoy = horariosDocente.stream()
                .filter(h -> h.getDiaSemana() != null && h.getDiaSemana().toUpperCase().contains(diaHoy.substring(0, 3)))
                .count();
            model.addAttribute("clasesHoy", clasesHoy);
        }

        return "docente/panel";
    }
}