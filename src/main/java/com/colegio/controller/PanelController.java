package com.colegio.controller;

import com.colegio.entity.Usuario;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class PanelController {

    @GetMapping("/administrador/panel")
    public String administradorPanel(HttpSession session, RedirectAttributes redirectAttrs) {
        Usuario u = (Usuario) session.getAttribute("usuarioLogueado");
        if (u == null || !"ADMINISTRADOR".equalsIgnoreCase(u.getRol())) {
            redirectAttrs.addFlashAttribute("mensajeError", "No tienes permiso para acceder a esta página.");
            return "redirect:/login";
        }
        return "administrador/panel";
    }

    @GetMapping("/docente/panel")
    public String docentePanel(HttpSession session, RedirectAttributes redirectAttrs) {
        Usuario u = (Usuario) session.getAttribute("usuarioLogueado");
        if (u == null || !"DOCENTE".equalsIgnoreCase(u.getRol())) {
            redirectAttrs.addFlashAttribute("mensajeError", "No tienes permiso para acceder a esta página.");
            return "redirect:/login";
        }
        return "docente/panel";
    }
}