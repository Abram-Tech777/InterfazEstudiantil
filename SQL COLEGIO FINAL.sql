DROP DATABASE IF EXISTS BD_COLEGIO;
CREATE DATABASE BD_COLEGIO;
USE BD_COLEGIO;

CREATE TABLE usuario (
    id_usuario INT AUTO_INCREMENT PRIMARY KEY,
    cod_usuario VARCHAR(50) NOT NULL UNIQUE,
    nombre_completo VARCHAR(100) NOT NULL,
    password VARCHAR(255) NOT NULL,
    rol VARCHAR(30) NOT NULL,
    estado VARCHAR(15) NOT NULL DEFAULT 'ACTIVO'
);

CREATE TABLE docente (
    id_docente INT AUTO_INCREMENT PRIMARY KEY,
    nombre_completo VARCHAR(100),
    id_usuario INT UNIQUE,
    FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario)
);

CREATE TABLE aula (
    id_aula INT AUTO_INCREMENT PRIMARY KEY,
    grado VARCHAR(20) NOT NULL,
    seccion VARCHAR(5) NOT NULL,
    capacidad INT NOT NULL,
    UNIQUE(grado, seccion)
);

CREATE TABLE curso (
    id_curso INT AUTO_INCREMENT PRIMARY KEY,
    nombre_curso VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE alumno (
    id_alumno INT AUTO_INCREMENT PRIMARY KEY,
    nombre_completo VARCHAR(100),
    id_aula INT NULL,
    id_usuario INT UNIQUE,
    FOREIGN KEY (id_aula) REFERENCES aula(id_aula),
    FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario)
);

CREATE TABLE horario (
    id_horario INT AUTO_INCREMENT PRIMARY KEY,
    id_aula INT NOT NULL,
    id_docente INT NOT NULL,
    id_curso INT NOT NULL,
    dia_semana VARCHAR(15) NOT NULL,
    hora_inicio TIME NOT NULL,
    hora_fin TIME NOT NULL,
    fecha_inicio DATE DEFAULT (CURDATE()),
    fecha_fin DATE DEFAULT (DATE_ADD(CURDATE(), INTERVAL 365 DAY)),
    activo TINYINT DEFAULT 1,
    FOREIGN KEY (id_aula) REFERENCES aula(id_aula),
    FOREIGN KEY (id_docente) REFERENCES docente(id_docente),
    FOREIGN KEY (id_curso) REFERENCES curso(id_curso),
    CONSTRAINT UQ_aula_tiempo UNIQUE (id_aula, dia_semana, hora_inicio),
    CONSTRAINT UQ_docente_tiempo UNIQUE (id_docente, dia_semana, hora_inicio)
);

CREATE TABLE aula_docente (
    id_aula_docente INT AUTO_INCREMENT PRIMARY KEY,
    id_docente INT NOT NULL,
    id_aula INT NOT NULL,
    rol VARCHAR(30) DEFAULT 'DOCENTE',
    activo TINYINT DEFAULT 1,
    creado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_auladocente_docente FOREIGN KEY (id_docente) REFERENCES docente(id_docente),
    CONSTRAINT fk_auladocente_aula FOREIGN KEY (id_aula) REFERENCES aula(id_aula),
    UNIQUE KEY uk_auladocente_docente_aula (id_docente, id_aula)
);

CREATE TABLE evaluacion_nota (
    id_nota INT AUTO_INCREMENT PRIMARY KEY,
    id_alumno INT NOT NULL,
    id_curso INT NOT NULL,
    id_docente INT NOT NULL,
    bimestre INT NOT NULL,
    nota DECIMAL(4,2) NOT NULL CHECK (nota >= 0 AND nota <= 20),
    fecha_registro DATE NOT NULL,
    tipo_evaluacion VARCHAR(50),
    FOREIGN KEY (id_alumno) REFERENCES alumno(id_alumno),
    FOREIGN KEY (id_curso) REFERENCES curso(id_curso),
    FOREIGN KEY (id_docente) REFERENCES docente(id_docente)
);

