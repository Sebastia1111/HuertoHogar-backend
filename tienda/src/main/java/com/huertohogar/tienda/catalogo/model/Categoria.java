package com.huertohogar.tienda.catalogo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Frutas Frescas, Verduras Organicas, Productos Organicos, Productos Lacteos. */
@Entity
@Table(name = "categorias")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Categoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Identificador que usa el frontend: frutas, verduras, organicos, lacteos */
    @Column(nullable = false, unique = true, length = 30)
    private String slug;

    /** Prefijo de los codigos de producto: FR, VR, PO, PL */
    @Column(nullable = false, unique = true, length = 5)
    private String codigo;

    @Column(nullable = false, unique = true, length = 60)
    private String nombre;

    @Column(length = 1000)
    private String descripcion;
}
