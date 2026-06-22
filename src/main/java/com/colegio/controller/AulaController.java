package com.colegio.controller;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

import jakarta.servlet.http.HttpServletRequest;
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

import com.colegio.entity.Aula;
import com.colegio.entity.Docente;
import com.colegio.entity.AulaDocente;
import com.colegio.entity.Alumno;
import jakarta.persistence.EntityNotFoundException;
import com.colegio.service.impl.AulaService;
import com.colegio.service.impl.AulaDocenteService;
import com.colegio.repository.DocenteRepository;
import com.colegio.repository.AlumnoRepository;

@Controller
@RequestMapping("/gestionaulas")
public class AulaController {

    @Autowired
    private AulaService aulaService;
    @Autowired
    private AulaDocenteService aulaDocenteService;
    @Autowired
    private DocenteRepository docenteRepository;
    @Autowired
    private AlumnoRepository alumnoRepository;
    @Autowired
    private com.colegio.service.impl.AulaAlumnoService aulaAlumnoService;

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

        java.util.Map<Integer, String> rolesPorDocente = new java.util.LinkedHashMap<>();
        java.util.List<Integer> idsAsignados = new java.util.ArrayList<>();
        for (AulaDocente ad : docentesAsignados) {
            if (ad.isActivo()) {
                int idDoc = ad.getDocente().getIdDocente();
                rolesPorDocente.putIfAbsent(idDoc, ad.getRol());
                if (!idsAsignados.contains(idDoc)) idsAsignados.add(idDoc);
            }
        }

        List<Docente> todosDocentes = docenteRepository.findAll();

        model.addAttribute("aula", aula);
        model.addAttribute("todosDocentes", todosDocentes);
        model.addAttribute("docentesAsignados", docentesAsignados);
        model.addAttribute("idsAsignados", idsAsignados);
        model.addAttribute("rolesPorDocente", rolesPorDocente);
        
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

    @GetMapping("/asignarAlumnos/{id}")
    public String mostrarAsignacionAlumnos(@PathVariable("id") int id, Model model) {
        Aula aula = aulaService.obtenerAulaPorId(id);
        java.util.List<com.colegio.entity.Alumno> todosAlumnos = alumnoRepository.findAll();
        java.util.List<com.colegio.entity.Alumno> alumnosAsignados = alumnoRepository.findByAula_IdAula(id);
        java.util.List<Integer> idsAsignados = alumnosAsignados.stream().map(a -> a.getIdAlumno()).collect(java.util.stream.Collectors.toList());

        model.addAttribute("aula", aula);
        model.addAttribute("todosAlumnos", todosAlumnos);
        model.addAttribute("idsAsignados", idsAsignados);
        return "aulas/asignar_alumnos";
    }

    @PostMapping("/asignarAlumnos/{id}")
    public String guardarAsignacionAlumnos(@PathVariable("id") int id,
            @RequestParam(value = "alumnos", required = false) java.util.List<Integer> alumnos,
            RedirectAttributes redirectAttrs) {
        try {
            aulaAlumnoService.asignarAlumnosAula(id, alumnos);
            redirectAttrs.addFlashAttribute("mensajeExito", "Alumnos asignados correctamente.");
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("mensajeError", "Error al asignar alumnos: " + e.getMessage());
        }
        return "redirect:/gestionaulas";
    }

    @GetMapping("/quitarAlumno/{id}")
    public String quitarAlumno(@PathVariable("id") int id, RedirectAttributes redirectAttrs) {
        try {
            Alumno alumno = alumnoRepository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("Alumno no encontrado"));
            Integer aulaId = alumno.getAula() != null ? alumno.getAula().getIdAula() : null;
            alumno.setAula(null);
            alumnoRepository.save(alumno);
            redirectAttrs.addFlashAttribute("mensajeExito", "Alumno quitado del aula correctamente.");
            return aulaId != null ? "redirect:/gestionaulas/detalle/" + aulaId : "redirect:/gestionaulas";
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("mensajeError", "Error al quitar alumno: " + e.getMessage());
            return "redirect:/gestionaulas";
        }
    }


}
