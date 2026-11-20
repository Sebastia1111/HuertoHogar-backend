-- Borra por completo las bases de HuertoHogar (todos los usuarios, productos y pedidos).
-- Ejecutar en HeidiSQL (Laragon > Base de datos) solo si quieres eliminar todo o partir de cero.
-- Si vuelves a iniciar el backend, las bases se crean de nuevo con los datos de prueba.

DROP DATABASE IF EXISTS huertohogar_usuarios;
DROP DATABASE IF EXISTS huertohogar_tienda;
