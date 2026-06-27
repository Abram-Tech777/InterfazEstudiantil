package com.colegio.controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.io.IOException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.colegio.entity.Alumno;
import com.colegio.entity.Comunicado;
import com.colegio.entity.Docente;
import com.colegio.entity.Mensaje;
import com.colegio.entity.MensajeArchivo;
import com.colegio.entity.Usuario;
import com.colegio.repository.AlumnoRepository;
import com.colegio.repository.AulaDocenteRepository;
import com.colegio.repository.ComunicadoRepository;
import com.colegio.repository.DocenteRepository;
import com.colegio.repository.MensajeArchivoRepository;
import com.colegio.repository.UsuarioRepository;
import com.colegio.service.impl.MensajeService;

@Controller
@RequestMapping("/mensajes")
public class MensajeController {

    @Autowired
    private MensajeService mensajeService;
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private DocenteRepository docenteRepository;
    @Autowired
    private AulaDocenteRepository aulaDocenteRepository;
    @Autowired
    private AlumnoRepository alumnoRepository;
    @Autowired
    private MensajeArchivoRepository mensajeArchivoRepository;
    @Autowired
    private ComunicadoRepository comunicadoRepository;

    @GetMapping("/nuevo")
    public String nuevoMensaje(Model model, @RequestParam(value = "to", required = false) Integer to,
                                HttpSession session) {
        Usuario remitente = (Usuario) session.getAttribute("usuarioLogueado");
        Mensaje mensaje = new Mensaje();
        if (to != null) {
            usuarioRepository.findById(to).ifPresent(usuario -> mensaje.setDestinatario(usuario));
        }
        model.addAttribute("mensaje", mensaje);

        List<Usuario> todos = usuarioRepository.findAll().stream()
                .filter(u -> !u.getIdUsuario().equals(remitente.getIdUsuario()))
                .collect(Collectors.toList());

        if (remitente != null && "ESTUDIANTE".equalsIgnoreCase(remitente.getRol())) {
            List<Usuario> docentes = todos.stream()
                    .filter(u -> "DOCENTE".equalsIgnoreCase(u.getRol()))
                    .collect(Collectors.toList());
            model.addAttribute("docenteList", docentes);
            model.addAttribute("adminList", List.of());
            model.addAttribute("docentes", docentes);
        } else if (remitente != null && ("DOCENTE".equalsIgnoreCase(remitente.getRol()) || "ADMINISTRADOR".equalsIgnoreCase(remitente.getRol()))) {
            List<Usuario> docentes = todos.stream()
                    .filter(u -> "DOCENTE".equalsIgnoreCase(u.getRol()))
                    .collect(Collectors.toList());
            List<Usuario> admins = todos.stream()
                    .filter(u -> "ADMINISTRADOR".equalsIgnoreCase(u.getRol()))
                    .collect(Collectors.toList());
            List<Usuario> todosJuntos = new ArrayList<>();
            todosJuntos.addAll(docentes);
            todosJuntos.addAll(admins);
            model.addAttribute("docenteList", docentes);
            model.addAttribute("adminList", admins);
            model.addAttribute("docentes", todosJuntos);
        } else {
            model.addAttribute("docenteList", List.of());
            model.addAttribute("adminList", List.of());
            model.addAttribute("docentes", List.of());
        }

        return "mensajes/nuevo";
    }

