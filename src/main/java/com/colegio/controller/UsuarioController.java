package com.colegio.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.colegio.entity.Usuario;
import com.colegio.service.impl.UsuarioService;

@Controller
@RequestMapping("/gestionusuarios") 
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping
    public String listarUsuarios(Model model) {
        List<Usuario> lista = usuarioService.listarTodos();
        model.addAttribute("usuarios", lista);
        return "usuarios/lista"; 
    }


    @GetMapping("/nuevo")
    public String mostrarFormularioNuevo(Model model) {
        model.addAttribute("usuario", new Usuario());
        return "usuarios/formulario"; 
    }


    @PostMapping("/guardar")
    public String guardarUsuario(@ModelAttribute("usuario") Usuario usuario, RedirectAttributes redirectAttrs) {
        try {
            if (usuario.getIdUsuario() == 0) {
                usuarioService.guardarUsuario(usuario);
                redirectAttrs.addFlashAttribute("mensajeExito", "¡Usuario registrado con éxito!");
            } else {
                usuarioService.actualizarUsuario(usuario.getIdUsuario(), usuario);
                redirectAttrs.addFlashAttribute("mensajeExito", "¡Usuario actualizado con éxito!");
            }
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("mensajeError", "Error al procesar la solicitud: " + e.getMessage());
            return "redirect:/gestionusuarios";
        }
        return "redirect:/gestionusuarios";
    }

    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditar(@PathVariable("id") int id, Model model) {
        Usuario usuario = usuarioService.obtenerUsuarioPorId(id);
        model.addAttribute("usuario", usuario);
        return "usuarios/formulario";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminarUsuario(@PathVariable("id") int id, RedirectAttributes redirectAttrs) {
        try {
            usuarioService.eliminarUsuario(id);
            redirectAttrs.addFlashAttribute("mensajeExito", "Usuario inhabilitado correctamente.");
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("mensajeError", "No se pudo inhabilitar el usuario.");
        }
        return "redirect:/gestionusuarios";
    }
}