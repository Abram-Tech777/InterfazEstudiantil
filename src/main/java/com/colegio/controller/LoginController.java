package com.colegio.controller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.colegio.entity.Usuario;
import com.colegio.service.impl.UsuarioService;

import jakarta.servlet.http.HttpSession;

@Controller
public class LoginController {
	
	
   @Autowired
    private  UsuarioService usuarioService;

    public LoginController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping("/login")
    public String mostrarLogin() {
        return "login/login";
    }

    @PostMapping("/login")
    public String autenticar(@RequestParam("codUsuario") String codUsuario, 
                             @RequestParam("password") String password,
                             HttpSession session, 
                             RedirectAttributes redirectAttrs) {
        
        Optional<Usuario> usuarioOpt = usuarioService.buscarPorCodigo(codUsuario);
       
        if (usuarioOpt.isPresent() && usuarioOpt.get().getPassword().equals(password)) {
            
            Usuario usuario = usuarioOpt.get();
            
            if ("INACTIVO".equals(usuario.getEstado())) {
                redirectAttrs.addFlashAttribute("error", "Esta cuenta se encuentra inhabilitada.");
                return "redirect:/login";
            }
            
            session.setAttribute("usuarioLogueado", usuario);
            
            if ("ADMINISTRADOR".equalsIgnoreCase(usuario.getRol())) {
                return "redirect:/gestionusuarios";
            } else if ("DOCENTE".equalsIgnoreCase(usuario.getRol())) {
                return "redirect:/docente/panel";
            } else {
                return "redirect:/estudiante/panel";
            }

        } else {
            redirectAttrs.addFlashAttribute("error", "Código de usuario o contraseña incorrectos.");
            return "redirect:/login";
        }
    }

    @PostMapping("/logout")
    public String logoutPost(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}