    @PostMapping("/enviar")
    public Object enviarMensaje(Mensaje mensaje,
                                @RequestParam(value = "files", required = false) MultipartFile[] archivos,
                                @RequestParam(value = "replyToId", required = false) Integer replyToId,
                                HttpSession session, HttpServletRequest request,
                                RedirectAttributes redirectAttrs) {
        Usuario u = (Usuario) session.getAttribute("usuarioLogueado");
        if (u == null) {
            if ("XMLHttpRequest".equals(request.getHeader("X-Requested-With"))) {
                return ResponseEntity.status(401).body(Map.of("error", "Debes iniciar sesión."));
            }
            redirectAttrs.addFlashAttribute("mensajeError", "Debes iniciar sesión para enviar mensajes.");
            return "redirect:/login";
        }
        boolean isAjax = "XMLHttpRequest".equals(request.getHeader("X-Requested-With"));

        try {
            mensaje.setRemitente(u);

            if (replyToId != null) {
                Mensaje padre = mensajeService.obtenerPorId(replyToId, u.getIdUsuario());
                if (padre != null) {
                    mensaje.setMensajePadre(padre);
                    if (mensaje.getAsunto() == null || mensaje.getAsunto().isBlank()) {
                        mensaje.setAsunto("Re: " + padre.getAsunto());
                    }
                }
            }

            if (archivos != null && archivos.length > 0) {
                long totalSize = 0;
                List<MensajeArchivo> listaArchivos = new ArrayList<>();
                for (MultipartFile f : archivos) {
                    if (f == null || f.isEmpty()) continue;
                    totalSize += f.getSize();
                    if (totalSize > 20 * 1024 * 1024) {
                        throw new IllegalArgumentException("El tamaño total de los archivos supera los 20 MB.");
                    }
                    MensajeArchivo ma = new MensajeArchivo();
                    ma.setArchivoData(f.getBytes());
                    ma.setArchivoNombre(f.getOriginalFilename());
                    ma.setArchivoTipo(f.getContentType());
                    ma.setMensaje(mensaje);
                    listaArchivos.add(ma);
                }
                mensaje.setArchivos(listaArchivos);
            }

            Mensaje saved = mensajeService.enviarMensaje(mensaje);

            if (isAjax) {
                DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm a");
                Map<String, Object> result = new java.util.LinkedHashMap<>();
                result.put("idMensaje", saved.getIdMensaje());
                result.put("contenido", saved.getContenido());
                result.put("asunto", saved.getAsunto());
                result.put("fechaEnvio", saved.getFechaEnvio() != null ? saved.getFechaEnvio().format(fmt) : "");
                result.put("remitenteId", saved.getRemitente().getIdUsuario());
                result.put("remitenteNombre", saved.getRemitente().getNombreCompleto());
                List<Map<String, Object>> archivosJson = new ArrayList<>();
                if (saved.getArchivos() != null) {
                    for (MensajeArchivo ma : saved.getArchivos()) {
                        Map<String, Object> am = new java.util.HashMap<>();
                        am.put("idArchivo", ma.getIdArchivo());
                        am.put("archivoNombre", ma.getArchivoNombre());
                        am.put("archivoTipo", ma.getArchivoTipo());
                        archivosJson.add(am);
                    }
                }
                result.put("archivos", archivosJson);
                return ResponseEntity.ok(result);
            }

            redirectAttrs.addFlashAttribute("mensajeExito", "Mensaje enviado correctamente.");
        } catch (Exception e) {
            if (isAjax) {
                return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
            }
            redirectAttrs.addFlashAttribute("mensajeError", e.getMessage());
        }
        return "redirect:/mensajes/inbox";
    }

    @GetMapping("/inbox")
    public String inbox(HttpSession session, Model model) {
        Usuario u = (Usuario) session.getAttribute("usuarioLogueado");
        if (u == null) return "redirect:/login";
        List<Mensaje> inbox = mensajeService.listarInbox(u.getIdUsuario());
        model.addAttribute("inbox", inbox);
        model.addAttribute("currentUserId", u.getIdUsuario());
        return "mensajes/inbox";
    }

    @PostMapping("/eliminar/{id}")
    public String eliminarMensaje(@PathVariable int id, HttpSession session, RedirectAttributes redirectAttrs) {
        Usuario u = (Usuario) session.getAttribute("usuarioLogueado");
        if (u == null) return "redirect:/login";
        try {
            mensajeService.eliminarMensaje(id, u.getIdUsuario());
            redirectAttrs.addFlashAttribute("mensajeExito", "Mensaje eliminado.");
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("mensajeError", e.getMessage());
        }
        return "redirect:/mensajes/inbox";
    }

