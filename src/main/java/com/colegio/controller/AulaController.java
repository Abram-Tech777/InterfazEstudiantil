package com.colegio.controller;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.colegio.entity.Aula;
import com.colegio.entity.Docente;
import com.colegio.entity.AulaDocente;
import com.colegio.service.impl.AulaService;
import com.colegio.service.impl.AulaDocenteService;
import com.colegio.repository.DocenteRepository;
import com.colegio.repository.AlumnoRepository;

@Controller
@RequestMapping("/gestionaulas")
public class AulaController {

	private final AulaService aulaService;
	private final AulaDocenteService aulaDocenteService;
	private final DocenteRepository docenteRepository;
	private final AlumnoRepository alumnoRepository;


    public AulaController(AulaService aulaService,
                          AulaDocenteService aulaDocenteService,
                          DocenteRepository docenteRepository,
                          AlumnoRepository alumnoRepository) {
        this.aulaService = aulaService;
        this.aulaDocenteService = aulaDocenteService;
        this.docenteRepository = docenteRepository;
        this.alumnoRepository = alumnoRepository;
    }


    @GetMapping
    public String listarAulas(Model model) {
        List<Aula> lista = aulaService.listarTodas();
        model.addAttribute("aulas", lista);
        return "aulas/lista"; 
    }


    @GetMapping("/nuevo")
    public String mostrarFormularioNuevo(Model model) {
        model.addAttribute("aula", new Aula());
        return "aulas/formulario";
    }

    @PostMapping("/guardar")
    public String guardarAula(@ModelAttribute("aula") Aula aula, RedirectAttributes redirectAttrs) {
        try {
            if (aula.getIdAula() == 0) {
                aulaService.guardarAula(aula);
                redirectAttrs.addFlashAttribute("mensajeExito", "¡Aula registrada con éxito!");
            } else {
                aulaService.actualizarAula(aula.getIdAula(), aula);
                redirectAttrs.addFlashAttribute("mensajeExito", "¡Aula actualizada con éxito!");
            }
        } catch (IllegalArgumentException e) {
            redirectAttrs.addFlashAttribute("mensajeError", e.getMessage());
            return "redirect:/gestionaulas/nuevo"; 
        }
        return "redirect:/gestionaulas"; 
    }


    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditar(@PathVariable("id") int id, Model model) {
        Aula aula = aulaService.obtenerAulaPorId(id);
        model.addAttribute("aula", aula);
        return "aulas/formulario";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminarAula(@PathVariable("id") int id, RedirectAttributes redirectAttrs) {
        try {
            aulaService.eliminarAula(id);
            redirectAttrs.addFlashAttribute("mensajeExito", "Aula eliminada correctamente.");
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("mensajeError", "No se pudo eliminar el aula.");
        }
        return "redirect:/gestionaulas";
    }


    @GetMapping("/asignar/{id}")
    public String mostrarAsignacion(@PathVariable("id") int id, Model model) {
        Aula aula = aulaService.obtenerAulaPorId(id);
        
        List<AulaDocente> docentesAsignados = aulaDocenteService.listarPorAula(id);
        List<Integer> idsAsignados = docentesAsignados.stream()
                .map(ad -> ad.getDocente().getIdDocente())
                .collect(Collectors.toList());
        
        List<Docente> todosDocentes = docenteRepository.findAll();
        
        model.addAttribute("aula", aula);
        model.addAttribute("todosDocentes", todosDocentes);
        model.addAttribute("docentesAsignados", docentesAsignados);
        model.addAttribute("idsAsignados", idsAsignados);
        
        return "aulas/asignar";
    }


    @PostMapping("/asignar/{id}")
    public String guardarAsignacion(@PathVariable("id") int id, @RequestParam(value = "docentes", required = false) List<Integer> docentes,@RequestParam(value = "rol", defaultValue = "DOCENTE") String rol,          HttpServletRequest request,
       RedirectAttributes redirectAttrs) {
        try {
            if (docentes == null) {
                docentes = List.of();
            }
            

            Map<Integer, String> rolesPorDocente = new HashMap<>();
            for (Integer idDocente : docentes) {
                String rolPerDocente = request.getParameter("rolAsignado_" + idDocente);
                if (rolPerDocente == null || rolPerDocente.isEmpty()) {
                    rolPerDocente = rol; 
                }
                rolesPorDocente.put(idDocente, rolPerDocente);
            }
            

            aulaDocenteService.asignarDocentesAulaMultipleConRolesUnTutor(id, rolesPorDocente);
            redirectAttrs.addFlashAttribute("mensajeExito", "Docentes asignados correctamente.");
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("mensajeError", "Error al asignar docentes: " + e.getMessage());
        }
        return "redirect:/gestionaulas";
    }


    @GetMapping("/detalle/{id}")
    public String verDetalleAula(@PathVariable("id") int id, Model model) {
        Aula aula = aulaService.obtenerAulaPorId(id);    
        List<?> alumnos = alumnoRepository.findByAula_IdAula(id);      
        List<AulaDocente> docentesAsignados = aulaDocenteService.listarPorAula(id);
        
        model.addAttribute("aula", aula);
        model.addAttribute("alumnos", alumnos);
        model.addAttribute("docentesAsignados", docentesAsignados);
        return "aulas/detalle";
    }
}
