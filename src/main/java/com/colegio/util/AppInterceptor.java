package com.colegio.util;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.colegio.entity.Usuario;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Configuration 
public class AppInterceptor implements HandlerInterceptor, WebMvcConfigurer {


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
    	String uri = request.getRequestURI();
        HttpSession session = request.getSession();
        

        if (uri.endsWith("/login") || uri.contains("/logout")) {
            return true;
        }
        

        Usuario usuarioLogueado = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuarioLogueado == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return false; 
        }
        

        if (uri.contains("/gestionusuarios") && !"ADMINISTRADOR".equalsIgnoreCase(usuarioLogueado.getRol())) {
            session.setAttribute("error", "No tienes permisos de Administrador para entrar aquí.");
            response.sendRedirect(request.getContextPath() + "/login");
            return false;
        }
        
        return true;
    }


    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(this) 
                .addPathPatterns("/gestionusuarios/**") 
                .excludePathPatterns("/login", "/logout"); 
    }
}
