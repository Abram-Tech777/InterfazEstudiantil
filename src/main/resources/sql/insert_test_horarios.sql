-- Script para crear horarios de prueba para Ana Gomez (docente)
-- Los horarios se crean para el aula 1 con estudiantes

-- Horario 1: Lunes 8:00-9:00 - Matemáticas
INSERT INTO horario (id_aula, id_docente, id_curso, dia_semana, hora_inicio, hora_fin, activo, fecha_inicio, fecha_fin)
SELECT 1, d.id_docente, c.id_curso, 'LUNES', '08:00:00', '09:00:00', 1, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 1 YEAR)
FROM docente d, curso c
WHERE d.nombre_completo = 'Maria Garcia' AND c.nombre_curso = 'Matemáticas'
AND NOT EXISTS (
    SELECT 1 FROM horario h2 
    WHERE h2.id_docente = d.id_docente 
    AND h2.dia_semana = 'LUNES' 
    AND h2.hora_inicio = '08:00:00'
);

-- Horario 2: Martes 9:00-10:00 - Lenguaje
INSERT INTO horario (id_aula, id_docente, id_curso, dia_semana, hora_inicio, hora_fin, activo, fecha_inicio, fecha_fin)
SELECT 1, d.id_docente, c.id_curso, 'MARTES', '09:00:00', '10:00:00', 1, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 1 YEAR)
FROM docente d, curso c
WHERE d.nombre_completo = 'Maria Garcia' AND c.nombre_curso = 'Lenguaje'
AND NOT EXISTS (
    SELECT 1 FROM horario h2 
    WHERE h2.id_docente = d.id_docente 
    AND h2.dia_semana = 'MARTES' 
    AND h2.hora_inicio = '09:00:00'
);

-- Horario 3: Miércoles 10:00-11:00 - Ciencias
INSERT INTO horario (id_aula, id_docente, id_curso, dia_semana, hora_inicio, hora_fin, activo, fecha_inicio, fecha_fin)
SELECT 1, d.id_docente, c.id_curso, 'MIERCOLES', '10:00:00', '11:00:00', 1, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 1 YEAR)
FROM docente d, curso c
WHERE d.nombre_completo = 'Maria Garcia' AND c.nombre_curso = 'Ciencias'
AND NOT EXISTS (
    SELECT 1 FROM horario h2 
    WHERE h2.id_docente = d.id_docente 
    AND h2.dia_semana = 'MIERCOLES' 
    AND h2.hora_inicio = '10:00:00'
);

-- Horario 4: Jueves 11:00-12:00 - Historia
INSERT INTO horario (id_aula, id_docente, id_curso, dia_semana, hora_inicio, hora_fin, activo, fecha_inicio, fecha_fin)
SELECT 1, d.id_docente, c.id_curso, 'JUEVES', '11:00:00', '12:00:00', 1, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 1 YEAR)
FROM docente d, curso c
WHERE d.nombre_completo = 'Maria Garcia' AND c.nombre_curso = 'Historia'
AND NOT EXISTS (
    SELECT 1 FROM horario h2 
    WHERE h2.id_docente = d.id_docente 
    AND h2.dia_semana = 'JUEVES' 
    AND h2.hora_inicio = '11:00:00'
);
