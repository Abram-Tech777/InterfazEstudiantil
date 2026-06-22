package com.colegio.controller;

import java.time.LocalDate;
import java.util.List;
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
import org.springframework.web.bind.annotation.RequestParam;

import com.colegio.dto.HorarioDTO;
import com.colegio.dto.HorarioMatrizDTO;
import com.colegio.entity.Aula;
import com.colegio.entity.Docente;
import com.colegio.entity.Horario;
import com.colegio.entity.Usuario;
import com.colegio.repository.AulaDocenteRepository;
import com.colegio.repository.AulaRepository;
import com.colegio.repository.DocenteRepository;
import com.colegio.service.HorarioPDFService;
import com.colegio.service.impl.HorarioService;

@Controller
public class DocenteController {
    
    private static final Logger logger = LoggerFactory.getLogger(DocenteController.class);

    private final DocenteRepository docenteRepository;
    private final AulaRepository aulaRepository;
    private final AulaDocenteRepository aulaDocenteRepository;
    private final HorarioService horarioService;
    private final HorarioPDFService horarioPDFService;

    public DocenteController(DocenteRepository docenteRepository,
                            AulaRepository aulaRepository,
                            AulaDocenteRepository aulaDocenteRepository,
                            HorarioService horarioService,
                            HorarioPDFService horarioPDFService) {
        this.docenteRepository = docenteRepository;
        this.aulaRepository = aulaRepository;
        this.aulaDocenteRepository = aulaDocenteRepository;
        this.horarioService = horarioService;
        this.horarioPDFService = horarioPDFService;
    }

    @GetMapping("/docente/horario")
    public String verHorario(HttpSession session, Model model, @RequestParam(required = false) Integer idAula) {
        Usuario u = (Usuario) session.getAttribute("usuarioLogueado");
        if (u == null || !"DOCENTE".equalsIgnoreCase(u.getRol())) {
            return "redirect:/login";
        }
        
        // Obtener docente del usuario
        Optional<Docente> optDocente = docenteRepository.findByUsuario_IdUsuario(u.getIdUsuario());
        if (optDocente.isEmpty()) {
            logger.warn("Docente no encontrado para usuario: {}", u.getIdUsuario());
            model.addAttribute("horarioMatriz", null);
            model.addAttribute("aulas", List.of());
            return "docente/horario";
        }
        
        Docente docente = optDocente.get();
        logger.info("Docente encontrado: {} (ID: {})", docente.getNombreCompleto(), docente.getIdDocente());
        
        // Obtener aulas asignadas al docente
        List<Aula> aulasAsignadas = aulaRepository.findAll().stream()
            .filter(aula -> aulaDocenteRepository.findByDocente_IdDocenteAndAula_IdAula(docente.getIdDocente(), aula.getIdAula()).isPresent())
            .toList();
        
        if (aulasAsignadas.isEmpty()) {
            logger.warn("Docente sin aulas asignadas");
            model.addAttribute("horarioMatriz", null);
            model.addAttribute("aulas", List.of());
            return "docente/horario";
        }
        
        // Si no viene idAula seleccionado, usar la primera
        if (idAula == null) {
            idAula = aulasAsignadas.get(0).getIdAula();
        }
        
        // Validar que el aula seleccionada pertenece al docente
        final int aulaSeleccionada = idAula;
        boolean aulaValida = aulasAsignadas.stream()
            .anyMatch(aula -> aula.getIdAula() == aulaSeleccionada);
        
        if (!aulaValida) {
            idAula = aulasAsignadas.get(0).getIdAula();
        }
        
        Aula aulaActual = aulaRepository.findById(idAula).orElse(null);
        if (aulaActual == null) {
            model.addAttribute("horarioMatriz", null);
            model.addAttribute("aulas", aulasAsignadas);
            return "docente/horario";
        }
        
        logger.info("Aula seleccionada: {} (ID: {})", aulaActual.getNombre(), aulaActual.getIdAula());
        
        // Obtener horarios del docente para esta aula
        LocalDate hoy = LocalDate.now();
        logger.info("========== HORARIO DEL DOCENTE ==========");
        logger.info("Docente: {} (ID: {}), Aula ID: {}", docente.getNombreCompleto(), docente.getIdDocente(), aulaActual.getIdAula());
        logger.info("Buscando horarios para fecha: {}", hoy);
        
        List<Horario> horarios = horarioService.findHorariosActivos(aulaActual.getIdAula(), hoy).stream()
            .filter(h -> h.getDocente().getIdDocente() == docente.getIdDocente())
            .toList();
        
        logger.info("Horarios encontrados: {}", horarios.size());
        if (horarios.isEmpty()) {
            logger.warn("NO SE ENCONTRARON HORARIOS PARA ESTE DOCENTE EN ESTA AULA");
        }
        for (Horario h : horarios) {
            logger.info("  OK {} {} {} -> {} (Activo: {}, FechaInicio: {}, FechaFin: {})", 
                h.getDiaSemana(), h.getHoraInicio(), h.getHoraFin(), 
                h.getCurso().getNombreCurso(), h.getActivo(), h.getFechaInicio(), h.getFechaFin());
        }
        
        // Construir matriz
        HorarioMatrizDTO matriz = construirMatrizHorario(horarios, aulaActual.getNombre(), aulaActual.getNombre());
        logger.info("Matriz construida con {} horas unicas", matriz.getHoras().size());
        logger.info("========== FIN HORARIO DEL DOCENTE ==========");
        
        model.addAttribute("horarioMatriz", matriz);
        model.addAttribute("aulas", aulasAsignadas);
        model.addAttribute("aulaSeleccionada", idAula);
        model.addAttribute("docenteNombre", docente.getNombreCompleto());
        
        return "docente/horario";
    }

    @GetMapping("/docente/horario/descargar")
    public ResponseEntity<byte[]> descargarHorarioPDF(HttpSession session, @RequestParam(required = false) Integer idAula) {
        Usuario u = (Usuario) session.getAttribute("usuarioLogueado");
        if (u == null || !"DOCENTE".equalsIgnoreCase(u.getRol())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        Optional<Docente> optDocente = docenteRepository.findByUsuario_IdUsuario(u.getIdUsuario());
        if (optDocente.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        
        Docente docente = optDocente.get();
        
        // Si no viene idAula, usar la primera aula asignada
        if (idAula == null) {
            List<Integer> aulasIds = aulaDocenteRepository.findAulaIdsByDocenteId(docente.getIdDocente());
            if (aulasIds.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            idAula = aulasIds.get(0);
        }
        
        Optional<Aula> optAula = aulaRepository.findById(idAula);
        if (optAula.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        
        Aula aula = optAula.get();
        List<Horario> horarios = horarioService.findHorariosActivos(aula.getIdAula(), LocalDate.now()).stream()
            .filter(h -> h.getDocente().getIdDocente() == docente.getIdDocente())
            .toList();
        
        HorarioMatrizDTO matriz = construirMatrizHorario(horarios, aula.getNombre(), aula.getNombre());
        
        try {
            byte[] pdfBytes = horarioPDFService.generarPDFHorario(matriz);
            
            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Disposition", "attachment; filename=horario_docente.pdf");
            headers.add("Content-Type", "application/pdf");
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);
        } catch (Exception e) {
            logger.error("Error al generar PDF: {}", e.getMessage());
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
}
