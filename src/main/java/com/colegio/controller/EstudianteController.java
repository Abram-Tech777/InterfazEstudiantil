package com.colegio.controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.colegio.dto.FilaHorarioDTO;
import com.colegio.dto.HorarioDTO;
import com.colegio.dto.HorarioMatrizDTO;
import com.colegio.dto.NotaDTO;
import com.colegio.entity.Alumno;
import com.colegio.entity.Asistencia;
import com.colegio.entity.Comunicado;
import com.colegio.entity.Curso;
import com.colegio.entity.EvaluacionNota;
import com.colegio.entity.Horario;
import com.colegio.entity.Usuario;
import com.colegio.repository.AlumnoRepository;
import com.colegio.repository.AsistenciaRepository;
import com.colegio.repository.EvaluacionNotaRepository;
import com.colegio.service.HorarioPDFService;
import com.colegio.service.impl.HorarioService;
import com.colegio.repository.*;
import com.colegio.util.JornadaConfig;

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
    @Autowired
    private JornadaConfig jornadaConfig;

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
        
        LinkedHashMap<String, String> cursosColores = new LinkedHashMap<>();
        for (Horario h : horarios) {
            String nombreCurso = h.getCurso() != null ? h.getCurso().getNombreCurso() : "N/A";
            if (!cursosColores.containsKey(nombreCurso)) {
                cursosColores.put(nombreCurso, asignarColorCurso(nombreCurso));
            }
        }
        
        model.addAttribute("horarioMatriz", matriz);
        model.addAttribute("cursosColores", cursosColores);
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

        Map<LocalTime, List<Horario>> horariosPorInicio = new LinkedHashMap<>();
        for (Horario h : horarios) {
            horariosPorInicio.computeIfAbsent(h.getHoraInicio(), k -> new ArrayList<>()).add(h);
        }

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
        }

        // Insert RECREO row
        boolean tieneDespuesDeRecreo = startTimesOrdenados.stream().anyMatch(t -> !t.isBefore(recreoFin));
        if (tieneDespuesDeRecreo) {
            FilaHorarioDTO recreoFila = new FilaHorarioDTO(recreoInicio, recreoFin, true);
            List<FilaHorarioDTO> filasActuales = matriz.getFilas();
            int insertIndex = 0;
            for (int i = 0; i < filasActuales.size(); i++) {
                if (!filasActuales.get(i).isRecreo() && filasActuales.get(i).getHoraInicio().isBefore(recreoInicio)) {
                    insertIndex = i + 1;
                }
            }
            filasActuales.add(insertIndex, recreoFila);
        }

        return matriz;
    }

    @GetMapping("/estudiante/notas")
    public String verNotas(HttpSession session, Model model) {
        Usuario u = (Usuario) session.getAttribute("usuarioLogueado");
        if (u == null || !"ESTUDIANTE".equalsIgnoreCase(u.getRol())) return "redirect:/login";
        
        Optional<Alumno> opt = alumnoRepository.findByUsuario_IdUsuario(u.getIdUsuario());
        if (opt.isEmpty()) {
            model.addAttribute("cursos", List.of());
            model.addAttribute("notas", List.of());
            return "estudiante/notas";
        }
        
        Alumno alumno = opt.get();
        model.addAttribute("alumno", alumno);
        
        List<Curso> cursos = evaluacionNotaRepository.findCursosByAlumnoId(alumno.getIdAlumno());
        model.addAttribute("cursos", cursos);
        
        List<EvaluacionNota> notas = evaluacionNotaRepository.findByAlumno_IdAlumnoOrderByFechaRegistroDesc(alumno.getIdAlumno());
        model.addAttribute("notas", notas);
        return "estudiante/notas";
    }

    // ==========================================
    // AJAX - NOTAS FILTRADAS
    // ==========================================
    @GetMapping(value = "/estudiante/notas/filtrar", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<NotaDTO> filtrarNotas(@RequestParam(value = "curso", required = false) Integer idCurso,
                                       @RequestParam(value = "bimestre", required = false) Integer bimestre,
                                       HttpSession session) {
        Usuario u = (Usuario) session.getAttribute("usuarioLogueado");
        if (u == null || !"ESTUDIANTE".equalsIgnoreCase(u.getRol())) return List.of();

        Optional<Alumno> opt = alumnoRepository.findByUsuario_IdUsuario(u.getIdUsuario());
        if (opt.isEmpty()) return List.of();

        Alumno alumno = opt.get();
        List<EvaluacionNota> notas;

        if (idCurso != null && bimestre != null && bimestre > 0) {
            notas = evaluacionNotaRepository.findByAlumno_IdAlumnoAndCurso_IdCursoAndBimestre(
                alumno.getIdAlumno(), idCurso, bimestre);
        } else if (idCurso != null) {
            notas = evaluacionNotaRepository.findByAlumno_IdAlumnoAndCurso_IdCursoOrderByBimestreAsc(
                alumno.getIdAlumno(), idCurso);
        } else if (bimestre != null && bimestre > 0) {
            List<Curso> cursos = evaluacionNotaRepository.findCursosByAlumnoId(alumno.getIdAlumno());
            notas = new ArrayList<>();
            for (Curso c : cursos) {
                notas.addAll(evaluacionNotaRepository.findByAlumno_IdAlumnoAndCurso_IdCursoAndBimestre(
                    alumno.getIdAlumno(), c.getIdCurso(), bimestre));
            }
        } else {
            notas = evaluacionNotaRepository.findByAlumno_IdAlumnoOrderByFechaRegistroDesc(alumno.getIdAlumno());
        }

        return notas.stream().map(this::toNotaDTO).collect(Collectors.toList());
    }

    private NotaDTO toNotaDTO(EvaluacionNota en) {
        NotaDTO dto = new NotaDTO();
        dto.setIdNota(en.getIdNota());
        dto.setIdAlumno(en.getAlumno().getIdAlumno());
        dto.setAlumnoNombre(en.getAlumno().getNombreCompleto());
        dto.setIdCurso(en.getCurso().getIdCurso());
        dto.setCursoNombre(en.getCurso().getNombreCurso());
        dto.setIdDocente(en.getDocente().getIdDocente());
        dto.setDocenteNombre(en.getDocente().getNombre());
        dto.setBimestre(en.getBimestre());
        dto.setNota(en.getNota());
        dto.setNotaLiteral(en.getNotaLiteral());
        dto.setNotaLogro(en.getNotaLogro());
        dto.setEscala(en.getEscala());
        dto.setFechaRegistro(en.getFechaRegistro());
        if (en.getNota() != null) {
            if (en.getNota().compareTo(new BigDecimal("14")) >= 0) dto.setEstado("Aprobado");
            else if (en.getNota().compareTo(new BigDecimal("11")) >= 0) dto.setEstado("En proceso");
            else dto.setEstado("En inicio");
        }
        return dto;
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