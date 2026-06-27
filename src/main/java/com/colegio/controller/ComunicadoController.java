package com.colegio.controller;

import java.util.*;
import java.util.stream.Collectors;
import java.io.IOException;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import com.colegio.entity.*;
import com.colegio.repository.*;
import com.colegio.service.impl.ComunicadoService;

@Controller
@RequestMapping("/gestioncomunicados")
public class ComunicadoController {

    @Autowired
    private ComunicadoService comunicadoService;
    @Autowired
    private AulaRepository aulaRepository;
    @Autowired
    private AlumnoRepository alumnoRepository;
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private DocenteRepository docenteRepository;
    @Autowired
    private ComunicadoArchivoRepository comunicadoArchivoRepository;

    private static final String[] EXTENSIONES_PERMITIDAS = {".jpg", ".jpeg", ".png", ".gif", ".bmp", ".webp", ".pdf", ".doc", ".docx", ".xls", ".xlsx", ".ppt", ".pptx"};
    private static final long MAX_BYTES_TOTAL = 20L * 1024L * 1024L;

    @GetMapping("")
    public String index() {
        return "redirect:/gestioncomunicados/lista";
    }

    @GetMapping("/nuevo")
    public String nuevoComunicado(Model model, HttpSession session, RedirectAttributes redirectAttrs) {
        Usuario u = (Usuario) session.getAttribute("usuarioLogueado");
        if (u == null || (!"DOCENTE".equalsIgnoreCase(u.getRol()) && !"ADMINISTRADOR".equalsIgnoreCase(u.getRol()))) {
            redirectAttrs.addFlashAttribute("mensajeError", "No tienes permiso para crear comunicados.");
            return "redirect:/login";
        }
        model.addAttribute("comunicado", new Comunicado());
        model.addAttribute("aulas", aulaRepository.findAll());
        List<String> grados = aulaRepository.listarGradosDistintos();
        model.addAttribute("grados", grados);
        return "comunicados/formulario";
    }

    @GetMapping("/editar/{id}")
    public String editarComunicado(@PathVariable Integer id, Model model, HttpSession session, RedirectAttributes redirectAttrs) {
        Usuario u = (Usuario) session.getAttribute("usuarioLogueado");
        if (u == null || (!"DOCENTE".equalsIgnoreCase(u.getRol()) && !"ADMINISTRADOR".equalsIgnoreCase(u.getRol()))) {
            redirectAttrs.addFlashAttribute("mensajeError", "No tienes permiso para editar comunicados.");
            return "redirect:/login";
        }
        Comunicado c = comunicadoService.obtenerComunicadoPorId(id);
        if (c == null) {
            redirectAttrs.addFlashAttribute("mensajeError", "Comunicado no encontrado.");
            return "redirect:/gestioncomunicados/lista";
        }
        if (c.getAutor() != null && c.getAutor().getIdUsuario() != u.getIdUsuario()) {
            redirectAttrs.addFlashAttribute("mensajeError", "No puedes editar un comunicado que no te pertenece.");
            return "redirect:/gestioncomunicados/lista";
        }
        model.addAttribute("comunicado", c);
        model.addAttribute("aulas", aulaRepository.findAll());
        List<String> grados = aulaRepository.listarGradosDistintos();
        model.addAttribute("grados", grados);

        String destinoTipo = "GLOBAL";
        if (c.getAula() != null) destinoTipo = "AULA";
        else if (c.getGrado() != null && !c.getGrado().isBlank()) destinoTipo = "GRADO";
        model.addAttribute("destinoTipo", destinoTipo);

        return "comunicados/formulario";
    }

