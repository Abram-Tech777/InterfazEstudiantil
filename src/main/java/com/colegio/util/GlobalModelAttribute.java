package com.colegio.util;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
@Component
public class GlobalModelAttribute {
	 @ModelAttribute
	    public void addCurrentUri(Model model, HttpServletRequest request) {
	        // añade la URI actual como atributo 'currentUri' en TODOS los controladores/vistas
	        model.addAttribute("currentUri", request.getRequestURI());
	    }
	}