# API de Música Quipux

Una API REST de Spring Boot para gestionar listas de reproducción de música con autenticación JWT e integración con géneros de Spotify.

## Características

- **Gestión de Listas**: Crear, leer y eliminar listas de reproducción
- **Gestión de Canciones**: Agregar, ver y eliminar canciones de listas
- **Autenticación JWT**: Endpoints seguros con tokens JWT
- **Integración con Spotify**: Validar canciones contra base de datos de géneros de Spotify
- **Base de Datos H2**: Base de datos en memoria para desarrollo y testing
- **Pruebas Unitarias**: Cobertura integral de pruebas con MockMvc

## Stack Tecnológico

- **Framework**: Spring Boot 2.7.12
- **Herramienta de Construcción**: Maven 3.9.11
- **ORM**: Spring Data JPA con Hibernate 5.6.15
- **Base de Datos**: H2 (en memoria)
- **Seguridad**: Spring Security + JJWT 0.11.5
- **Testing**: JUnit 5, MockMvc
- **Java**: 17+

## Estructura del Proyecto

```
src/
├── main/
│   ├── java/com/quipux/
│   │   ├── QuipuxApplication.java          # Punto de entrada Spring Boot
│   │   ├── model/
│   │   │   ├── Playlist.java               # Entidad Lista
│   │   │   └── Cancion.java                # Entidad Canción
│   │   ├── repository/
│   │   │   ├── PlaylistRepository.java     # Repositorio JPA para Listas
│   │   │   └── CancionRepository.java      # Repositorio JPA para Canciones
│   │   ├── controller/
│   │   │   ├── PlaylistController.java     # Endpoints REST para listas/canciones
│   │   │   └── AuthController.java         # Endpoint de autenticación
│   │   ├── service/
│   │   │   └── SpotifyService.java         # Integración con API de Spotify
│   │   ├── config/
│   │   │   └── SecurityConfig.java         # Configuración Spring Security
│   │   └── security/
│   │       ├── JwtTokenProvider.java       # Generación/validación de token JWT
│   │       └── JwtAuthenticationFilter.java # Filtro de solicitud JWT
│   └── resources/
│       └── application.properties           # Configuración de aplicación
└── test/
    └── java/com/quipux/
        └── PlaylistControllerTest.java      # Pruebas de integración
```

## Endpoints de la API

### Autenticación

**POST** `/auth/login?username={username}`
- Inicia sesión con cualquier nombre de usuario para obtener un token JWT
- Retorna: `{ "token": "jwt_token_aqui", "username": "nombre_usuario" }`
- No requiere autenticación

### Listas de Reproducción

**POST** `/lists` (Requiere JWT)
- Crear una nueva lista de reproducción
- Body: `{ "name": "string", "description": "string" }`
- Retorna: 201 Creado

**GET** `/lists` (Requiere JWT)
- Obtener todas las listas de reproducción
- Retorna: 200 OK con array de listas

**GET** `/lists/{listName}` (Requiere JWT)
- Obtener una lista específica por nombre
- Retorna: 200 OK o 404 No Encontrado

**DELETE** `/lists/{listName}` (Requiere JWT)
- Eliminar una lista por nombre
- Retorna: 204 Sin Contenido o 404 No Encontrado

### Canciones

**POST** `/lists/{listName}/canciones` (Requiere JWT)
- Agregar una canción a una lista
- Body: `{ "titulo": "string", "artista": "string", "album": "string", "anno": número, "genero": "string" }`
- Retorna: 201 Creado

**GET** `/lists/{listName}/canciones` (Requiere JWT)
- Obtener todas las canciones en una lista
- Retorna: 200 OK con array de canciones

**DELETE** `/lists/{listName}/canciones/{cancionId}` (Requiere JWT)
- Eliminar una canción de una lista
- Retorna: 204 Sin Contenido o 404 No Encontrado

## Configuración y Ejecución

### Requisitos Previos

- Java 17 o superior
- Maven 3.9+
- Acceso a la línea de comandos

### Instalación

1. **Clonar o descargar el proyecto**
```bash
cd /Users/jaimeandresayaluna/Documents/Proyectos/Quipux
```

2. **Compilar el proyecto**
```bash
mvn clean compile
```

3. **Ejecutar las pruebas**
```bash
mvn test
```

4. **Construir el JAR ejecutable**
```bash
mvn package
```

### Opciones de Ejecución

#### Opción 1: Usar Maven
```bash
mvn spring-boot:run
```

#### Opción 2: Ejecutar JAR directamente
```bash
java -jar target/quipux-senior-1.0-SNAPSHOT.jar
```

La aplicación se iniciará en: **http://localhost:8080**

## Configuración

### Configuración de Puerto
Editar `src/main/resources/application.properties`:
```properties
server.port=8081
```

### Integración con Spotify (Opcional)

Para habilitar la validación real de géneros contra Spotify:

