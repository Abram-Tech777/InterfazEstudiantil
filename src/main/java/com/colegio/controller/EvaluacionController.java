package com.colegio.controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.colegio.dto.NotaDTO;
import com.colegio.entity.Alumno;
import com.colegio.entity.Curso;
import com.colegio.entity.Docente;
import com.colegio.entity.EvaluacionNota;
import com.colegio.entity.Horario;
import com.colegio.entity.Usuario;
import com.colegio.entity.enums.EscalaCalificacion;
import com.colegio.repository.AlumnoRepository;
import com.colegio.repository.CursoRepository;
import com.colegio.repository.DocenteRepository;
import com.colegio.repository.EvaluacionNotaRepository;
import com.colegio.service.impl.AulaDocenteService;
import com.colegio.service.impl.HorarioService;
import com.colegio.util.ConversionNotas;

@Controller
@RequestMapping("/docente/evaluaciones")
public class EvaluacionController {

    @Autowired
    private DocenteRepository docenteRepository;
    @Autowired
    private AlumnoRepository alumnoRepository;
    @Autowired
    private CursoRepository cursoRepository;
    @Autowired
    private HorarioService horarioService;
    @Autowired
    private AulaDocenteService aulaDocenteService;
    @Autowired
    private EvaluacionNotaRepository evaluacionNotaRepository;

    @GetMapping
    public String listarEvaluaciones(HttpSession session, Model model) {
        Usuario u = (Usuario) session.getAttribute("usuarioLogueado");
        if (u == null || !"DOCENTE".equalsIgnoreCase(u.getRol())) {
            return "redirect:/login";
        }

        Optional<Docente> docenteOpt = docenteRepository.findByUsuario_IdUsuario(u.getIdUsuario());
        if (docenteOpt.isEmpty()) {
            model.addAttribute("horarios", List.of());
            return "docente/evaluaciones";
        }

        Docente d = docenteOpt.get();
        List<Integer> aulasAsignadas = aulaDocenteService.listarAulasDelDocente(d.getIdDocente());
        List<Horario> horarios = horarioService.findHorarioRepository()
            .findByDocenteAndAulasAsignadas(d.getIdDocente(), aulasAsignadas);

        model.addAttribute("horarios", horarios);
        model.addAttribute("docente", d);
        model.addAttribute("escalas", EscalaCalificacion.values());
        return "docente/evaluaciones";
    }

    @GetMapping("/abrir/{id}")
    public String abrirEvaluacion(@PathVariable("id") int idHorario,
                                  @RequestParam("bimestre") int bimestre,
                                  @RequestParam(value = "escala", defaultValue = "DECIMAL") String escala,
                                  HttpSession session,
                                  Model model) {
        Usuario u = (Usuario) session.getAttribute("usuarioLogueado");
        if (u == null || !"DOCENTE".equalsIgnoreCase(u.getRol())) {
            return "redirect:/login";
        }

        Optional<Docente> docenteOpt = docenteRepository.findByUsuario_IdUsuario(u.getIdUsuario());
        if (docenteOpt.isEmpty()) {
            return "redirect:/docente/evaluaciones";
        }

        Docente d = docenteOpt.get();
        Horario h = horarioService.getHorarioById(idHorario);
        
        if (h == null || h.getAula() == null) {
            return "redirect:/docente/evaluaciones";
        }

        List<Alumno> alumnos = alumnoRepository.findByAula_IdAula(h.getAula().getIdAula());

        Map<Integer, EvaluacionNota> notasMap = new HashMap<>();
        for (Alumno a : alumnos) {
            Optional<EvaluacionNota> notaOpt = evaluacionNotaRepository
                .findByAlumno_IdAlumnoAndCurso_IdCursoAndDocente_IdDocenteAndBimestre(
                    a.getIdAlumno(), h.getCurso().getIdCurso(), d.getIdDocente(), bimestre);
            if (notaOpt.isPresent()) {
                notasMap.put(a.getIdAlumno(), notaOpt.get());
            }
        }

        model.addAttribute("horario", h);
        model.addAttribute("docente", d);
        model.addAttribute("alumnos", alumnos);
        model.addAttribute("bimestre", bimestre);
        model.addAttribute("escala", escala);
        model.addAttribute("notasMap", notasMap);
        model.addAttribute("escalas", EscalaCalificacion.values());

        return "docente/evaluacion_detalle";
    }

