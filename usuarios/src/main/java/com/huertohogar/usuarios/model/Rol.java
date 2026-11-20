package com.huertohogar.usuarios.model;

/**
 * Roles del sistema HuertoHogar.
 * CLIENTE: compra (catalogo, carrito, perfil).
 * VENDEDOR: ademas gestiona productos del catalogo/inventario.
 * ADMIN: todo, incluida la gestion de usuarios.
 */
public enum Rol {
    ADMIN,
    VENDEDOR,
    CLIENTE
}