    @PostMapping("/vaciar-inbox")
    public String vaciarInbox(HttpSession session, RedirectAttributes redirectAttrs) {
        Usuario u = (Usuario) session.getAttribute("usuarioLogueado");
        if (u == null) return "redirect:/login";
        try {
            mensajeService.vaciarInbox(u.getIdUsuario());
            redirectAttrs.addFlashAttribute("mensajeExito", "Bandeja vaciada.");
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("mensajeError", e.getMessage());
        }
        return "redirect:/mensajes/inbox";
    }

    @PostMapping("/eliminar-chat/{conUserId}")
    public String eliminarChat(@PathVariable int conUserId, HttpSession session, RedirectAttributes redirectAttrs) {
        Usuario u = (Usuario) session.getAttribute("usuarioLogueado");
        if (u == null) return "redirect:/login";
        try {
            mensajeService.eliminarConversacion(u.getIdUsuario(), conUserId);
            redirectAttrs.addFlashAttribute("mensajeExito", "Conversación eliminada.");
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("mensajeError", e.getMessage());
        }
        return "redirect:/mensajes/contactos";
    }

    @GetMapping("/contactos")
    public String contactos(HttpSession session, Model model) {
        Usuario u = (Usuario) session.getAttribute("usuarioLogueado");
        if (u == null) return "redirect:/login";
        List<Usuario> contactos = mensajeService.listarContactos(u.getIdUsuario());
        if ("ADMINISTRADOR".equalsIgnoreCase(u.getRol())) {
            contactos = contactos.stream()
                    .filter(c -> "DOCENTE".equalsIgnoreCase(c.getRol()) || "ADMINISTRADOR".equalsIgnoreCase(c.getRol()))
                    .collect(Collectors.toList());
        }
        model.addAttribute("contactos", contactos);
        model.addAttribute("currentUserId", u.getIdUsuario());
        Map<Integer, Long> noLeidos = mensajeService.contarNoLeidosPorContacto(u.getIdUsuario());
        model.addAttribute("noLeidos", noLeidos);
        return "mensajes/contactos";
    }

    @GetMapping("/chat/{conUserId}")
    public String chat(@PathVariable int conUserId, HttpSession session, Model model,
                       @RequestParam(value = "refTitulo", required = false) String refTitulo,
                       @RequestParam(value = "refId", required = false) Integer refId) {
        Usuario u = (Usuario) session.getAttribute("usuarioLogueado");
        if (u == null) return "redirect:/login";
        Usuario contacto = usuarioRepository.findById(conUserId).orElse(null);
        if (contacto == null) return "redirect:/mensajes/inbox";
        List<Mensaje> conversacion = mensajeService.obtenerConversacion(u.getIdUsuario(), conUserId);
        model.addAttribute("contacto", contacto);
        model.addAttribute("conversacion", conversacion);
        model.addAttribute("currentUserId", u.getIdUsuario());
        model.addAttribute("refTitulo", refTitulo);
        model.addAttribute("refId", refId);
        String enlaceAnuncio = "DOCENTE".equalsIgnoreCase(u.getRol()) ? "/gestioncomunicados/lista" : "/gestioncomunicados/bandeja";
        model.addAttribute("enlaceAnuncio", enlaceAnuncio);
        String lastCheck = conversacion.stream()
                .map(Mensaje::getFechaEnvio)
                .filter(java.util.Objects::nonNull)
                .max(java.time.LocalDateTime::compareTo)
                .map(java.time.LocalDateTime::toString)
                .orElse(java.time.LocalDateTime.now().minusMinutes(5).toString());
        model.addAttribute("lastCheck", lastCheck);
        return "mensajes/chat";
    }

