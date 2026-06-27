package com.colegio.controller;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.colegio.dto.FilaHorarioDTO;
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
import com.colegio.util.JornadaConfig;

@Controller
public class DocenteController {
    
    private static final Logger logger = LoggerFactory.getLogger(DocenteController.class);

    @Autowired
    private DocenteRepository docenteRepository;
    @Autowired
    private AulaRepository aulaRepository;
    @Autowired
    private AulaDocenteRepository aulaDocenteRepository;
    @Autowired
    private HorarioService horarioService;
    @Autowired
    private HorarioPDFService horarioPDFService;
    @Autowired
    private JornadaConfig jornadaConfig;

    @GetMapping("/docente/horario")
    public String verHorario(HttpSession session, Model model,
                             @RequestParam(required = false) Integer idAula,
                             @RequestParam(required = false, defaultValue = "propio") String filtro) {
        Usuario u = (Usuario) session.getAttribute("usuarioLogueado");
        if (u == null || !"DOCENTE".equalsIgnoreCase(u.getRol())) {
            return "redirect:/login";
        }
        
        Optional<Docente> optDocente = docenteRepository.findByUsuario_IdUsuario(u.getIdUsuario());
        if (optDocente.isEmpty()) {
            logger.warn("Docente no encontrado para usuario: {}", u.getIdUsuario());
            model.addAttribute("horarioMatriz", null);
            model.addAttribute("aulas", List.of());
            return "docente/horario";
        }
        
        Docente docente = optDocente.get();
        logger.info("Docente encontrado: {} (ID: {})", docente.getNombreCompleto(), docente.getIdDocente());
        
        List<Aula> aulasAsignadas = aulaRepository.findAll().stream()
            .filter(aula -> aulaDocenteRepository.findByDocente_IdDocenteAndAula_IdAula(docente.getIdDocente(), aula.getIdAula()).isPresent())
            .toList();
        
        if (aulasAsignadas.isEmpty()) {
            logger.warn("Docente sin aulas asignadas");
            model.addAttribute("horarioMatriz", null);
            model.addAttribute("aulas", List.of());
            return "docente/horario";
        }
        
        if (idAula == null) {
            idAula = aulasAsignadas.get(0).getIdAula();
        }
        
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
        
        logger.info("Aula seleccionada: {} (ID: {}), Filtro: {}", aulaActual.getNombre(), aulaActual.getIdAula(), filtro);
        
        LocalDate hoy = LocalDate.now();
        List<Horario> horariosAula = horarioService.findHorariosActivos(aulaActual.getIdAula(), hoy);
        
        List<Horario> horarios;
        if ("completo".equalsIgnoreCase(filtro)) {
            horarios = horariosAula;
            logger.info("Filtro COMPLETO: mostrando todos los horarios del aula ({} registros)", horarios.size());
        } else {
            horarios = horariosAula.stream()
                .filter(h -> h.getDocente().getIdDocente() == docente.getIdDocente())
                .toList();
            logger.info("Filtro PROPIO: mostrando solo horarios del docente ({} registros)", horarios.size());
        }
        
        logger.info("========== HORARIOS ==========");
        for (Horario h : horarios) {
            logger.info("  [{}] {} {} - {} -> {} (Docente: {})", 
                h.getDiaSemana(), h.getHoraInicio(), h.getHoraFin(), 
                h.getCurso().getNombreCurso(), h.getDocente().getNombre());
        }
        
        HorarioMatrizDTO matriz = construirMatrizHorario(horarios, aulaActual.getNombre(), aulaActual.getNombre());
        
        java.util.LinkedHashMap<String, String> cursosColores = new java.util.LinkedHashMap<>();
        for (Horario h : horarios) {
            String nombreCurso = h.getCurso() != null ? h.getCurso().getNombreCurso() : "N/A";
            if (!cursosColores.containsKey(nombreCurso)) {
                cursosColores.put(nombreCurso, asignarColorCurso(nombreCurso));
            }
        }

        // Calcular jornada real desde los horarios
        String jornadaInicioReal = jornadaConfig.getInicio().toString();
        String jornadaFinReal = jornadaConfig.getFin().toString();
        if (!horarios.isEmpty()) {
            LocalTime minInicio = horarios.stream()
                .map(Horario::getHoraInicio)
                .min(Comparator.naturalOrder())
                .orElse(jornadaConfig.getInicio());
            LocalTime maxFin = horarios.stream()
                .map(Horario::getHoraFin)
                .max(Comparator.naturalOrder())
                .orElse(jornadaConfig.getFin());
            // Adjust to show full jornada (round down/up to nearest 15 min)
            int startMin = (minInicio.getHour() * 60 + minInicio.getMinute()) / 15 * 15;
            int endMin = ((maxFin.getHour() * 60 + maxFin.getMinute()) + 14) / 15 * 15;
            jornadaInicioReal = LocalTime.of(startMin / 60, startMin % 60).toString();
            jornadaFinReal = LocalTime.of(endMin / 60, endMin % 60).toString();
        }

        model.addAttribute("horarioMatriz", matriz);
        model.addAttribute("aulas", aulasAsignadas);
        model.addAttribute("aulaSeleccionada", idAula);
        model.addAttribute("filtro", filtro);
        model.addAttribute("docenteNombre", docente.getNombreCompleto());
        model.addAttribute("jornadaInicio", jornadaInicioReal);
        model.addAttribute("jornadaFin", jornadaFinReal);
        model.addAttribute("cursosColores", cursosColores);
        
        return "docente/horario";
    }

