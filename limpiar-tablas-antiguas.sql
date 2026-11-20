-- Elimina las tablas que ya no usa el backend (versiones anteriores las creaban).
-- Hibernate (ddl-auto=update) agrega tablas y columnas, pero nunca las borra.
-- Ejecutar UNA vez en HeidiSQL (Laragon > Base de datos) o en la terminal de MySQL.
-- Alternativa: borrar las bases huertohogar_usuarios y huertohogar_tienda; se crean de nuevo al iniciar.

USE huertohogar_tienda;
DROP TABLE IF EXISTS items_carrito;
DROP TABLE IF EXISTS carritos;

USE huertohogar_usuarios;
DROP TABLE IF EXISTS refresh_tokens;