    @PostMapping("/guardar")
    public String guardar(@ModelAttribute("comunicado") Comunicado c,
                          @RequestParam(value = "destinoTipo", required = false) String destinoTipo,
                          @RequestParam(value = "fileAdjuntos", required = false) MultipartFile[] archivos,
                          HttpSession session, Model model, RedirectAttributes redirectAttrs) {
        Usuario u = (Usuario) session.getAttribute("usuarioLogueado");
        if (u == null || (!"DOCENTE".equalsIgnoreCase(u.getRol()) && !"ADMINISTRADOR".equalsIgnoreCase(u.getRol()))) {
            redirectAttrs.addFlashAttribute("mensajeError", "No tienes permiso para crear comunicados.");
            return "redirect:/login";
        }
        try {
            if (destinoTipo == null) destinoTipo = "AULA";

            if ("AULA".equalsIgnoreCase(destinoTipo)) {
                if (c.getAula() == null || c.getAula().getIdAula() == 0) {
                    throw new IllegalArgumentException("Debes seleccionar un aula.");
                }
                Aula a = aulaRepository.findById(c.getAula().getIdAula()).orElseThrow(() -> new IllegalArgumentException("Aula no encontrada."));
                c.setAula(a);
                c.setGrado(null);
            } else if ("GRADO".equalsIgnoreCase(destinoTipo)) {
                if (c.getGrado() == null || c.getGrado().isBlank()) {
                    throw new IllegalArgumentException("Debes seleccionar un grado.");
                }
                c.setAula(null);
            } else if ("GLOBAL".equalsIgnoreCase(destinoTipo)) {
                c.setAula(null);
                c.setGrado(null);
            }

            List<ComunicadoArchivo> listaArchivos = procesarArchivos(archivos, c);
            c.setArchivos(listaArchivos);

            c.setAutor(u);
            comunicadoService.crearComunicado(c);
            redirectAttrs.addFlashAttribute("mensajeExito", "Comunicado creado correctamente.");
            return "redirect:/gestioncomunicados/lista";
        } catch (Exception e) {
            model.addAttribute("mensajeError", e.getMessage());
            model.addAttribute("aulas", aulaRepository.findAll());
            model.addAttribute("grados", aulaRepository.listarGradosDistintos());
            return "comunicados/formulario";
        }
    }

    @PostMapping("/actualizar")
    public String actualizar(@ModelAttribute("comunicado") Comunicado c,
                             @RequestParam(value = "destinoTipo", required = false) String destinoTipo,
                             @RequestParam(value = "fileAdjuntos", required = false) MultipartFile[] archivos,
                             @RequestParam(value = "eliminarArchivosIds", required = false) String eliminarArchivosIds,
                             HttpSession session, Model model, RedirectAttributes redirectAttrs) {
        Usuario u = (Usuario) session.getAttribute("usuarioLogueado");
        if (u == null || (!"DOCENTE".equalsIgnoreCase(u.getRol()) && !"ADMINISTRADOR".equalsIgnoreCase(u.getRol()))) {
            redirectAttrs.addFlashAttribute("mensajeError", "No tienes permiso para editar comunicados.");
            return "redirect:/login";
        }
        try {
            if (destinoTipo == null) destinoTipo = "AULA";

            if ("AULA".equalsIgnoreCase(destinoTipo)) {
                if (c.getAula() == null || c.getAula().getIdAula() == 0) {
                    throw new IllegalArgumentException("Debes seleccionar un aula.");
                }
                Aula a = aulaRepository.findById(c.getAula().getIdAula()).orElseThrow(() -> new IllegalArgumentException("Aula no encontrada."));
                c.setAula(a);
                c.setGrado(null);
            } else if ("GRADO".equalsIgnoreCase(destinoTipo)) {
                if (c.getGrado() == null || c.getGrado().isBlank()) {
                    throw new IllegalArgumentException("Debes seleccionar un grado.");
                }
                c.setAula(null);
            } else if ("GLOBAL".equalsIgnoreCase(destinoTipo)) {
                c.setAula(null);
                c.setGrado(null);
            }

            Comunicado existente = comunicadoService.obtenerComunicadoPorId(c.getIdComunicado());
            if (existente == null) {
                throw new IllegalArgumentException("Comunicado no encontrado.");
            }
            if (existente.getAutor() != null && existente.getAutor().getIdUsuario() != u.getIdUsuario()) {
                throw new IllegalArgumentException("No puedes editar un comunicado que no te pertenece.");
            }

            existente.setTitulo(c.getTitulo());
            existente.setContenido(c.getContenido());
            existente.setAula(c.getAula());
            existente.setGrado(c.getGrado());

            if (eliminarArchivosIds != null && !eliminarArchivosIds.isBlank()) {
                Set<Integer> idsAEliminar = Arrays.stream(eliminarArchivosIds.split(","))
                        .map(String::trim).filter(s -> !s.isEmpty()).map(Integer::parseInt)
                        .collect(Collectors.toSet());
                if (existente.getArchivos() != null) {
                    existente.getArchivos().removeIf(a -> idsAEliminar.contains(a.getIdArchivo()));
                }
            }

            if (archivos != null && archivos.length > 0 && archivos[0] != null && !archivos[0].isEmpty()) {
                List<ComunicadoArchivo> nuevosArchivos = procesarArchivos(archivos, existente);
                if (existente.getArchivos() != null) {
                    existente.getArchivos().addAll(nuevosArchivos);
                } else {
                    existente.setArchivos(nuevosArchivos);
                }
            }

            comunicadoService.actualizarComunicado(existente);
            redirectAttrs.addFlashAttribute("mensajeExito", "Comunicado actualizado correctamente.");
            return "redirect:/gestioncomunicados/lista";
        } catch (Exception e) {
            model.addAttribute("mensajeError", e.getMessage());
            model.addAttribute("aulas", aulaRepository.findAll());
            model.addAttribute("grados", aulaRepository.listarGradosDistintos());
            return "comunicados/formulario";
        }
    }