CREATE TABLE asistencia (
    id_asistencia INT AUTO_INCREMENT PRIMARY KEY,
    id_alumno INT NOT NULL,
    id_curso INT NOT NULL,
    id_horario INT NOT NULL,
    fecha DATE NOT NULL,
    hora_llegada TIME NULL,
    estado VARCHAR(15) NOT NULL,
    observaciones VARCHAR(255) NULL,
    FOREIGN KEY (id_alumno) REFERENCES alumno(id_alumno),
    FOREIGN KEY (id_curso) REFERENCES curso(id_curso),
    FOREIGN KEY (id_horario) REFERENCES horario(id_horario)
);

CREATE TABLE comunicado (
    id_comunicado INT AUTO_INCREMENT PRIMARY KEY,
    id_usuario INT NOT NULL,
    id_aula INT NULL,   
    grado VARCHAR(50) NULL,
    titulo VARCHAR(150) NOT NULL,
    contenido TEXT NOT NULL,
    fecha_emision DATETIME DEFAULT CURRENT_TIMESTAMP,
    ruta_adjunto VARCHAR(500) NULL,
    archivo_data LONGBLOB NULL,
    archivo_nombre VARCHAR(255) NULL,
    archivo_tipo VARCHAR(100) NULL,
    CONSTRAINT fk_comunicado_usuario FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario),
    FOREIGN KEY (id_aula) REFERENCES aula(id_aula)
);

CREATE TABLE incidencia (
    id_incidencia INT AUTO_INCREMENT PRIMARY KEY,
    id_alumno INT NOT NULL,
    id_reporta INT NOT NULL,
    categoria VARCHAR(30) NOT NULL,
    descripcion TEXT NOT NULL,
    fecha_registro DATE NOT NULL,
    documento_descargo VARCHAR(255) NULL,
    FOREIGN KEY (id_alumno) REFERENCES alumno(id_alumno),
    FOREIGN KEY (id_reporta) REFERENCES usuario(id_usuario)
);

CREATE TABLE reconocimiento (
    id_reconocimiento INT AUTO_INCREMENT PRIMARY KEY,
    id_alumno INT NOT NULL,
    titulo VARCHAR(100) NOT NULL,
    icono VARCHAR(50),
    color_fondo VARCHAR(20),
    color_texto VARCHAR(20),
    FOREIGN KEY (id_alumno) REFERENCES alumno(id_alumno)
);

CREATE TABLE conducta (
    id_conducta INT AUTO_INCREMENT PRIMARY KEY,
    id_alumno INT NOT NULL,
    titulo VARCHAR(100) NOT NULL,
    descripcion TEXT,
    icono VARCHAR(50),
    color_icono VARCHAR(20),
    fecha_registro DATE,
    FOREIGN KEY (id_alumno) REFERENCES alumno(id_alumno)
);

INSERT INTO usuario (cod_usuario, nombre_completo, password, rol, estado) VALUES ('C2026001', 'Falcao Eduardo', '123456', 'ADMINISTRADOR', 'ACTIVO');
INSERT INTO usuario (cod_usuario, nombre_completo, password, rol, estado) VALUES ('D2026001', 'Maria Garcia Lopez', '123456', 'DOCENTE', 'ACTIVO');
INSERT INTO usuario (cod_usuario, nombre_completo, password, rol, estado) VALUES ('D2026002', 'Juan Perez Rodriguez', '123456', 'DOCENTE', 'ACTIVO');
INSERT INTO usuario (cod_usuario, nombre_completo, password, rol, estado) VALUES ('D2026003', 'Carlos Martinez Sanchez', '123456', 'DOCENTE', 'ACTIVO');
INSERT INTO usuario (cod_usuario, nombre_completo, password, rol, estado) VALUES ('E2026001', 'Juan Perez', '456', 'ESTUDIANTE', 'ACTIVO');
INSERT INTO usuario (cod_usuario, nombre_completo, password, rol, estado) VALUES ('E2026002', 'Ana Rodriguez Castro', '123456', 'ESTUDIANTE', 'ACTIVO');
INSERT INTO usuario (cod_usuario, nombre_completo, password, rol, estado) VALUES ('E2026003', 'Miguel Lopez Vargas', '123456', 'ESTUDIANTE', 'ACTIVO');
INSERT INTO usuario (cod_usuario, nombre_completo, password, rol, estado) VALUES ('E2026004', 'Sofia Torres Ruiz', '123456', 'ESTUDIANTE', 'ACTIVO');
INSERT INTO usuario (cod_usuario, nombre_completo, password, rol, estado) VALUES ('E2026005', 'Diego Flores Mendoza', '123456', 'ESTUDIANTE', 'ACTIVO');
INSERT INTO usuario (cod_usuario, nombre_completo, password, rol, estado) VALUES ('E2026006', 'Elena Gomez Peralta', '123456', 'ESTUDIANTE', 'ACTIVO');
INSERT INTO usuario (cod_usuario, nombre_completo, password, rol, estado) VALUES ('E2026007', 'Mateo Silva Rojas', '123456', 'ESTUDIANTE', 'ACTIVO');

