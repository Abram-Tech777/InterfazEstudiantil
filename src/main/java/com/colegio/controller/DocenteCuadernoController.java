package com.colegio.controller;

import com.colegio.entity.*;
import com.colegio.repository.*;
import com.colegio.service.impl.ConductaService;
import com.colegio.service.impl.AulaDocenteService;
import com.colegio.service.impl.HorarioService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.Objects;

@Controller
@RequestMapping("/docente/cuaderno-control")
public class DocenteCuadernoController {

    @Autowired private DocenteRepository docenteRepository;
    @Autowired private AlumnoRepository alumnoRepository;
    @Autowired private AulaRepository aulaRepository;
    @Autowired private CursoRepository cursoRepository;
    @Autowired private HorarioService horarioService;
    @Autowired private AulaDocenteService aulaDocenteService;
    @Autowired private ConductaService conductaService;
    @Autowired private AsistenciaRepository asistenciaRepository;
    @Autowired private EvaluacionNotaRepository evaluacionNotaRepository;

    @GetMapping
    public String listarAlumnos(HttpSession session, Model model,
                                @RequestParam(required = false) Integer idAula,
                                @RequestParam(required = false) Integer idCurso,
                                @RequestParam(defaultValue = "false") boolean fragment) {
        Usuario u = (Usuario) session.getAttribute("usuarioLogueado");
        if (u == null || !"DOCENTE".equalsIgnoreCase(u.getRol())) return "redirect:/login";

        Optional<Docente> optDoc = docenteRepository.findByUsuario_IdUsuario(u.getIdUsuario());
        if (optDoc.isEmpty()) return "redirect:/login";
        Docente d = optDoc.get();

        List<Integer> aulasIds = aulaDocenteService.listarAulasDelDocente(d.getIdDocente());
        List<Aula> aulas;
        if (aulasIds != null && !aulasIds.isEmpty()) {
            aulas = aulaRepository.findAllById(aulasIds);
        } else {
            aulas = List.of();
        }

        List<Alumno> alumnos;
        if (idAula != null && idAula > 0) {
            alumnos = alumnoRepository.findByAula_IdAula(idAula);
        } else if (!aulas.isEmpty()) {
            Set<Integer> aulaIdsSet = aulas.stream().map(Aula::getIdAula).collect(Collectors.toSet());
            alumnos = new ArrayList<>();
            for (Integer aid : aulaIdsSet) {
                alumnos.addAll(alumnoRepository.findByAula_IdAula(aid));
            }
        } else {
            alumnos = List.of();
        }

        if (idCurso != null && idCurso > 0) {
            List<Integer> finalIds = new ArrayList<>();
            for (Alumno a : alumnos) {
                List<EvaluacionNota> notas = evaluacionNotaRepository.findByAlumno_IdAlumnoAndCurso_IdCursoOrderByBimestreAsc(a.getIdAlumno(), idCurso);
                if (!notas.isEmpty()) finalIds.add(a.getIdAlumno());
            }
            alumnos = alumnos.stream().filter(a -> finalIds.contains(a.getIdAlumno())).toList();
        }

        List<Curso> cursos = courseListFromAlumnos(alumnos);

        model.addAttribute("alumnos", alumnos);
        model.addAttribute("aulas", aulas);
        model.addAttribute("cursos", cursos);
        model.addAttribute("idAulaSel", idAula != null ? idAula : 0);
        model.addAttribute("idCursoSel", idCurso != null ? idCurso : 0);
        model.addAttribute("docente", d);

        if (fragment) {
            return "docente/cuaderno-control :: #tabla-container";
        }
        return "docente/cuaderno-control";
    }

    @GetMapping("/alumno/{id}")
    public String verAlumno(@PathVariable("id") int idAlumno, HttpSession session, Model model,
                            @RequestParam(defaultValue = "0") int bimestre) {
        Usuario u = (Usuario) session.getAttribute("usuarioLogueado");
        if (u == null || !"DOCENTE".equalsIgnoreCase(u.getRol())) return "redirect:/login";

        Optional<Alumno> optAl = alumnoRepository.findById(idAlumno);
        if (optAl.isEmpty()) return "redirect:/docente/cuaderno-control";
        Alumno alumno = optAl.get();

        List<Asistencia> asistencias = asistenciaRepository.findByAlumno_IdAlumnoOrderByFechaDesc(idAlumno);
        long presentes = asistencias.stream().filter(a -> "PRESENTE".equals(a.getEstado())).count();
        long retardos = asistencias.stream().filter(a -> "RETARDO".equals(a.getEstado())).count();
        long ausencias = asistencias.stream().filter(a -> "AUSENCIA".equals(a.getEstado())).count();
        int total = (int) (presentes + retardos + ausencias);
        double pctAsistencia = total > 0 ? Math.round((double) presentes / total * 100) : 0;

        List<EvaluacionNota> notas = evaluacionNotaRepository.findByAlumno_IdAlumnoOrderByFechaRegistroDesc(idAlumno);
        double promedio = 0;
        if (!notas.isEmpty()) {
            promedio = notas.stream()
                .filter(n -> n.getNota() != null)
                .mapToDouble(n -> n.getNota().doubleValue())
                .average().orElse(0);
        }

        List<Conducta> conductas = conductaService.listarPorAlumnoYBimestre(idAlumno, bimestre > 0 ? bimestre : null);
        long positivas = conductaService.contarPorAlumnoYTipoYBimestre(idAlumno, "POSITIVA", bimestre > 0 ? bimestre : null);
        long negativas = conductaService.contarPorAlumnoYTipoYBimestre(idAlumno, "NEGATIVA", bimestre > 0 ? bimestre : null);

        model.addAttribute("alumno", alumno);
        model.addAttribute("asistencias", asistencias);
        model.addAttribute("presentes", presentes);
        model.addAttribute("retardos", retardos);
        model.addAttribute("ausencias", ausencias);
        model.addAttribute("total", total);
        model.addAttribute("pctAsistencia", pctAsistencia);
        model.addAttribute("promedio", Math.round(promedio * 100.0) / 100.0);
        model.addAttribute("conductas", conductas);
        model.addAttribute("positivas", positivas);
        model.addAttribute("negativas", negativas);
        model.addAttribute("bimestreSel", bimestre);
        model.addAttribute("bimestres", List.of(1, 2, 3, 4));
        return "docente/cuaderno-alumno";
    }

