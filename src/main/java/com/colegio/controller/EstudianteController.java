package com.colegio.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Map;
import java.util.HashMap;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.colegio.dto.HorarioDTO;
import com.colegio.dto.HorarioMatrizDTO;
import com.colegio.entity.Alumno;
import com.colegio.entity.Asistencia;
import com.colegio.entity.EvaluacionNota;
import com.colegio.entity.Horario;
import com.colegio.entity.Usuario;
import com.colegio.repository.AlumnoRepository;
import com.colegio.repository.AsistenciaRepository;
import com.colegio.repository.EvaluacionNotaRepository;
import com.colegio.service.HorarioPDFService;
import com.colegio.service.impl.HorarioService;
import com.colegio.repository.*;
import com.colegio.entity.Comunicado;

@Controller
public class EstudianteController {
    
    private static final Logger logger = LoggerFactory.getLogger(EstudianteController.class);

    @Autowired
    private AlumnoRepository alumnoRepository;
    @Autowired
    private HorarioService horarioService;
    @Autowired
    private AsistenciaRepository asistenciaRepository;
    @Autowired
    private EvaluacionNotaRepository evaluacionNotaRepository;
    @Autowired
    private HorarioPDFService horarioPDFService;
    @Autowired
    private ComunicadoRepository comunicadoRepository;
    @Autowired
    private ConductaRepository conductaRepository;

    @GetMapping("/estudiante/panel")
    public String mostrarPanel(HttpSession session, Model model) {
        Usuario u = (Usuario) session.getAttribute("usuarioLogueado");
        if (u == null || !"ESTUDIANTE".equalsIgnoreCase(u.getRol())) return "redirect:/login";
        
        Optional<Alumno> opt = alumnoRepository.findByUsuario_IdUsuario(u.getIdUsuario());
        if (opt.isPresent()) {
            Alumno alumno = opt.get();
            model.addAttribute("alumno", alumno);
            
            // Anuncios
            model.addAttribute("anuncios", comunicadoRepository.listarTodosOrdenados());
            
            // Asistencia
            List<Asistencia> asistencias = asistenciaRepository.findByAlumno_IdAlumnoOrderByFechaDesc(alumno.getIdAlumno());
            model.addAttribute("totalAsistidos", asistencias.stream().filter(a -> "PRESENTE".equals(a.getEstado())).count());
            model.addAttribute("totalClases", asistencias.size());
            
            model.addAttribute("conductas", conductaRepository.findByAlumno_IdAlumno(alumno.getIdAlumno()));
        }
        
        return "estudiante/panel"; 
    }

    @GetMapping("/estudiante/anuncios")
    public String mostrarAnuncios(HttpSession session, Model model) {
        Usuario u = (Usuario) session.getAttribute("usuarioLogueado");
        if (u == null || !"ESTUDIANTE".equalsIgnoreCase(u.getRol())) return "redirect:/login";
        
        Optional<Alumno> opt = alumnoRepository.findByUsuario_IdUsuario(u.getIdUsuario());
        if (opt.isPresent()) {
            Alumno alumno = opt.get();
            model.addAttribute("alumno", alumno);
            // Asegurarnos de que el alumno tiene aula asignada antes de llamar al repositorio
            if (alumno.getAula() == null) {
                // Si no hay aula asignada, devolvemos listas vacías para evitar NPE y mostrar la vista
                model.addAttribute("comunicados", List.of());
                model.addAttribute("autorMap", Map.of());
            } else {
                // 1. Buscamos los comunicados para el aula y grado del alumno
                List<Comunicado> comunicados = comunicadoRepository.listarParaAulaOGrado(
                    alumno.getAula().getIdAula(), 
                    alumno.getAula().getGrado()
                );
                model.addAttribute("comunicados", comunicados);

                // 2. Creamos el mapa de autores para que el nombre del docente salga en la vista
                // (Esto es necesario porque la vista usa autorMap[c.autor.idUsuario])
                Map<Integer, String> autorMap = new HashMap<>();
                for (Comunicado c : comunicados) {
                    if (c.getAutor() != null) {
                        autorMap.put(c.getAutor().getIdUsuario(), c.getAutor().getNombreCompleto());
                    }
                }
                model.addAttribute("autorMap", autorMap);
            }
        }
        
        return "comunicados/bandeja";
    }