INSERT INTO aula (grado, seccion, capacidad) VALUES ('1ro Secundaria', 'A', 35);
INSERT INTO aula (grado, seccion, capacidad) VALUES ('2do Secundaria', 'A', 35);
INSERT INTO aula (grado, seccion, capacidad) VALUES ('3ro Secundaria', 'A', 35);
INSERT INTO aula (grado, seccion, capacidad) VALUES ('4to Secundaria', 'A', 35);
INSERT INTO aula (grado, seccion, capacidad) VALUES ('5to Secundaria', 'A', 35);
INSERT INTO aula (grado, seccion, capacidad) VALUES ('1ro Secundaria', 'B', 30);

INSERT INTO docente (nombre_completo, id_usuario) VALUES ('Maria Garcia Lopez', 2);
INSERT INTO docente (nombre_completo, id_usuario) VALUES ('Juan Perez Rodriguez', 3);
INSERT INTO docente (nombre_completo, id_usuario) VALUES ('Carlos Martinez Sanchez', 4);

INSERT INTO alumno (nombre_completo, id_usuario, id_aula) VALUES ('Juan Perez', 5, 1);
INSERT INTO alumno (nombre_completo, id_usuario, id_aula) VALUES ('Ana Rodriguez Castro', 6, 1);
INSERT INTO alumno (nombre_completo, id_usuario, id_aula) VALUES ('Miguel Lopez Vargas', 7, 2);
INSERT INTO alumno (nombre_completo, id_usuario, id_aula) VALUES ('Sofia Torres Ruiz', 8, 3);
INSERT INTO alumno (nombre_completo, id_usuario, id_aula) VALUES ('Diego Flores Mendoza', 9, 4);
INSERT INTO alumno (nombre_completo, id_usuario, id_aula) VALUES ('Elena Gomez Peralta', 10, 5);
INSERT INTO alumno (nombre_completo, id_usuario, id_aula) VALUES ('Mateo Silva Rojas', 11, 6);

INSERT INTO curso (nombre_curso) VALUES ('Matemática');
INSERT INTO curso (nombre_curso) VALUES ('Lenguaje');
INSERT INTO curso (nombre_curso) VALUES ('Ciencias Naturales');
INSERT INTO curso (nombre_curso) VALUES ('Historia');
INSERT INTO curso (nombre_curso) VALUES ('Educación Física');