    @PostMapping("/anotacion/crear")
    public String crearAnotacion(@RequestParam int idAlumno,
                                 @RequestParam String tipo,
                                 @RequestParam String titulo,
                                 @RequestParam(required = false) String descripcion,
                                 @RequestParam(required = false) String observaciones,
                                 @RequestParam(defaultValue = "0") int bimestre,
                                 @RequestParam(defaultValue = "lista") String origen,
                                 HttpSession session,
                                 RedirectAttributes ra) {
        Usuario u = (Usuario) session.getAttribute("usuarioLogueado");
        if (u == null || !"DOCENTE".equalsIgnoreCase(u.getRol())) return "redirect:/login";
        Optional<Docente> optDoc = docenteRepository.findByUsuario_IdUsuario(u.getIdUsuario());
        if (optDoc.isEmpty()) return "redirect:/login";
        Optional<Alumno> optAl = alumnoRepository.findById(idAlumno);
        if (optAl.isEmpty()) return "redirect:/login";

        Conducta c = new Conducta();
        c.setAlumno(optAl.get());
        c.setDocente(optDoc.get());
        c.setTipo(tipo);
        c.setTitulo(titulo);
        c.setDescripcion(descripcion);
        c.setObservaciones(observaciones);
        c.setBimestre(bimestre > 0 ? bimestre : 1);
        c.setAnio(LocalDate.now().getYear());
        if ("POSITIVA".equals(tipo)) {
            c.setIcono("fa-star");
            c.setColorIcono("#22c55e");
        } else {
            c.setIcono("fa-exclamation-triangle");
            c.setColorIcono("#ef4444");
        }
        conductaService.guardar(c);
        ra.addFlashAttribute("mensajeExito", "Anotaci\u00f3n registrada correctamente");
        if ("alumno".equals(origen)) {
            return "redirect:/docente/cuaderno-control/alumno/" + idAlumno + "?bimestre=" + bimestre;
        }
        return "redirect:/docente/cuaderno-control";
    }

    @PostMapping("/anotacion/actualizar")
    public String actualizarAnotacion(@RequestParam int idConducta,
                                      @RequestParam String titulo,
                                      @RequestParam(required = false) String descripcion,
                                      @RequestParam(required = false) String observaciones,
                                      @RequestParam(defaultValue = "0") int bimestre,
                                      HttpSession session,
                                      RedirectAttributes ra) {
        Usuario u = (Usuario) session.getAttribute("usuarioLogueado");
        if (u == null || !"DOCENTE".equalsIgnoreCase(u.getRol())) return "redirect:/login";

        Conducta c = conductaService.obtenerPorId(idConducta);
        if (c == null) return "redirect:/docente/cuaderno-control";
        c.setTitulo(titulo);
        c.setDescripcion(descripcion);
        c.setObservaciones(observaciones);
        if (bimestre > 0) c.setBimestre(bimestre);
        conductaService.actualizar(c);
        ra.addFlashAttribute("mensajeExito", "Anotaci\u00f3n actualizada");
        return "redirect:/docente/cuaderno-control/alumno/" + c.getAlumno().getIdAlumno() + "?bimestre=" + bimestre;
    }

    @GetMapping("/anotacion/eliminar/{id}")
    public String eliminarAnotacion(@PathVariable("id") int idConducta, HttpSession session, RedirectAttributes ra) {
        Usuario u = (Usuario) session.getAttribute("usuarioLogueado");
        if (u == null || !"DOCENTE".equalsIgnoreCase(u.getRol())) return "redirect:/login";
        Conducta c = conductaService.obtenerPorId(idConducta);
        if (c != null) {
            int idAlumno = c.getAlumno().getIdAlumno();
            conductaService.eliminar(idConducta);
            ra.addFlashAttribute("mensajeExito", "Anotaci\u00f3n eliminada");
            return "redirect:/docente/cuaderno-control/alumno/" + idAlumno;
        }
        return "redirect:/docente/cuaderno-control";
    }

    private List<Curso> courseListFromAlumnos(List<Alumno> alumnos) {
        return alumnos.stream()
            .map(Alumno::getAula)
            .filter(Objects::nonNull)
            .distinct()
            .flatMap(aula -> horarioService.findHorariosActivos(aula.getIdAula(), LocalDate.now()).stream())
            .map(Horario::getCurso)
            .filter(Objects::nonNull)
            .distinct()
            .collect(Collectors.toList());
    }
}