    // ==========================================
    // VISTA DE HORARIO
    // ==========================================
    @GetMapping("/estudiante/horario")
    public String verHorario(HttpSession session, Model model) {
        Usuario u = (Usuario) session.getAttribute("usuarioLogueado");
        if (u == null || !"ESTUDIANTE".equalsIgnoreCase(u.getRol())) return "redirect:/login";
        
        Optional<Alumno> opt = alumnoRepository.findByUsuario_IdUsuario(u.getIdUsuario());
        if (opt.isEmpty()) {
            logger.warn("Alumno no encontrado para usuario: {}", u.getIdUsuario());
            model.addAttribute("horarioMatriz", null);
            return "estudiante/horario";
        }
        
        Alumno alumno = opt.get();
        model.addAttribute("alumno", alumno); 
        
        if (alumno.getAula() == null) {
            logger.warn("Alumno sin aula asignada");
            model.addAttribute("horarioMatriz", null);
            return "estudiante/horario";
        }
        
        LocalDate hoy = LocalDate.now();
        List<Horario> horarios = horarioService.findHorariosActivos(alumno.getAula().getIdAula(), hoy);
        
        HorarioMatrizDTO matriz = construirMatrizHorario(horarios, alumno.getAula().getNombre(), alumno.getAula().getNombre());
        model.addAttribute("horarioMatriz", matriz);
        return "estudiante/horario";
    }

    @GetMapping("/estudiante/horario/descargar")
    public ResponseEntity<byte[]> descargarHorarioPDF(HttpSession session) {
        Usuario u = (Usuario) session.getAttribute("usuarioLogueado");
        if (u == null || !"ESTUDIANTE".equalsIgnoreCase(u.getRol())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        Optional<Alumno> opt = alumnoRepository.findByUsuario_IdUsuario(u.getIdUsuario());
        if (opt.isEmpty() || opt.get().getAula() == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        
        Alumno alumno = opt.get();
        List<Horario> horarios = horarioService.findHorariosActivos(alumno.getAula().getIdAula(), LocalDate.now());
        HorarioMatrizDTO matriz = construirMatrizHorario(horarios, alumno.getAula().getNombre(), alumno.getAula().getNombre());
        
        try {
            byte[] pdfBytes = horarioPDFService.generarPDFHorario(matriz);
            
            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Disposition", "attachment; filename=horario.pdf");
            headers.add("Content-Type", "application/pdf");
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private HorarioMatrizDTO construirMatrizHorario(List<Horario> horarios, String aulaNombre, String aulaGrado) {
        HorarioMatrizDTO matriz = new HorarioMatrizDTO(aulaNombre, aulaGrado);
        
        for (Horario h : horarios) {
            HorarioDTO dto = new HorarioDTO(
                    h.getIdHorario(),
                    h.getCurso() != null ? h.getCurso().getNombreCurso() : "N/A",
                    h.getDocente() != null ? h.getDocente().getNombre() : "N/A",
                    h.getHoraInicio(),
                    h.getHoraFin(),
                    h.getDiaSemana()
            );
            matriz.agregarHorario(h.getHoraInicio(), h.getDiaSemana(), dto);
        }
        
        return matriz;
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
        model.addAttribute("alumno", alumno); 
        List<EvaluacionNota> notas = evaluacionNotaRepository.findByAlumno_IdAlumnoOrderByFechaRegistroDesc(alumno.getIdAlumno());
        model.addAttribute("notas", notas);
        return "estudiante/notas";
    }

    // ==========================================
    // VISTA DE ASISTENCIA / BITÁCORA
    // ==========================================
    @GetMapping("/estudiante/asistencia")
    public String verAsistencia(HttpSession session, Model model) {
        Usuario u = (Usuario) session.getAttribute("usuarioLogueado");
        if (u == null || !"ESTUDIANTE".equalsIgnoreCase(u.getRol())) return "redirect:/login";
        
        Optional<Alumno> opt = alumnoRepository.findByUsuario_IdUsuario(u.getIdUsuario());
        if (opt.isEmpty()) {
            model.addAttribute("asistencias", List.of());
            model.addAttribute("presentes", 0);
            model.addAttribute("retardos", 0);
            model.addAttribute("ausencias", 0);
            return "estudiante/asistencia";
        }
        
        Alumno alumno = opt.get();
        model.addAttribute("alumno", alumno); 
        List<Asistencia> asistencias = asistenciaRepository.findByAlumno_IdAlumnoOrderByFechaDesc(alumno.getIdAlumno());
        
        long presentes = asistencias.stream().filter(a -> "PRESENTE".equals(a.getEstado())).count();
        long retardos = asistencias.stream().filter(a -> "RETARDO".equals(a.getEstado())).count();
        long ausencias = asistencias.stream().filter(a -> "AUSENCIA".equals(a.getEstado())).count();
        
        model.addAttribute("asistencias", asistencias);
        model.addAttribute("presentes", presentes);
        model.addAttribute("retardos", retardos);
        model.addAttribute("ausencias", ausencias);
        return "estudiante/asistencia";
    }
}