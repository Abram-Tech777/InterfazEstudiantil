package com.colegio;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import com.colegio.util.JornadaConfig;

@SpringBootApplication
@EnableConfigurationProperties(JornadaConfig.class)
public class InterfazEstudiantilApplication {

	public static void main(String[] args) {
		SpringApplication.run(InterfazEstudiantilApplication.class, args);
	}

}
