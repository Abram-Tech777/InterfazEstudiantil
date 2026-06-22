-- Agregar columna hora_llegada a la tabla asistencia
ALTER TABLE asistencia ADD COLUMN IF NOT EXISTS hora_llegada TIME NULL;
