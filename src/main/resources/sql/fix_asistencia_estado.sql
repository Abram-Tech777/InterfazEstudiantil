-- Actualizar registros sin estado a PRESENTE por defecto
UPDATE asistencia SET estado = 'PRESENTE' WHERE estado IS NULL OR estado = '';