    @GetMapping("/docente/horario/descargar")
    public ResponseEntity<byte[]> descargarHorarioPDF(HttpSession session,
                                                      @RequestParam(required = false) Integer idAula,
                                                      @RequestParam(required = false, defaultValue = "propio") String filtro) {
        Usuario u = (Usuario) session.getAttribute("usuarioLogueado");
        if (u == null || !"DOCENTE".equalsIgnoreCase(u.getRol())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        Optional<Docente> optDocente = docenteRepository.findByUsuario_IdUsuario(u.getIdUsuario());
        if (optDocente.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        
        Docente docente = optDocente.get();
        
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
        List<Horario> horariosAula = horarioService.findHorariosActivos(aula.getIdAula(), LocalDate.now());
        
        List<Horario> horarios;
        if ("completo".equalsIgnoreCase(filtro)) {
            horarios = horariosAula;
        } else {
            horarios = horariosAula.stream()
                .filter(h -> h.getDocente().getIdDocente() == docente.getIdDocente())
                .toList();
        }
        
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

    private static final List<String> COLORES_CURSOS = List.of(
        "#a03030", "#2563eb", "#059669", "#d97706", "#7c3aed",
        "#dc2626", "#0891b2", "#ca8a04", "#9333ea", "#ea580c"
    );

    private String asignarColorCurso(String nombreCurso) {
        if (nombreCurso == null || nombreCurso.isEmpty()) return COLORES_CURSOS.get(0);
        return COLORES_CURSOS.get(Math.abs(nombreCurso.hashCode()) % COLORES_CURSOS.size());
    }

    private HorarioMatrizDTO construirMatrizHorario(List<Horario> horarios, String aulaNombre, String aulaGrado) {
        HorarioMatrizDTO matriz = new HorarioMatrizDTO(aulaNombre, aulaGrado);
        List<String> dias = matriz.getDias();

        // Organize horarios by start time
        Map<LocalTime, List<Horario>> horariosPorInicio = new java.util.LinkedHashMap<>();
        for (Horario h : horarios) {
            horariosPorInicio.computeIfAbsent(h.getHoraInicio(), k -> new ArrayList<>()).add(h);
        }

        // Build rows for each unique start time
        List<LocalTime> startTimesOrdenados = new ArrayList<>(horariosPorInicio.keySet());
        startTimesOrdenados.sort(Comparator.naturalOrder());

        LocalTime recreoInicio = jornadaConfig.getRecreoInicio();
        LocalTime recreoFin = recreoInicio.plusMinutes(jornadaConfig.getRecreoDuracion());

        for (LocalTime inicio : startTimesOrdenados) {
            List<Horario> horariosEnInicio = horariosPorInicio.get(inicio);
            LocalTime fin = horariosEnInicio.get(0).getHoraFin();
            FilaHorarioDTO fila = new FilaHorarioDTO(inicio, fin, false);

            for (String dia : dias) {
                HorarioDTO dto = null;
                for (Horario h : horariosEnInicio) {
                    if (h.getDiaSemana().equalsIgnoreCase(dia) || h.getDiaSemana().equals(dia)) {
                        dto = new HorarioDTO(
                                h.getIdHorario(),
                                h.getCurso() != null ? h.getCurso().getNombreCurso() : "N/A",
                                h.getDocente() != null ? h.getDocente().getNombreCompleto() : "N/A",
                                h.getHoraInicio(),
                                h.getHoraFin(),
                                h.getDiaSemana()
                        );
                        dto.setColor(asignarColorCurso(dto.getCursoNombre()));
                        dto.setTipo(h.getTipo() != null ? h.getTipo() : "CLASE");
                        break;
                    }
                }
                fila.asignarHorario(dia, dto);
            }

            matriz.agregarFila(fila);
            logger.info("  Fila creada: [{} - {}] {}", inicio, fin,
                horariosEnInicio.stream().map(h -> h.getCurso().getNombreCurso() + "(" + h.getDiaSemana() + ")").toList());
        }

        // Insert recreo row
        boolean insertadoRecreo = false;
        List<FilaHorarioDTO> filasConRecreo = new ArrayList<>();
        for (FilaHorarioDTO fila : matriz.getFilas()) {
            if (!insertadoRecreo && fila.getHoraInicio().isAfter(recreoInicio)) {
                filasConRecreo.add(new FilaHorarioDTO(recreoInicio, recreoFin, true));
                insertadoRecreo = true;
            }
            filasConRecreo.add(fila);
        }

        if (!insertadoRecreo && !matriz.getFilas().isEmpty()) {
            FilaHorarioDTO recreoFila = new FilaHorarioDTO(recreoInicio, recreoFin, true);
            int pos = 0;
            for (int i = 0; i < matriz.getFilas().size(); i++) {
                if (matriz.getFilas().get(i).getHoraInicio().isBefore(recreoInicio)) {
                    pos = i + 1;
                }
            }
            filasConRecreo = new ArrayList<>(matriz.getFilas());
            filasConRecreo.add(pos, recreoFila);
        }

        matriz.setFilas(filasConRecreo);
        logger.info("Matriz construida con {} filas", matriz.getFilas().size());
        return matriz;
    }
}