INSERT INTO horario (id_aula, id_docente, id_curso, dia_semana, hora_inicio, hora_fin) VALUES (1, 1, 1, 'Lunes', '08:00:00', '09:00:00');
INSERT INTO horario (id_aula, id_docente, id_curso, dia_semana, hora_inicio, hora_fin) VALUES (1, 2, 2, 'Lunes', '09:00:00', '10:00:00');
INSERT INTO horario (id_aula, id_docente, id_curso, dia_semana, hora_inicio, hora_fin) VALUES (1, 3, 3, 'Martes', '08:00:00', '09:00:00');
INSERT INTO horario (id_aula, id_docente, id_curso, dia_semana, hora_inicio, hora_fin) VALUES (1, 1, 4, 'Miercoles', '10:00:00', '11:00:00');
INSERT INTO horario (id_aula, id_docente, id_curso, dia_semana, hora_inicio, hora_fin) VALUES (1, 2, 5, 'Jueves', '11:00:00', '12:00:00');
INSERT INTO horario (id_aula, id_docente, id_curso, dia_semana, hora_inicio, hora_fin) VALUES (2, 1, 1, 'Lunes', '09:30:00', '10:30:00');
INSERT INTO horario (id_aula, id_docente, id_curso, dia_semana, hora_inicio, hora_fin) VALUES (2, 2, 2, 'Martes', '10:00:00', '11:00:00');
INSERT INTO horario (id_aula, id_docente, id_curso, dia_semana, hora_inicio, hora_fin) VALUES (3, 3, 3, 'Viernes', '08:00:00', '09:00:00');

SET SQL_SAFE_UPDATES = 0;
UPDATE horario SET dia_semana = 'Miercoles' WHERE dia_semana = 'Miércoles';
SET SQL_SAFE_UPDATES = 1;

SELECT '========== CURSOS CREADOS ==========' as verificacion;
SELECT * FROM curso;

SELECT '========== HORARIOS CREADOS ==========' as verificacion;
SELECT h.id_horario, h.dia_semana, h.hora_inicio, h.hora_fin, a.grado as aula_grado, c.nombre_curso, d.nombre_completo as docente FROM horario h JOIN aula a ON h.id_aula = a.id_aula JOIN curso c ON h.id_curso = c.id_curso JOIN docente d ON h.id_docente = d.id_docente ORDER BY h.dia_semana, h.hora_inicio;

SELECT * FROM usuario;

INSERT INTO comunicado (id_usuario, id_aula, grado, titulo, contenido, fecha_emision) VALUES
(1, 1, '1ro Secundaria', 'Reunión de Padres', 'Reunión vía Zoom este viernes a las 6pm.', NOW()),
(1, 1, '1ro Secundaria', 'Feria de Ciencias', 'Traer materiales para el proyecto de Robótica.', NOW() - INTERVAL 1 DAY),
(1, 1, '1ro Secundaria', 'Torneo Deportivo', 'Inscripciones abiertas en secretaría hasta mañana.', NOW() - INTERVAL 2 DAY),
(1, 1, '1ro Secundaria', 'Taller de Arte', 'Inscripciones para pintura al óleo, cupos limitados.', NOW() - INTERVAL 3 DAY),
(1, 1, '1ro Secundaria', 'Cierre de Notas', 'Revisar plataforma antes de las 6pm para verificar promedios.', NOW() - INTERVAL 4 DAY);

INSERT INTO evaluacion_nota (id_alumno, id_curso, id_docente, bimestre, nota, fecha_registro) VALUES
(1, 1, 1, 1, 15.00, '2026-06-20'), -- Alumno 1, Curso 1, Docente 1, Bimestre 1, Nota 15
(1, 2, 2, 1, 18.00, '2026-06-21'), -- Alumno 1, Curso 2, Docente 2, Bimestre 1, Nota 18
(1, 3, 3, 1, 09.50, '2026-06-22'), -- Alumno 1, Curso 3, Docente 3, Bimestre 1, Nota 9.5
(1, 4, 1, 1, 14.00, '2026-06-23'), -- Alumno 1, Curso 4, Docente 1, Bimestre 1, Nota 14
(1, 5, 2, 1, 12.00, '2026-06-24'); -- Alumno 1, Curso 5, Docente 2, Bimestre 1, Nota 12

SELECT '========== NOTAS REGISTRADAS ==========' as verificacion;
SELECT n.id_nota, a.nombre_completo as alumno, c.nombre_curso, n.nota, n.bimestre 
FROM evaluacion_nota n
JOIN alumno a ON n.id_alumno = a.id_alumno
JOIN curso c ON n.id_curso = c.id_curso;

INSERT INTO aula_docente (id_docente, id_aula, rol, activo)VALUES (id_juan, id_aula_asignada, 'DOCENTE', 1);

select * from usuario;
select *  from alumno;