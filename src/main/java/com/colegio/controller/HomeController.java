package com.colegio.controller;

import com.colegio.entity.Usuario;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home(HttpSession session) {
        Usuario u = (Usuario) session.getAttribute("usuarioLogueado");
        if (u == null) {
            return "redirect:/login";
        }

        String rol = u.getRol() == null ? "" : u.getRol().trim().toUpperCase();
        switch (rol) {
            case "ADMINISTRADOR":
                return "redirect:/administrador/panel";
            case "DOCENTE":
                return "redirect:/docente/panel";
            case "ESTUDIANTE":
                return "redirect:/estudiante/panel";
            default:
                return "redirect:/login";
        }
    }
}