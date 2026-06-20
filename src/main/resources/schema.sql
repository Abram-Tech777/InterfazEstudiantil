-- Migración: Permitir NULL en id_aula de comunicado
ALTER TABLE comunicado MODIFY COLUMN id_aula INT NULL;
