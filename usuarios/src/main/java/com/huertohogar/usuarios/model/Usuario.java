package com.huertohogar.usuarios.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "usuarios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** RUN sin puntos ni guion, ej: 190110222 */
    @Column(nullable = false, unique = true, length = 9)
    private String run;

    @Column(nullable = false, length = 50)
    private String nombre;

    @Column(length = 100)
    private String apellidos;

    @Column(nullable = false, unique = true, length = 100)
    private String correo;

    /** Contrasena cifrada con BCrypt (nunca se guarda en texto plano) */
    @Column(nullable = false, length = 100)
    private String password;

    private LocalDate fechaNacimiento;

    @Column(length = 60)
    private String region;

    @Column(length = 60)
    private String comuna;

    /** Direccion de entrega */
    @Column(length = 300)
    private String direccion;

    @Column(length = 15)
    private String telefono;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Rol rol;

    @Column(nullable = false)
    private boolean activo;

    /**
     * Version de los tokens del usuario. Al cerrar sesion o cambiar la clave se incrementa
     * y todos los access token emitidos antes dejan de ser validos en este servicio.
     */
    @Column(nullable = false)
    private int tokenVersion;

    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaRegistro;

    @PrePersist
    void alCrear() {
        if (fechaRegistro == null) {
            fechaRegistro = LocalDateTime.now();
        }
    }
}
