package com.colegio.controller;

import com.colegio.repository.DocenteRepository;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.colegio.entity.Horario;
import com.colegio.entity.Usuario;
import com.colegio.entity.Docente;
import com.colegio.repository.AulaRepository;
import com.colegio.repository.CursoRepository;
import com.colegio.service.impl.HorarioService;
import com.colegio.service.impl.AulaDocenteService;
import com.colegio.entity.Aula;

@Controller
@RequestMapping("/gestionarhorarios")
public class HorarioController {

	@Autowired
	private HorarioService horarioService;
	@Autowired
	private  AulaRepository aulaRepository;
	@Autowired
	private DocenteRepository docenteRepository;
	@Autowired
	private  CursoRepository cursoRepository;
	@Autowired
	private AulaDocenteService aulaDocenteService;

	public HorarioController(HorarioService horarioService,
	                         AulaRepository aulaRepository,
	                         DocenteRepository docenteRepository,
	                         CursoRepository cursoRepository,
	                         AulaDocenteService aulaDocenteService) {
	    this.horarioService = horarioService;
	    this.aulaRepository = aulaRepository;
	    this.docenteRepository = docenteRepository;
	    this.cursoRepository = cursoRepository;
	    this.aulaDocenteService = aulaDocenteService;
	}
	
	@GetMapping
	public String listarHorarios(Model model, HttpSession session) {
	    List<Horario> horarios;
	    boolean esDocente = false;
	    
	    Usuario usuarioLogueado = (Usuario) session.getAttribute("usuarioLogueado");
	    
	    if (usuarioLogueado != null && "DOCENTE".equals(usuarioLogueado.getRol())) {
	        esDocente = true;
	        var docenteOpt = docenteRepository.findByUsuario_IdUsuario(usuarioLogueado.getIdUsuario());
	        if (docenteOpt.isPresent()) {
	            Docente docente = docenteOpt.get();
	            List<Integer> aulasAsignadas = aulaDocenteService.listarAulasDelDocente(docente.getIdDocente());
	            if (!aulasAsignadas.isEmpty()) {
	                horarios = horarioService.findHorarioRepository().findByDocenteAndAulasAsignadas(docente.getIdDocente(), aulasAsignadas);
	            } else {
	                horarios = List.of(); 
	            }
	        } else {
	            horarios = List.of();
	        }
	    } else {
	        horarios = horarioService.findAll();
	    }
	    
	    model.addAttribute("horarios", horarios);
	    model.addAttribute("esDocente", esDocente);
	    
	    Map<Integer, String> docentesMap = docenteRepository.findAll()
	            .stream()
	            .collect(Collectors.toMap(d -> d.getIdDocente(), d -> d.getNombre() + " " + d.getApellido()));
	    model.addAttribute("docentesMap", docentesMap);

	    Map<Integer, String> cursosMap = cursoRepository.findAll()
	            .stream()
	            .collect(Collectors.toMap(c -> c.getIdCurso(), c -> c.getNombreCurso()));
	    model.addAttribute("cursosMap", cursosMap);

	    return "horarios/lista";
	}
	
	
	@GetMapping("/nuevo")
	public String mostrarFormularioNuevo(@ModelAttribute("horario") Horario horario, Model model, HttpSession session) {
	    Usuario usuarioLogueado = (Usuario) session.getAttribute("usuarioLogueado");
	    if (usuarioLogueado != null && "DOCENTE".equals(usuarioLogueado.getRol())) {
	        return "redirect:/gestionarhorarios";
	    }
	    
	    if (horario.getAula() == null) {
	        horario.setAula(new Aula());
	    }
	    
	    model.addAttribute("aulas", aulaRepository.findAll());
	    model.addAttribute("cursos", cursoRepository.findAll());
	    model.addAttribute("docentes", docenteRepository.findAll());
	    model.addAttribute("dias", diasSemana());
	    
	    return "horarios/formulario";
	}
	