    private List<ComunicadoArchivo> procesarArchivos(MultipartFile[] archivos, Comunicado comunicado) {
        List<ComunicadoArchivo> lista = new ArrayList<>();
        if (archivos == null || archivos.length == 0 || (archivos.length == 1 && archivos[0].isEmpty())) {
            return lista;
        }

        long totalBytes = 0;
        for (MultipartFile f : archivos) {
            if (f.isEmpty()) continue;
            totalBytes += f.getSize();
        }
        if (totalBytes > MAX_BYTES_TOTAL) {
            throw new IllegalArgumentException("El tamaño total de los archivos supera los 20 MB.");
        }

        for (MultipartFile f : archivos) {
            if (f.isEmpty()) continue;
            String nombreArchivo = f.getOriginalFilename();
            if (nombreArchivo == null || !nombreArchivo.contains(".")) {
                throw new IllegalArgumentException("Archivo con nombre inválido: " + nombreArchivo);
            }
            String extension = nombreArchivo.substring(nombreArchivo.lastIndexOf('.')).toLowerCase();
            boolean ok = false;
            for (String p : EXTENSIONES_PERMITIDAS) {
                if (p.equals(extension)) { ok = true; break; }
            }
            if (!ok) {
                throw new IllegalArgumentException("Tipo de archivo no permitido: " + extension + ". Solo: .jpg, .jpeg, .png, .gif, .bmp, .webp, .pdf, .doc, .docx, .xls, .xlsx, .ppt, .pptx");
            }

            try {
                ComunicadoArchivo ca = new ComunicadoArchivo();
                ca.setComunicado(comunicado);
                ca.setArchivoData(f.getBytes());
                ca.setArchivoNombre(nombreArchivo);
                ca.setArchivoTipo(f.getContentType());
                lista.add(ca);
            } catch (IOException e) {
                throw new IllegalArgumentException("Error al procesar archivo: " + e.getMessage());
            }
        }
        return lista;
    }

