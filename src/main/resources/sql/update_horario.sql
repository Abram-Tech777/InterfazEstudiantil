-- Script para agregar campos de vigencia a tabla horario
USE BD_COLEGIO;

ALTER TABLE horario ADD COLUMN IF NOT EXISTS fecha_inicio DATE DEFAULT CURDATE();
ALTER TABLE horario ADD COLUMN IF NOT EXISTS fecha_fin DATE DEFAULT DATE_ADD(CURDATE(), INTERVAL 365 DAY);
ALTER TABLE horario ADD COLUMN IF NOT EXISTS activo TINYINT(1) DEFAULT 1;

SELECT 'Campos agregados exitosamente' as status;