    @PostMapping("/chat/enviar")
    public Object enviarChat(@RequestParam("destinatarioId") int destinatarioId,
                             @RequestParam("contenido") String contenido,
                             @RequestParam(value = "files", required = false) MultipartFile[] archivos,
                             @RequestParam(value = "refId", required = false) Integer refId,
                             HttpSession session, HttpServletRequest request) {
        Usuario u = (Usuario) session.getAttribute("usuarioLogueado");
        if (u == null) return "redirect:/login";

        boolean isAjax = "XMLHttpRequest".equals(request.getHeader("X-Requested-With"));

        try {
            Usuario destinatario = usuarioRepository.findById(destinatarioId)
                    .orElseThrow(() -> new IllegalArgumentException("Destinatario no encontrado."));
            Mensaje m = new Mensaje();
            m.setRemitente(u);
            m.setDestinatario(destinatario);
            m.setContenido(contenido);

            if (refId != null) {
                comunicadoRepository.findById(refId).ifPresent(c -> m.setComunicadoReferencia(c));
            }

            if (archivos != null && archivos.length > 0) {
                long totalSize = 0;
                List<MensajeArchivo> listaArchivos = new ArrayList<>();
                for (MultipartFile f : archivos) {
                    if (f == null || f.isEmpty()) continue;
                    totalSize += f.getSize();
                    if (totalSize > 20 * 1024 * 1024) {
                        throw new IllegalArgumentException("El tamaño total de los archivos supera los 20 MB.");
                    }
                    MensajeArchivo ma = new MensajeArchivo();
                    ma.setArchivoData(f.getBytes());
                    ma.setArchivoNombre(f.getOriginalFilename());
                    ma.setArchivoTipo(f.getContentType());
                    ma.setMensaje(m);
                    listaArchivos.add(ma);
                }
                m.setArchivos(listaArchivos);
            }

            Mensaje saved = mensajeService.enviarMensaje(m);

            if (isAjax) {
                DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm a");
                Map<String, Object> result = new java.util.HashMap<>();
                result.put("id", saved.getIdMensaje());
                result.put("contenido", saved.getContenido());
                result.put("fechaEnvio", saved.getFechaEnvio() != null ? saved.getFechaEnvio().format(fmt) : "");
                result.put("remitenteId", u.getIdUsuario());
                result.put("remitenteNombre", u.getNombreCompleto());

                if (saved.getComunicadoReferencia() != null) {
                    result.put("refId", saved.getComunicadoReferencia().getIdComunicado());
                    result.put("refTitulo", saved.getComunicadoReferencia().getTitulo());
                }

                List<Map<String, Object>> archivosJson = new ArrayList<>();
                if (saved.getArchivos() != null) {
                    for (MensajeArchivo ma : saved.getArchivos()) {
                        Map<String, Object> am = new java.util.HashMap<>();
                        am.put("idArchivo", ma.getIdArchivo());
                        am.put("archivoNombre", ma.getArchivoNombre());
                        am.put("archivoTipo", ma.getArchivoTipo());
                        archivosJson.add(am);
                    }
                }
                result.put("archivos", archivosJson);

                return ResponseEntity.ok(result);
            }

            return "redirect:/mensajes/chat/" + destinatarioId;
        } catch (Exception e) {
            if (isAjax) {
                return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
            }
            return "redirect:/mensajes/chat/" + destinatarioId;
        }
    }