    @GetMapping("/lista")
    public String listaGestion(Model model) {
        List<Comunicado> comunicaciones = comunicadoService.listarTodos();

        Set<Integer> authorIds = comunicaciones.stream()
                .map(c -> c.getAutor() != null ? c.getAutor().getIdUsuario() : null)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        List<Usuario> autores = authorIds.isEmpty() ? List.of() : usuarioRepository.findAllById(authorIds);

        Map<Integer, String> autorMap = new HashMap<>();
        for (Usuario au : autores) {
            String rolePrefix = au.getRol().substring(0, 1).toUpperCase() + au.getRol().substring(1).toLowerCase();
            String label;
            if ("DOCENTE".equalsIgnoreCase(au.getRol())) {
                label = docenteRepository.findByUsuario_IdUsuario(au.getIdUsuario())
                        .map(d -> rolePrefix + " - " + d.getNombre() + " " + d.getApellido())
                        .orElse(rolePrefix + " - " + au.getNombreCompleto());
            } else {
                label = rolePrefix + " - " + au.getNombreCompleto();
            }
            autorMap.put(au.getIdUsuario(), label);
        }

        model.addAttribute("comunicados", comunicaciones);
        model.addAttribute("autorMap", autorMap);
        return "comunicados/lista";
    }

    @GetMapping("/bandeja")
    public String bandejaAlumno(HttpSession session, Model model) {
        Usuario u = (Usuario) session.getAttribute("usuarioLogueado");
        if (u == null) return "redirect:/login";
        Optional<Alumno> opt = alumnoRepository.findByUsuario_IdUsuario(u.getIdUsuario());
        if (opt.isEmpty()) {
            model.addAttribute("comunicados", List.of());
            return "comunicados/bandeja";
        }
        Alumno alumno = opt.get();
        List<Comunicado> comunicaciones = comunicadoService.listarParaAlumno(alumno);

        Set<Integer> authorIds = comunicaciones.stream()
                .map(c -> c.getAutor().getIdUsuario())
                .collect(Collectors.toSet());
        List<Usuario> autores = usuarioRepository.findAllById(authorIds);
        Map<Integer, String> autorMap = new HashMap<>();
        for (Usuario au : autores) {
            String rolePrefix = au.getRol().substring(0, 1).toUpperCase() + au.getRol().substring(1).toLowerCase();
            String label;
            if ("DOCENTE".equalsIgnoreCase(au.getRol())) {
                label = docenteRepository.findByUsuario_IdUsuario(au.getIdUsuario())
                        .map(d -> rolePrefix + " - " + d.getNombre() + " " + d.getApellido())
                        .orElse(rolePrefix + " - " + au.getNombreCompleto());
            } else {
                label = rolePrefix + " - " + au.getNombreCompleto();
            }
            autorMap.put(au.getIdUsuario(), label);
        }

        model.addAttribute("comunicados", comunicaciones);
        model.addAttribute("autorMap", autorMap);
        return "comunicados/bandeja";
    }

