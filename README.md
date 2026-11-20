# HuertoHogar-backend

Backend de la tienda online **HuertoHogar** (DSY1104 – Desarrollo Fullstack II, EP3): microservicios **Spring Boot 4.1 + Spring Cloud Eureka + MySQL (Laragon)**, con la API que consume el frontend **HuertoHogar-Moderno** (React + Vite).

## Arquitectura

| Servicio | Puerto | Base de datos (MySQL) | Responsabilidad |
| --- | --- | --- | --- |
| `eureka-server` | 8761 | — | Registro y descubrimiento de servicios |
| `usuarios` | 8082 | `huertohogar_usuarios` | Registro, login JWT, refresh token, sesión (`/me`, logout), perfil y CRUD de usuarios |
| `tienda` | 8083 | `huertohogar_tienda` | Inventario, catálogo, checkout, pedidos/boletas e historial de stock |

```
React + Vite (5173)
  ├─ /api/auth, /api/usuarios ─────────► usuarios :8082 ──► MySQL huertohogar_usuarios
  └─ /api/tienda, /api/inventario ─────► tienda   :8083 ──► MySQL huertohogar_tienda
                                            │  (checkout) pregunta a Eureka dónde está "usuarios"
                                            └──────────────► usuarios /api/auth/me (dirección de entrega)
usuarios y tienda se registran en eureka-server :8761
```

- **usuarios emite el JWT** y **tienda solo lo verifica** con la misma clave (`jwt.secret`), sin llamar a usuarios en cada petición.
- Cada microservicio tiene **su propia base de datos**. Los pedidos guardan el `usuarioId` del token (sin FK entre bases). El carrito vive en el navegador y se envía completo al comprar.
- El paquete `inventario` original de tienda (`Producto`, `ProductoRepository`, `ProductoService`, `ProductoController`) **no se modificó**. Los datos extra del caso (código, categoría, origen, stock mínimo, etc.) están en `ProductoCatalogo`, una ficha 1 a 1 con `Producto`.

## Puesta en marcha (Windows + Laragon)

1. Abre **Laragon** → *Iniciar todo* (MySQL en el puerto 3306, usuario `root` sin clave). **Las bases y tablas se crean solas** al arrancar cada servicio, junto con los 9 productos y las cuentas de prueba.
2. Haz **doble clic en `iniciar.bat`**. Revisa Java y MySQL y abre una ventana por servicio (Eureka 8761, usuarios 8082, tienda 8083). Detecta solo la versión de Java instalada (21 o superior). Para detener todo, haz **doble clic en `detener.bat`**: cierra los 3 servicios y los procesos de Gradle que quedan en segundo plano, y deja la carpeta libre para moverla o borrarla.

   **El orden no importa y Eureka es opcional:** si Eureka está apagado, usuarios y tienda funcionan igual (tienda usa la dirección de respaldo `app.usuarios.url`) y se registran solos cuando Eureka arranca.

   De forma manual, cada uno en su terminal y en cualquier orden:
   ```powershell
   cd eureka-server; .\gradlew.bat bootRun
   cd usuarios;      .\gradlew.bat bootRun
   cd tienda;        .\gradlew.bat bootRun
   ```
3. Swagger: abre <http://localhost:8082> (usuarios) o <http://localhost:8083> (tienda); ambos redirigen a Swagger.
   - La portada de cada Swagger trae la guía de 3 pasos. El login ya viene con el ejemplo `admin@huertohogar.cl` / `12345`: *Try it out* → *Execute*, copia el `accessToken` y pégalo en **Authorize**. El token queda guardado aunque recargues.
   - Los grupos están numerados en el orden de uso. Usuarios: 1. Sesión · 2. Mi perfil · 3. Usuarios (ADMIN). Tienda: 1. Catálogo (público) · 2. Compras y pedidos · 3. Inventario (ADMIN / VENDEDOR) · 4. Categorías.
   - Cada operación dice quién puede usarla (`público`, `con token`, `ADMIN o VENDEDOR`, `solo ADMIN`), y todos los formularios traen datos de ejemplo.

Otra clave o puerto de MySQL: variables `DB_URL`, `DB_USER` y `DB_PASSWORD`. Sin MySQL: `.\gradlew.bat bootRun --args='--spring.profiles.active=h2'`.