1. Crear una aplicación en [Spotify Developer Dashboard](https://developer.spotify.com/dashboard)
2. Obtener `Client ID` y `Client Secret`
3. Configurar variables de entorno:
```bash
export SPOTIFY_CLIENT_ID=3075e115c1314d338f113cf7a6283953
export SPOTIFY_CLIENT_SECRET=6888d64c0a8143e28b92839bb90a4086
```

Nota sobre Redirect URI:

- Si vas a usar el flujo Authorization Code (login de usuario), añade en el Dashboard una Redirect URI que coincida con la que maneje tu aplicación. Ejemplo local:

  `http://localhost:8080/spotify/callback`

- Si usas únicamente el flujo Client Credentials (como en `SpotifyService` para obtener géneros), ese flujo no requiere redirect en tiempo de ejecución, pero el Dashboard solicita un valor — puedes usar la misma URI de ejemplo anterior.

O configurar en `application.properties`:
```properties
spotify.client-id=tu_client_id
spotify.client-secret=tu_client_secret
```

**Nota**: Las pruebas funcionan sin credenciales de Spotify (usa mocking)

## Uso de la API con cURL

### 1. Obtener Token JWT
```bash
curl -X POST "http://localhost:8080/auth/login?username=usuario1"
```

Guardar el token en una variable:
```bash
TOKEN=$(curl -s -X POST "http://localhost:8080/auth/login?username=usuario1" | jq -r '.token')
```

### 2. Crear una Lista
```bash
curl -X POST http://localhost:8080/lists \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Mi Lista de Rock",
    "description": "Las mejores canciones de rock"
  }'
```

### 3. Agregar una Canción
```bash
curl -X POST http://localhost:8080/lists/Mi%20Lista%20de%20Rock/canciones \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "titulo": "Bohemian Rhapsody",
    "artista": "Queen",
    "album": "A Night at the Opera",
    "anno": 1975,
    "genero": "rock"
  }'
```

### 4. Listar Canciones
```bash
curl http://localhost:8080/lists/Mi%20Lista%20de%20Rock/canciones \
  -H "Authorization: Bearer $TOKEN"
```

### 5. Eliminar una Canción
```bash
curl -X DELETE http://localhost:8080/lists/Mi%20Lista%20de%20Rock/canciones/1 \
  -H "Authorization: Bearer $TOKEN"
```

### 6. Eliminar una Lista
```bash
curl -X DELETE http://localhost:8080/lists/Mi%20Lista%20de%20Rock \
  -H "Authorization: Bearer $TOKEN"
```

## Uso de la Colección de Postman

### Importar la Colección

1. Abrir Postman
2. Click en "Import" (arriba a la izquierda)
3. Seleccionar archivo `Quipux-API.postman_collection.json`
4. Click "Import"

### Configurar Ambiente

1. En Postman, hacer click en el icono de "Environment" (arriba a la derecha)
2. Seleccionar o crear un nuevo ambiente
3. Configurar variable: `base_url` = `http://localhost:8080`

### Usar los Endpoints

1. **Primer paso**: Ejecutar "Authentication → Login"
   - El token se guardará automáticamente en la variable `jwt_token`
2. **Luego**: Usar cualquier otro endpoint de la colección
   - Todos usan automáticamente el token guardado

## Base de Datos H2

La consola de H2 está disponible en: **http://localhost:8080/h2-console**

**Credenciales**:
- URL JDBC: `jdbc:h2:mem:quipux`
- Usuario: `sa`
- Contraseña: (dejar en blanco)

Aquí puedes:
- Ver las tablas creadas
- Ejecutar consultas SQL
- Ver datos en tiempo real

## Pruebas

### Ejecutar Todas las Pruebas
```bash
mvn test
```

### Resultados Esperados
```
Tests run: 5, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

### Cobertura de Pruebas

| Prueba | Descripción |
|--------|-------------|
| `AppTest` | Prueba básica de funcionalidad |
| `createList_ok` | Crear lista exitosamente (201) |
| `createList_invalidName_badRequest` | Validación de nombre (400) |
| `getAllAndGetByName_andDelete` | Operaciones CRUD completas |
| `authLogin_ok` | Emisión de token JWT |

## Resolución de Problemas

### Puerto 8080 en Uso
```bash
# Cambiar el puerto
mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=8081"
```

### Error al Conectar con Spotify
- Verificar que las credenciales sean correctas
- Si no tiene credenciales, las pruebas funcionan con mocking
- Para producción, registrarse en https://developer.spotify.com

### Problemas con JWT
- Verificar que el token Bearer esté en el header "Authorization"
- Los tokens expiran después de 24 horas
- Obtener un nuevo token ejecutando `/auth/login` nuevamente

### No Se Puede Conectar a la Base de Datos
- H2 está en memoria, se recrea en cada inicio
- Si necesita persistencia, cambiar a PostgreSQL/MySQL
- Los datos se pierden al reiniciar la aplicación (comportamiento normal)

## Documentación Adicional

- **[IMPLEMENTATION_SUMMARY.md](IMPLEMENTATION_SUMMARY.md)**: Resumen detallado de la implementación
- **[Postman Collection](Quipux-API.postman_collection.json)**: Colección de endpoints lista para usar

## Estructura de Respuestas

### Respuesta Exitosa (200/201)
```json
{
  "id": 1,
  "name": "Mi Lista",
  "description": "Descripción",
  "canciones": []
}
```

### Respuesta de Error (400/404)
```json
"Error message describing the issue"
```

### Respuesta de Autenticación (200)
```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "username": "usuario1"
}
```

## Seguridad

- ✅ Autenticación JWT con HMAC-SHA512
- ✅ Tokens con expiración de 24 horas
- ✅ Validación de entrada en todos los endpoints
- ✅ CORS configurado para desarrollo
- ✅ Protección CSRF deshabilitada (API sin estado)

## Próximos Pasos (Futuro)

- Migrar a base de datos PostgreSQL
- Agregar autenticación con contraseñas
- Implementar paginación
- Agregar búsqueda y filtrado avanzado
- Documentación OpenAPI/Swagger

## Contacto y Soporte

Para reportar problemas o sugerencias, contactar al equipo de desarrollo.

## Licencia

Este proyecto es parte de la prueba técnica de Quipux.

---

**Última actualización**: Diciembre 2025
