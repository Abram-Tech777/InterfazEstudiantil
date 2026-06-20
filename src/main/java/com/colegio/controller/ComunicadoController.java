package com.colegio.controller;

import java.util.*;
import java.util.stream.Collectors;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.colegio.entity.*;
import com.colegio.repository.*;
import com.colegio.service.impl.ComunicadoService;

@Controller
@RequestMapping("/gestioncomunicados")
public class ComunicadoController {

    @Autowired
    private ComunicadoService comunicadoService;
    @Autowired
    private AulaRepository aulaRepository;
    @Autowired
    private AlumnoRepository alumnoRepository;
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private DocenteRepository docenteRepository;

    
    @GetMapping("")
    public String index() {
        return "redirect:/gestioncomunicados/lista";
    }
    
    @GetMapping("/nuevo")
    public String nuevoComunicado(Model model, HttpSession session, RedirectAttributes redirectAttrs) {
        Usuario u = (Usuario) session.getAttribute("usuarioLogueado");
        if (u == null || (!"DOCENTE".equalsIgnoreCase(u.getRol()) && !"ADMINISTRADOR".equalsIgnoreCase(u.getRol()))) {
            redirectAttrs.addFlashAttribute("mensajeError", "No tienes permiso para crear comunicados.");
            return "redirect:/login";
        }
        model.addAttribute("comunicado", new Comunicado());
        model.addAttribute("aulas", aulaRepository.findAll());

        List<String> grados = aulaRepository.listarGradosDistintos();
        model.addAttribute("grados", grados);
        return "comunicados/formulario";
    }

    @PostMapping("/guardar")
    public String guardar(@ModelAttribute("comunicado") Comunicado c,
                          @RequestParam(value = "destinoTipo", required = false) String destinoTipo,
                          HttpSession session, Model model, RedirectAttributes redirectAttrs) {
        Usuario u = (Usuario) session.getAttribute("usuarioLogueado");
        if (u == null || (!"DOCENTE".equalsIgnoreCase(u.getRol()) && !"ADMINISTRADOR".equalsIgnoreCase(u.getRol()))) {
            redirectAttrs.addFlashAttribute("mensajeError", "No tienes permiso para crear comunicados.");
            return "redirect:/login";
        }
        try {

            if (destinoTipo == null) destinoTipo = "AULA"; 

            if ("AULA".equalsIgnoreCase(destinoTipo)) {
                if (c.getAula() == null || c.getAula().getIdAula() == 0) {
                    throw new IllegalArgumentException("Debes seleccionar un aula.");
                }
                Aula a = aulaRepository.findById(c.getAula().getIdAula()).orElseThrow(() -> new IllegalArgumentException("Aula no encontrada."));
                c.setAula(a);
                c.setGrado(null);
            } else if ("GRADO".equalsIgnoreCase(destinoTipo)) {
                if (c.getGrado() == null || c.getGrado().isBlank()) {
                    throw new IllegalArgumentException("Debes seleccionar un grado.");
                }

                c.setAula(null);
            } else if ("GLOBAL".equalsIgnoreCase(destinoTipo)) {
                c.setAula(null);
                c.setGrado(null);
            }

            c.setAutor(u);
            comunicadoService.crearComunicado(c);
            redirectAttrs.addFlashAttribute("mensajeExito", "Comunicado creado correctamente.");
            return "redirect:/gestioncomunicados/lista";
        } catch (Exception e) {
            model.addAttribute("mensajeError", e.getMessage());
            model.addAttribute("aulas", aulaRepository.findAll());
            model.addAttribute("grados", aulaRepository.listarGradosDistintos());
            return "comunicados/formulario";
        }
    }

    @GetMapping("/lista")
    public String listaGestion(Model model) {
        List<Comunicado> comunicaciones = comunicadoService.listarTodos();

        Set<Integer> authorIds = comunicaciones.stream()
                .map(c -> c.getAutor() != null ? c.getAutor().getIdUsuario() : null)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        List<Usuario> autores = authorIds.isEmpty() ? List.of() : usuarioRepository.findAllById(authorIds);

        Map<Integer, String> autorMap = new HashMap<>();
        for (Usuario au : autores) {
            String label;
            if ("DOCENTE".equalsIgnoreCase(au.getRol())) {
                label = docenteRepository.findByUsuario_IdUsuario(au.getIdUsuario())
                        .map(d -> "Docente " + d.getNombre() + " " + d.getApellido())
                        .orElse(au.getNombreCompleto());
            } else if ("ADMINISTRADOR".equalsIgnoreCase(au.getRol())) {
                label = "Administrador";
            } else {
                label = au.getNombreCompleto();
            }
            autorMap.put(au.getIdUsuario(), label);
        }

        model.addAttribute("comunicados", comunicaciones);
        model.addAttribute("autorMap", autorMap);
        return "comunicados/lista";
    }

    @GetMapping("/bandeja")
    public String bandejaAlumno(HttpSession session, Model model) {
        Usuario u = (Usuario) session.getAttribute("usuarioLogueado");
        if (u == null) return "redirect:/login";
        Optional<Alumno> opt = alumnoRepository.findByUsuario_IdUsuario(u.getIdUsuario());
        if (opt.isEmpty()) {
            model.addAttribute("comunicados", List.of());
            return "comunicados/bandeja";
        }
        Alumno alumno = opt.get();
        List<Comunicado> comunicaciones = comunicadoService.listarParaAlumno(alumno);

        Set<Integer> authorIds = comunicaciones.stream()
                .map(c -> c.getAutor().getIdUsuario())
                .collect(Collectors.toSet());
        List<Usuario> autores = usuarioRepository.findAllById(authorIds);
        Map<Integer, String> autorMap = new HashMap<>();
        for (Usuario au : autores) {
            String label;
            if ("DOCENTE".equalsIgnoreCase(au.getRol())) {
                label = docenteRepository.findByUsuario_IdUsuario(au.getIdUsuario())
                        .map(d -> "Docente " + d.getNombre() + " " + d.getApellido())
                        .orElse(au.getNombreCompleto());
            } else if ("ADMINISTRADOR".equalsIgnoreCase(au.getRol())) {
                label = "Administrador";
            } else {
                label = au.getNombreCompleto();
            }
            autorMap.put(au.getIdUsuario(), label);
        }

        model.addAttribute("comunicados", comunicaciones);
        model.addAttribute("autorMap", autorMap);
        return "comunicados/bandeja";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminarMensaje(@PathVariable Integer id, HttpSession session, RedirectAttributes redirectAttrs) {
        Usuario u = (Usuario) session.getAttribute("usuarioLogueado");
        if (u == null) {
            redirectAttrs.addFlashAttribute("mensajeError", "Debes iniciar sesión.");
            return "redirect:/login";
        }
        try {
            comunicadoService.eliminarComunicado(id);
            redirectAttrs.addFlashAttribute("mensajeExito", "Mensaje eliminado correctamente.");
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("mensajeError", "Error al eliminar el mensaje.");
        }
        return "redirect:/gestioncomunicados/bandeja";
    }
}