## Cuentas de prueba (clave `12345`)

| Correo | Rol | Puede |
| --- | --- | --- |
| `admin@huertohogar.cl` | ADMIN | Todo, incluido eliminar y gestionar usuarios |
| `vendedor@huertohogar.cl` | VENDEDOR | Productos, stock y estados de pedidos (no eliminar) |
| `cliente@huertohogar.cl` | CLIENTE | Comprar, ver sus pedidos y su perfil |

El registro público solo acepta `@gmail.com` y `@duocuc.cl` (igual que el frontend). Los correos `@duocuc.cl` tienen **20 % de descuento** en la boleta (y en el carrito del frontend).

## Seguridad

- Contraseñas con **BCrypt**. Access token **JWT de 15 min** y refresh token **JWT de 7 días**, firmados con HMAC-SHA. **Ningún token se guarda en la base de datos**; el refresh token no sirve como access token (claim `tipo`).
- **Logout** (`POST /api/auth/logout` con el token) y cambio de clave: suben `tokenVersion` del usuario, así que todos los tokens anteriores (access y refresh) dejan de servir en usuarios. En tienda, un access token antiguo expira en 15 minutos como máximo.
- Respuestas de error en el formato que lee el frontend: `{ status, error, message, detalles }`. `401` = sin token o token inválido (`message: "TOKEN_EXPIRADO"` si expiró); `403` = el rol no tiene permiso.
- Un cliente solo opera con **sus** datos: el `correo` que envía el frontend se compara con el del token.

## Endpoints

### usuarios (8082)

| Método | Ruta | Acceso | Descripción |
| --- | --- | --- | --- |
| POST | `/api/auth/registro` | Público | `{run, nombre, apellidos, correo, clave, fechaNacimiento?, region, comuna, direccion}` → tokens |
| POST | `/api/auth/login` | Público | `{correo, clave}` → `{accessToken, refreshToken, tokenType, expiresIn, usuario}` |
| POST | `/api/auth/refresh` | Público | `{refreshToken}` → par nuevo de tokens |
| GET | `/api/auth/me` | Token | Usuario de la sesión (el frontend revalida al recargar) |
| POST | `/api/auth/logout` | Token | Cierra sesión (204) |
| GET / PUT | `/api/usuarios/me` | Token | Ver / editar perfil (dirección de entrega, teléfono, región, comuna) |
| PUT | `/api/usuarios/me/password` | Token | `{claveActual, claveNueva}` |
| GET / POST | `/api/usuarios` | ADMIN | Listar (`?rol=`) / crear con cualquier rol |
| GET / PUT / DELETE | `/api/usuarios/{id}` | ADMIN | Obtener / editar (rol, activo) / eliminar |

### tienda (8083) – inventario

| Método | Ruta | Acceso | Descripción |
| --- | --- | --- | --- |
| GET | `/api/inventario/productos?q=&categoria=&soloDisponibles=&stockBajo=` | Público | Listar con filtros |
| GET | `/api/inventario/productos/{id}` · `/productos/codigo/{FR001}` | Público | Detalle |
| POST / PUT | `/api/inventario/productos[/{id}]` | ADMIN, VENDEDOR | Crear / editar (registra el movimiento de stock) |
| PATCH | `/api/inventario/productos/{id}/stock` | ADMIN, VENDEDOR | `{stock, motivo}` (panel *Gestión de Stock*) |
| POST | `/api/inventario/productos/{id}/ajuste` | ADMIN, VENDEDOR | `{tipo: ENTRADA/SALIDA, cantidad, motivo}` |
| DELETE | `/api/inventario/productos/{id}?definitivo=false` | ADMIN | Baja lógica (o borrado si nunca se vendió) |
| GET | `/api/inventario/productos/{id}/movimientos` | ADMIN, VENDEDOR | Historial de stock (quién, cuándo, por qué) |
| GET | `/api/inventario/resumen` · `/alertas/stock-bajo` | ADMIN, VENDEDOR | Indicadores y alertas |
| GET / POST / PUT / DELETE | `/api/inventario/categorias[/{id}]` | GET público; DELETE ADMIN | CRUD de categorías |
| GET / POST / DELETE | `/api/inventario` (original) | GET público | Endpoints originales del equipo, sin cambios (no se muestran en Swagger; la versión completa es `/api/inventario/productos`) |