	@PostMapping("/guardar")
	public String guardarHorario(@ModelAttribute("horario") Horario h, @RequestParam(value = "horaInicio", required = false) String horaInicioStr,
	       @RequestParam(value = "horaFin", required = false) String horaFinStr,
	       Model model, RedirectAttributes redirectAttrs, HttpSession session) {
	    Usuario usuarioLogueado = (Usuario) session.getAttribute("usuarioLogueado");
	    if (usuarioLogueado != null && "DOCENTE".equals(usuarioLogueado.getRol())) {
	        return "redirect:/gestionarhorarios";
	    }
	    
	    try {
	        if (h.getAula() == null || h.getAula().getIdAula() == 0) {
	            throw new IllegalArgumentException("Debe seleccionar un aula válida.");
	        }
	        
	        Aula aula = aulaRepository.findById(h.getAula().getIdAula())
	                .orElseThrow(() -> new IllegalArgumentException("Aula no encontrada"));
	        h.setAula(aula);

	        try {
	            if (h.getHoraInicio() == null && horaInicioStr != null && !horaInicioStr.isBlank()) {
	                h.setHoraInicio(java.time.LocalTime.parse(horaInicioStr));
	            }
	            if (h.getHoraFin() == null && horaFinStr != null && !horaFinStr.isBlank()) {
	                h.setHoraFin(java.time.LocalTime.parse(horaFinStr));
	            }
	        } catch (java.time.format.DateTimeParseException ex) {
	            throw new IllegalArgumentException("Formato de hora inválido. Usa el selector o el formato 24h (ej. 13:00).");
	        }

	        if (h.getIdHorario() == 0) {
	            horarioService.crearHorario(h);
	            redirectAttrs.addFlashAttribute("mensajeExito", "Horario creado correctamente.");
	        } else {
	            horarioService.updateHorario(h.getIdHorario(), h);
	            redirectAttrs.addFlashAttribute("mensajeExito", "Horario actualizado correctamente.");
	        }
	    } catch (Exception e) {
	        model.addAttribute("mensajeError", e.getMessage());
	        model.addAttribute("horario", h);
	        model.addAttribute("aulas", aulaRepository.findAll());
	        model.addAttribute("dias", diasSemana());
	        return "horarios/formulario";
	    }
	    return "redirect:/gestionarhorarios";
    }
	
	@GetMapping("/editar/{id}")
    public String mostrarFormularioEditar(@PathVariable("id") int id, Model model, HttpSession session) {
        Usuario usuarioLogueado = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuarioLogueado != null && "DOCENTE".equals(usuarioLogueado.getRol())) {
            return "redirect:/gestionarhorarios";
        }
        
        Horario h = horarioService.getHorarioById(id);
        if (h.getAula() == null) {
            h.setAula(new Aula());
        }
        model.addAttribute("horario", h);
        model.addAttribute("aulas", aulaRepository.findAll());
        model.addAttribute("cursos", cursoRepository.findAll());
        model.addAttribute("docentes", docenteRepository.findAll());
        model.addAttribute("dias", diasSemana());
        return "horarios/formulario";
    }
	
	@GetMapping("/eliminar/{id}")
    public String eliminarHorario(@PathVariable("id") int id, RedirectAttributes redirectAttrs, HttpSession session) {
        Usuario usuarioLogueado = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuarioLogueado != null && "DOCENTE".equals(usuarioLogueado.getRol())) {
            return "redirect:/gestionarhorarios";
        }
        
        try {
            horarioService.deleteHorario(id);
            redirectAttrs.addFlashAttribute("mensajeExito", "Horario eliminado correctamente.");
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("mensajeError", "No se pudo eliminar el horario: " + e.getMessage());
        }
        return "redirect:/gestionarhorarios";
    }
	
	private List<String> diasSemana() {
        return Arrays.asList("LUNES", "MARTES", "MIERCOLES", "JUEVES", "VIERNES");
    }
	
}