    @GetMapping("/descargar/{id}")
    public ResponseEntity<Resource> descargarArchivo(@PathVariable int id, HttpSession session) {
        Usuario u = (Usuario) session.getAttribute("usuarioLogueado");
        if (u == null) return ResponseEntity.status(401).build();
        try {
            Mensaje m = mensajeService.obtenerPorId(id, u.getIdUsuario());
            if (m == null || m.getArchivos() == null || m.getArchivos().isEmpty())
                return ResponseEntity.badRequest().build();
            MensajeArchivo ma = m.getArchivos().get(0);
            String tipo = ma.getArchivoTipo() != null ? ma.getArchivoTipo() : "application/octet-stream";
            ByteArrayResource resource = new ByteArrayResource(ma.getArchivoData());
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + ma.getArchivoNombre() + "\"")
                    .contentType(MediaType.parseMediaType(tipo))
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/abrir/{id}")
    public ResponseEntity<Resource> abrirArchivo(@PathVariable int id, HttpSession session) {
        Usuario u = (Usuario) session.getAttribute("usuarioLogueado");
        if (u == null) return ResponseEntity.status(401).build();
        try {
            Mensaje m = mensajeService.obtenerPorId(id, u.getIdUsuario());
            if (m == null || m.getArchivos() == null || m.getArchivos().isEmpty())
                return ResponseEntity.badRequest().build();
            MensajeArchivo ma = m.getArchivos().get(0);
            String tipo = ma.getArchivoTipo() != null ? ma.getArchivoTipo() : "application/octet-stream";
            ByteArrayResource resource = new ByteArrayResource(ma.getArchivoData());
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + ma.getArchivoNombre() + "\"")
                    .contentType(MediaType.parseMediaType(tipo))
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/descargarArchivo/{idArchivo}")
    public ResponseEntity<Resource> descargarArchivoPorId(@PathVariable int idArchivo, HttpSession session) {
        Usuario u = (Usuario) session.getAttribute("usuarioLogueado");
        if (u == null) return ResponseEntity.status(401).build();
        try {
            MensajeArchivo ma = mensajeArchivoRepository.findById(idArchivo).orElse(null);
            if (ma == null) return ResponseEntity.badRequest().build();
            String tipo = ma.getArchivoTipo() != null ? ma.getArchivoTipo() : "application/octet-stream";
            ByteArrayResource resource = new ByteArrayResource(ma.getArchivoData());
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + ma.getArchivoNombre() + "\"")
                    .contentType(MediaType.parseMediaType(tipo))
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/abrirArchivo/{idArchivo}")
    public ResponseEntity<Resource> abrirArchivoPorId(@PathVariable int idArchivo, HttpSession session) {
        Usuario u = (Usuario) session.getAttribute("usuarioLogueado");
        if (u == null) return ResponseEntity.status(401).build();
        try {
            MensajeArchivo ma = mensajeArchivoRepository.findById(idArchivo).orElse(null);
            if (ma == null) return ResponseEntity.badRequest().build();
            String tipo = ma.getArchivoTipo() != null ? ma.getArchivoTipo() : "application/octet-stream";
            ByteArrayResource resource = new ByteArrayResource(ma.getArchivoData());
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + ma.getArchivoNombre() + "\"")
                    .contentType(MediaType.parseMediaType(tipo))
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/chat/nuevos/{conUserId}")
    @ResponseBody
    public ResponseEntity<?> chatNuevos(@PathVariable int conUserId,
                                         @RequestParam("after") String after,
                                         HttpSession session) {
        Usuario u = (Usuario) session.getAttribute("usuarioLogueado");
        if (u == null) return ResponseEntity.status(401).body(Map.of("error", "No autenticado"));
        try {
            LocalDateTime afterDate = LocalDateTime.parse(after, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            List<Mensaje> nuevos = mensajeService.obtenerNuevosDesde(u.getIdUsuario(), conUserId, afterDate);
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm a");
            List<Map<String, Object>> result = new ArrayList<>();
            for (Mensaje m : nuevos) {
                if (m.getRemitente().getIdUsuario().intValue() == u.getIdUsuario().intValue()) continue;
                Map<String, Object> item = new java.util.HashMap<>();
                item.put("id", m.getIdMensaje());
                item.put("contenido", m.getContenido());
                item.put("fechaEnvio", m.getFechaEnvio() != null ? m.getFechaEnvio().format(fmt) : "");
                item.put("fechaIso", m.getFechaEnvio() != null ? m.getFechaEnvio().toString() : "");
                item.put("remitenteId", m.getRemitente().getIdUsuario());
                item.put("remitenteNombre", m.getRemitente().getNombreCompleto());
                if (m.getComunicadoReferencia() != null) {
                    item.put("refId", m.getComunicadoReferencia().getIdComunicado());
                    item.put("refTitulo", m.getComunicadoReferencia().getTitulo());
                }
                List<Map<String, Object>> archivosJson = new ArrayList<>();
                if (m.getArchivos() != null) {
                    for (MensajeArchivo ma : m.getArchivos()) {
                        Map<String, Object> am = new java.util.HashMap<>();
                        am.put("idArchivo", ma.getIdArchivo());
                        am.put("archivoNombre", ma.getArchivoNombre());
                        am.put("archivoTipo", ma.getArchivoTipo());
                        archivosJson.add(am);
                    }
                }
                item.put("archivos", archivosJson);
                result.add(item);
            }
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/no-leidos")
    @ResponseBody
    public ResponseEntity<?> noLeidos(HttpSession session) {
        Usuario u = (Usuario) session.getAttribute("usuarioLogueado");
        if (u == null) return ResponseEntity.status(401).body(Map.of("error", "No autenticado"));
        long count = mensajeService.contarNoLeidos(u.getIdUsuario());
        return ResponseEntity.ok(Map.of("count", count));
    }

    @GetMapping("/ver/{id}")
    @ResponseBody
    public ResponseEntity<?> verMensaje(@PathVariable int id, HttpSession session) {
        Usuario u = (Usuario) session.getAttribute("usuarioLogueado");
        if (u == null) return ResponseEntity.status(401).body("No autenticado");
        try {
            Mensaje m = mensajeService.obtenerPorId(id, u.getIdUsuario());
            if (m == null) return ResponseEntity.badRequest().body("Mensaje no encontrado.");
            if (m.getDestinatario() != null && m.getDestinatario().getIdUsuario().intValue() == u.getIdUsuario().intValue()) {
                mensajeService.marcarLeido(id, u.getIdUsuario());
            }
            // Build response manually to include respuestas (ignored by @JsonIgnore)
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm a");
            Map<String, Object> result = new java.util.LinkedHashMap<>();
            result.put("idMensaje", m.getIdMensaje());
            result.put("asunto", m.getAsunto());
            result.put("contenido", m.getContenido());
            result.put("fechaEnvio", m.getFechaEnvio() != null ? m.getFechaEnvio().format(fmt) : "");
            result.put("leido", m.getLeido());
            result.put("remitente", Map.of("idUsuario", m.getRemitente().getIdUsuario(), "nombreCompleto", m.getRemitente().getNombreCompleto(), "rol", m.getRemitente().getRol()));
            result.put("destinatario", m.getDestinatario() != null ? Map.of("idUsuario", m.getDestinatario().getIdUsuario(), "nombreCompleto", m.getDestinatario().getNombreCompleto(), "rol", m.getDestinatario().getRol()) : null);
            List<Map<String, Object>> archivosJson = new ArrayList<>();
            if (m.getArchivos() != null) {
                for (MensajeArchivo ma : m.getArchivos()) {
                    Map<String, Object> am = new java.util.HashMap<>();
                    am.put("idArchivo", ma.getIdArchivo());
                    am.put("archivoNombre", ma.getArchivoNombre());
                    am.put("archivoTipo", ma.getArchivoTipo());
                    archivosJson.add(am);
                }
            }
            result.put("archivos", archivosJson);
            List<Map<String, Object>> respuestasJson = new ArrayList<>();
            List<Mensaje> respuestas = mensajeService.obtenerRespuestas(id);
            if (respuestas != null) {
                for (Mensaje r : respuestas) {
                    Map<String, Object> rm = new java.util.LinkedHashMap<>();
                    rm.put("idMensaje", r.getIdMensaje());
                    rm.put("contenido", r.getContenido());
                    rm.put("fechaEnvio", r.getFechaEnvio() != null ? r.getFechaEnvio().format(fmt) : "");
                    rm.put("remitenteId", r.getRemitente().getIdUsuario());
                    rm.put("remitenteNombre", r.getRemitente().getNombreCompleto());
                    List<Map<String, Object>> rArchivos = new ArrayList<>();
                    if (r.getArchivos() != null) {
                        for (MensajeArchivo ra : r.getArchivos()) {
                            Map<String, Object> ram = new java.util.HashMap<>();
                            ram.put("idArchivo", ra.getIdArchivo());
                            ram.put("archivoNombre", ra.getArchivoNombre());
                            ram.put("archivoTipo", ra.getArchivoTipo());
                            rArchivos.add(ram);
                        }
                    }
                    rm.put("archivos", rArchivos);
                    respuestasJson.add(rm);
                }
            }
            result.put("respuestas", respuestasJson);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(403).body(e.getMessage());
        }
    }
}
