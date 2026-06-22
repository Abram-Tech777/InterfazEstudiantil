package com.colegio.controller;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

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

@Controller
public class EstudianteController {
    
    private static final Logger logger = LoggerFactory.getLogger(EstudianteController.class);

    private final AlumnoRepository alumnoRepository;
    private final HorarioService horarioService;
    private final AsistenciaRepository asistenciaRepository;
    private final EvaluacionNotaRepository evaluacionNotaRepository;
    private final HorarioPDFService horarioPDFService;

    public EstudianteController(AlumnoRepository alumnoRepository, 
                                HorarioService horarioService,
                                AsistenciaRepository asistenciaRepository,
                                EvaluacionNotaRepository evaluacionNotaRepository,
                                HorarioPDFService horarioPDFService) {
        this.alumnoRepository = alumnoRepository;
        this.horarioService = horarioService;
        this.asistenciaRepository = asistenciaRepository;
        this.evaluacionNotaRepository = evaluacionNotaRepository;
        this.horarioPDFService = horarioPDFService;
    }

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
        logger.info("Alumno encontrado: {} (ID: {})", alumno.getNombreCompleto(), alumno.getIdAlumno());
        
        if (alumno.getAula() == null) {
            logger.warn("Alumno sin aula asignada");
            model.addAttribute("horarioMatriz", null);
            return "estudiante/horario";
        }
        
        logger.info("Aula asignada: {} (ID: {})", alumno.getAula().getNombre(), alumno.getAula().getIdAula());
        
        // Obtener horarios activos para hoy
        LocalDate hoy = LocalDate.now();
        logger.info("========== HORARIO DEL ESTUDIANTE ==========");
        logger.info("Alumno: {} (ID: {}), Aula ID: {}", alumno.getNombreCompleto(), alumno.getIdAlumno(), alumno.getAula().getIdAula());
        logger.info("Buscando horarios para fecha: {}", hoy);
        List<Horario> horarios = horarioService.findHorariosActivos(alumno.getAula().getIdAula(), hoy);
        
        logger.info("Horarios encontrados: {}", horarios.size());
        if (horarios.isEmpty()) {
            logger.warn("⚠️ NO SE ENCONTRARON HORARIOS PARA ESTA AULA");
        }
        for (Horario h : horarios) {
            logger.info("  ✓ {} {} {} -> {} (Activo: {}, FechaInicio: {}, FechaFin: {})", 
                h.getDiaSemana(), h.getHoraInicio(), h.getHoraFin(), 
                h.getCurso().getNombreCurso(), h.getActivo(), h.getFechaInicio(), h.getFechaFin());
        }
        
        // Construir matriz
        HorarioMatrizDTO matriz = construirMatrizHorario(horarios, alumno.getAula().getNombre(), alumno.getAula().getNombre());
        logger.info("Matriz construida con {} horas únicas", matriz.getHoras().size());
        logger.info("========== FIN HORARIO DEL ESTUDIANTE ==========");
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
            logger.info("  Agregado a matriz: [{}] [{}] {} - {}", h.getHoraInicio(), h.getDiaSemana(), h.getCurso().getNombreCurso(), h.getDocente().getNombre());
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
            model.addAttribute("presentes", 0);
            model.addAttribute("retardos", 0);
            model.addAttribute("ausencias", 0);
            return "estudiante/asistencia";
        }
        
        Alumno alumno = opt.get();
        List<Asistencia> asistencias = asistenciaRepository.findByAlumno_IdAlumnoOrderByFechaDesc(alumno.getIdAlumno());
        
        // Contar estados
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