    @PostMapping("/guardar")
    public String guardarEvaluacion(@RequestParam("idHorario") int idHorario,
                                    @RequestParam("bimestre") int bimestre,
                                    @RequestParam("escala") String escala,
                                    @RequestParam Map<String, String> allParams,
                                    HttpSession session,
                                    RedirectAttributes redirectAttrs) {
        Usuario u = (Usuario) session.getAttribute("usuarioLogueado");
        if (u == null || !"DOCENTE".equalsIgnoreCase(u.getRol())) {
            return "redirect:/login";
        }

        Optional<Docente> docenteOpt = docenteRepository.findByUsuario_IdUsuario(u.getIdUsuario());
        if (docenteOpt.isEmpty()) {
            redirectAttrs.addFlashAttribute("mensajeError", "Docente no encontrado.");
            return "redirect:/docente/evaluaciones";
        }

        try {
            Docente d = docenteOpt.get();
            Horario h = horarioService.getHorarioById(idHorario);

            if (h == null || h.getCurso() == null) {
                redirectAttrs.addFlashAttribute("mensajeError", "Horario o curso inválido.");
                return "redirect:/docente/evaluaciones";
            }

            List<Alumno> alumnos = alumnoRepository.findByAula_IdAula(h.getAula().getIdAula());

            for (Alumno a : alumnos) {
                String key = "nota_" + a.getIdAlumno();
                String valorStr = allParams.get(key);

                if (valorStr != null && !valorStr.isBlank()) {
                    Optional<EvaluacionNota> existingOpt = evaluacionNotaRepository
                        .findByAlumno_IdAlumnoAndCurso_IdCursoAndDocente_IdDocenteAndBimestre(
                            a.getIdAlumno(), h.getCurso().getIdCurso(), d.getIdDocente(), bimestre);

                    EvaluacionNota evaluacion;
                    if (existingOpt.isPresent()) {
                        evaluacion = existingOpt.get();
                        long diasTranscurridos = ChronoUnit.DAYS.between(
                            evaluacion.getFechaRegistro(), LocalDate.now());
                        if (diasTranscurridos > 3) {
                            redirectAttrs.addFlashAttribute("mensajeError",
                                "No puede editar la nota de " + a.getNombreCompleto()
                                + " (más de 3 días desde el registro).");
                            return "redirect:/docente/evaluaciones/abrir/" + idHorario + "?bimestre=" + bimestre + "&escala=" + escala;
                        }
                    } else {
                        evaluacion = new EvaluacionNota();
                        evaluacion.setAlumno(a);
                        evaluacion.setCurso(h.getCurso());
                        evaluacion.setDocente(d);
                        evaluacion.setBimestre(bimestre);
                        evaluacion.setFechaRegistro(LocalDate.now());
                    }

                    switch (escala.toUpperCase()) {
                        case "LETRA" -> {
                            evaluacion.setEscala("LETRA");
                            evaluacion.setNotaLiteral(valorStr.toUpperCase());
                            int notaBase = switch (valorStr.toUpperCase()) {
                                case "AD" -> 18;
                                case "A" -> 14;
                                case "B" -> 11;
                                case "C" -> 6;
                                default -> 0;
                            };
                            evaluacion.setNota(new BigDecimal(notaBase));
                            evaluacion.setNotaLogro(ConversionNotas.notaLogroFromLetra(valorStr));
                        }
                        case "LOGRO" -> {
                            evaluacion.setEscala("LOGRO");
                            evaluacion.setNotaLogro(valorStr);
                            int notaBase = switch (valorStr.toUpperCase()) {
                                case "DESTACADO" -> 18;
                                case "LOGRO ESPERADO" -> 14;
                                case "PROCESO" -> 11;
                                case "INICIO" -> 6;
                                default -> 0;
                            };
                            evaluacion.setNota(new BigDecimal(notaBase));
                            evaluacion.setNotaLiteral(ConversionNotas.notaLiteral(new BigDecimal(notaBase)));
                        }
                        default -> {
                            try {
                                BigDecimal notaVal = new BigDecimal(valorStr.trim());
                                if (notaVal.compareTo(BigDecimal.ZERO) < 0 || notaVal.compareTo(new BigDecimal("20")) > 0) {
                                    redirectAttrs.addFlashAttribute("mensajeError",
                                        "Nota para " + a.getNombreCompleto() + " debe estar entre 0 y 20.");
                                    return "redirect:/docente/evaluaciones/abrir/" + idHorario + "?bimestre=" + bimestre + "&escala=" + escala;
                                }
                                evaluacion.setEscala("DECIMAL");
                                evaluacion.setNota(notaVal);
                                evaluacion.setNotaLiteral(ConversionNotas.notaLiteral(notaVal));
                                evaluacion.setNotaLogro(ConversionNotas.notaLogro(notaVal));
                            } catch (NumberFormatException e) {
                                redirectAttrs.addFlashAttribute("mensajeError",
                                    "Formato de nota inválido para " + a.getNombreCompleto() + ".");
                                return "redirect:/docente/evaluaciones/abrir/" + idHorario + "?bimestre=" + bimestre + "&escala=" + escala;
                            }
                        }
                    }

                    evaluacionNotaRepository.save(evaluacion);
                }
            }

            redirectAttrs.addFlashAttribute("mensajeExito",
                "Notas del bimestre " + bimestre + " registradas correctamente.");
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("mensajeError",
                "Error al guardar evaluaciones: " + e.getMessage());
        }

        return "redirect:/docente/evaluaciones/abrir/" + idHorario + "?bimestre=" + bimestre + "&escala=" + escala;
    }