    @GetMapping("/vaciar-bandeja")
    public String vaciarBandeja(HttpSession session, RedirectAttributes redirectAttrs) {
        Usuario u = (Usuario) session.getAttribute("usuarioLogueado");
        if (u == null) {
            redirectAttrs.addFlashAttribute("mensajeError", "Debes iniciar sesión.");
            return "redirect:/login";
        }
        try {
            Optional<Alumno> opt = alumnoRepository.findByUsuario_IdUsuario(u.getIdUsuario());
            if (opt.isPresent()) {
                Alumno alumno = opt.get();
                List<Comunicado> comunicados = comunicadoService.listarParaAlumno(alumno);
                for (Comunicado c : comunicados) {
                    comunicadoService.eliminarComunicado(c.getIdComunicado());
                }
            }
            redirectAttrs.addFlashAttribute("mensajeExito", "Bandeja vaciada correctamente.");
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("mensajeError", "Error al vaciar la bandeja.");
        }
        return "redirect:/gestioncomunicados/bandeja";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminarMensaje(@PathVariable Integer id, HttpSession session, RedirectAttributes redirectAttrs) {
        Usuario u = (Usuario) session.getAttribute("usuarioLogueado");
        if (u == null) {
            redirectAttrs.addFlashAttribute("mensajeError", "Debes iniciar sesión.");
            return "redirect:/login";
        }
        try {
            comunicadoService.eliminarComunicado(id);
            redirectAttrs.addFlashAttribute("mensajeExito", "Mensaje eliminado correctamente.");
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("mensajeError", "Error al eliminar el mensaje.");
        }
        return "redirect:/gestioncomunicados/bandeja";
    }

    @GetMapping("/descargar/{id}")
    public ResponseEntity<Resource> descargarArchivo(@PathVariable Integer id) throws Exception {
        try {
            Comunicado com = comunicadoService.obtenerComunicadoPorId(id);
            if (com == null) return ResponseEntity.notFound().build();
            if (com.getArchivoData() == null || com.getArchivoData().length == 0) {
                return ResponseEntity.badRequest().build();
            }
            String nombreArchivo = com.getArchivoNombre();
            String tipoArchivo = com.getArchivoTipo();
            if (tipoArchivo == null || tipoArchivo.isEmpty()) {
                tipoArchivo = "application/octet-stream";
            }
            ByteArrayResource resource = new ByteArrayResource(com.getArchivoData());
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + nombreArchivo + "\"")
                    .header(HttpHeaders.CONTENT_TYPE, tipoArchivo)
                    .contentType(MediaType.parseMediaType(tipoArchivo))
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/abrir/{id}")
    public ResponseEntity<Resource> abrirArchivo(@PathVariable Integer id) throws Exception {
        try {
            Comunicado com = comunicadoService.obtenerComunicadoPorId(id);
            if (com == null) return ResponseEntity.notFound().build();
            if (com.getArchivoData() == null || com.getArchivoData().length == 0) {
                return ResponseEntity.badRequest().build();
            }
            String nombreArchivo = com.getArchivoNombre();
            String tipoArchivo = com.getArchivoTipo();
            if (tipoArchivo == null || tipoArchivo.isEmpty()) {
                tipoArchivo = "application/octet-stream";
            }
            ByteArrayResource resource = new ByteArrayResource(com.getArchivoData());
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + nombreArchivo + "\"")
                    .header(HttpHeaders.CONTENT_TYPE, tipoArchivo)
                    .contentType(MediaType.parseMediaType(tipoArchivo))
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/abrirArchivo/{id}")
    public ResponseEntity<Resource> abrirArchivoIndividual(@PathVariable Integer id) throws Exception {
        ComunicadoArchivo ca = comunicadoArchivoRepository.findById(id).orElse(null);
        if (ca == null) return ResponseEntity.notFound().build();
        byte[] data = ca.getArchivoData();
        if (data == null || data.length == 0) return ResponseEntity.badRequest().build();
        String nombre = ca.getArchivoNombre() != null ? ca.getArchivoNombre() : "archivo";
        String tipo = ca.getArchivoTipo() != null ? ca.getArchivoTipo() : "application/octet-stream";
        ByteArrayResource resource = new ByteArrayResource(data);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + nombre + "\"")
                .contentType(MediaType.parseMediaType(tipo))
                .body(resource);
    }

    @GetMapping("/descargarArchivo/{id}")
    public ResponseEntity<Resource> descargarArchivoIndividual(@PathVariable Integer id) throws Exception {
        ComunicadoArchivo ca = comunicadoArchivoRepository.findById(id).orElse(null);
        if (ca == null) return ResponseEntity.notFound().build();
        byte[] data = ca.getArchivoData();
        if (data == null || data.length == 0) return ResponseEntity.badRequest().build();
        String nombre = ca.getArchivoNombre() != null ? ca.getArchivoNombre() : "archivo";
        String tipo = ca.getArchivoTipo() != null ? ca.getArchivoTipo() : "application/octet-stream";
        ByteArrayResource resource = new ByteArrayResource(data);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + nombre + "\"")
                .contentType(MediaType.parseMediaType(tipo))
                .body(resource);
    }
}