### tienda (8083) – catálogo, checkout y pedidos

| Método | Ruta | Acceso | Descripción |
| --- | --- | --- | --- |
| GET | `/api/tienda/catalogo?q=&categoria=&soloDisponibles=` | Público | Catálogo (solo productos activos) |
| GET | `/api/tienda/catalogo/{codigo}` | Público | Detalle de un producto |
| POST | `/api/tienda/carrito/checkout` | Token | `{correo, items:[{codigo,cantidad}], referencia, fechaEntrega?}` → boleta + stock actualizado |
| GET | `/api/tienda/pedidos` | Token | Mis pedidos (historial de compras) |
| GET | `/api/tienda/pedidos/{numero}` | Token (dueño o personal) | Boleta y seguimiento |
| POST | `/api/tienda/pedidos/{numero}/cancelar` | Token | Cancelar si está PENDIENTE (devuelve stock) |
| GET | `/api/tienda/pedidos/todos?estado=` | ADMIN, VENDEDOR | Todos los pedidos |
| PATCH | `/api/tienda/pedidos/{numero}/estado` | ADMIN, VENDEDOR | `PENDIENTE → EN_PREPARACION → EN_CAMINO → ENTREGADO` o `CANCELADO` |

**Checkout:** en una sola transacción crea el pedido `HH-1001…`, bloquea las filas de stock (`SELECT … FOR UPDATE`), valida todas las líneas y descuenta. Si falta stock en una línea, **no descuenta nada** y responde `409` indicando qué código falló. Guarda una copia de los precios en la boleta, aplica el descuento Duoc, calcula neto e IVA (19 %) y la fecha de entrega preferida (por defecto 2 días; entre mañana y 30 días). La dirección se obtiene del perfil en usuarios (vía Eureka, o con la dirección de respaldo si Eureka no está).

## Tablas de la base de datos (8)

| Base | Tabla | Para qué sirve |
| --- | --- | --- |
| `huertohogar_usuarios` | `usuarios` | Cuentas, rol, perfil y `tokenVersion` (cierre de sesión) |
| `huertohogar_tienda` | `productos` | Tabla original de inventario: nombre, descripción, precio y stock |
| | `productos_catalogo` | Ficha 1 a 1 del producto: código, categoría, origen, stock mínimo, emoji, activo |
| | `categorias` | Frutas, verduras, orgánicos y lácteos |
| | `movimientos_stock` | Quién cambió el stock, cuándo y por qué |
| | `pedidos` | Cada compra: cliente, estado, totales, fecha y dirección de entrega |
| | `detalle_pedido` | Productos de cada boleta, con el precio del momento |
| | `historial_estado_pedido` | Seguimiento: cada cambio de estado del pedido |

Si tu MySQL ya tenía las tablas `carritos`, `items_carrito` o `refresh_tokens` de una versión anterior, ejecuta una vez `limpiar-tablas-antiguas.sql`.

### Eliminar todo por completo

1. Doble clic en `detener.bat` (si no, Windows muestra "Acceso denegado a la carpeta" al borrarla).
2. Borra la carpeta del backend.
3. Opcional, en HeidiSQL: ejecuta `borrar-bases.sql` para eliminar las bases `huertohogar_usuarios` y `huertohogar_tienda`.
4. Opcional: la carpeta `%USERPROFILE%\.gradle` guarda Gradle y las librerías descargadas (cerca de 1 GB). Bórrala solo si no tienes otros proyectos con Gradle.

## Pruebas

```powershell
cd usuarios; .\gradlew.bat test    # JWT, RUN, registro, login, /me, roles, refresh, logout
cd tienda;   .\gradlew.bat test    # catálogo, permisos, stock, checkout con descuento Duoc, estados, cancelación
```
Las pruebas usan H2 en memoria (perfil `h2`), así que no necesitan MySQL.

## Conexión con el frontend

El frontend ya usa estas rutas y JSON. Solo hay que apuntar el proxy de `vite.config.js`: `/api/auth` y `/api/usuarios` → `http://localhost:8082`; `/api/tienda` y `/api/inventario` → `http://localhost:8083`.