    // --- AJAX ---

    @PostMapping(value = "/guardar/ajax", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Map<String, Object> guardarEvaluacionAjax(@RequestParam("idHorario") int idHorario,
                                                      @RequestParam("bimestre") int bimestre,
                                                      @RequestParam("escala") String escala,
                                                      @RequestParam Map<String, String> allParams,
                                                      HttpSession session) {
        Map<String, Object> resp = new HashMap<>();
        Usuario u = (Usuario) session.getAttribute("usuarioLogueado");
        if (u == null || !"DOCENTE".equalsIgnoreCase(u.getRol())) {
            resp.put("success", false);
            resp.put("message", "No autorizado");
            return resp;
        }

        Optional<Docente> docenteOpt = docenteRepository.findByUsuario_IdUsuario(u.getIdUsuario());
        if (docenteOpt.isEmpty()) {
            resp.put("success", false);
            resp.put("message", "Docente no encontrado.");
            return resp;
        }

        try {
            Docente d = docenteOpt.get();
            Horario h = horarioService.getHorarioById(idHorario);
            if (h == null || h.getCurso() == null) {
                resp.put("success", false);
                resp.put("message", "Horario o curso inválido.");
                return resp;
            }

            List<Alumno> alumnos = alumnoRepository.findByAula_IdAula(h.getAula().getIdAula());
            int guardados = 0;

            for (Alumno a : alumnos) {
                String key = "nota_" + a.getIdAlumno();
                String valorStr = allParams.get(key);
                if (valorStr == null || valorStr.isBlank()) continue;

                Optional<EvaluacionNota> existingOpt = evaluacionNotaRepository
                    .findByAlumno_IdAlumnoAndCurso_IdCursoAndDocente_IdDocenteAndBimestre(
                        a.getIdAlumno(), h.getCurso().getIdCurso(), d.getIdDocente(), bimestre);

                EvaluacionNota evaluacion;
                if (existingOpt.isPresent()) {
                    evaluacion = existingOpt.get();
                } else {
                    evaluacion = new EvaluacionNota();
                    evaluacion.setAlumno(a);
                    evaluacion.setCurso(h.getCurso());
                    evaluacion.setDocente(d);
                    evaluacion.setBimestre(bimestre);
                    evaluacion.setFechaRegistro(LocalDate.now());
                }

                switch (escala.toUpperCase()) {
                    case "LETRA" -> {
                        evaluacion.setEscala("LETRA");
                        evaluacion.setNotaLiteral(valorStr.toUpperCase());
                        int nb = switch (valorStr.toUpperCase()) {
                            case "AD" -> 18; case "A" -> 14; case "B" -> 11; default -> 6;
                        };
                        evaluacion.setNota(new BigDecimal(nb));
                        evaluacion.setNotaLogro(ConversionNotas.notaLogroFromLetra(valorStr));
                    }
                    case "LOGRO" -> {
                        evaluacion.setEscala("LOGRO");
                        evaluacion.setNotaLogro(valorStr);
                        int nb = switch (valorStr.toUpperCase()) {
                            case "DESTACADO" -> 18; case "LOGRO ESPERADO" -> 14; case "PROCESO" -> 11; default -> 6;
                        };
                        evaluacion.setNota(new BigDecimal(nb));
                        evaluacion.setNotaLiteral(ConversionNotas.notaLiteral(new BigDecimal(nb)));
                    }
                    default -> {
                        BigDecimal notaVal = new BigDecimal(valorStr.trim());
                        if (notaVal.compareTo(BigDecimal.ZERO) < 0 || notaVal.compareTo(new BigDecimal("20")) > 0) {
                            resp.put("success", false);
                            resp.put("message", "Nota para " + a.getNombreCompleto() + " debe estar entre 0 y 20.");
                            return resp;
                        }
                        evaluacion.setEscala("DECIMAL");
                        evaluacion.setNota(notaVal);
                        evaluacion.setNotaLiteral(ConversionNotas.notaLiteral(notaVal));
                        evaluacion.setNotaLogro(ConversionNotas.notaLogro(notaVal));
                    }
                }

                evaluacionNotaRepository.save(evaluacion);
                guardados++;
            }

            resp.put("success", true);
            resp.put("message", guardados + " nota(s) registrada(s) correctamente.");
            resp.put("count", guardados);
        } catch (Exception e) {
            resp.put("success", false);
            resp.put("message", "Error: " + e.getMessage());
        }

        return resp;
    }

    @GetMapping(value = "/datos", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<NotaDTO> obtenerNotas(@RequestParam("idHorario") int idHorario,
                                       @RequestParam("bimestre") int bimestre,
                                       HttpSession session) {
        Usuario u = (Usuario) session.getAttribute("usuarioLogueado");
        if (u == null || !"DOCENTE".equalsIgnoreCase(u.getRol())) return List.of();

        Optional<Docente> docenteOpt = docenteRepository.findByUsuario_IdUsuario(u.getIdUsuario());
        if (docenteOpt.isEmpty()) return List.of();

        Docente d = docenteOpt.get();
        Horario h = horarioService.getHorarioById(idHorario);
        if (h == null || h.getCurso() == null) return List.of();

        List<Alumno> alumnos = alumnoRepository.findByAula_IdAula(h.getAula().getIdAula());
        List<NotaDTO> result = new ArrayList<>();

        for (Alumno a : alumnos) {
            Optional<EvaluacionNota> notaOpt = evaluacionNotaRepository
                .findByAlumno_IdAlumnoAndCurso_IdCursoAndDocente_IdDocenteAndBimestre(
                    a.getIdAlumno(), h.getCurso().getIdCurso(), d.getIdDocente(), bimestre);

            NotaDTO dto = new NotaDTO();
            dto.setIdAlumno(a.getIdAlumno());
            dto.setAlumnoNombre(a.getNombreCompleto());
            dto.setIdCurso(h.getCurso().getIdCurso());
            dto.setCursoNombre(h.getCurso().getNombreCurso());
            dto.setBimestre(bimestre);

            if (notaOpt.isPresent()) {
                EvaluacionNota en = notaOpt.get();
                dto.setIdNota(en.getIdNota());
                dto.setNota(en.getNota());
                dto.setNotaLiteral(en.getNotaLiteral());
                dto.setNotaLogro(en.getNotaLogro());
                dto.setEscala(en.getEscala());
                dto.setFechaRegistro(en.getFechaRegistro());
            }

            result.add(dto);
        }

        return result;
    }
}
