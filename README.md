# Quipux Music API

A Spring Boot REST API for managing music playlists with JWT authentication and Spotify genre integration.

## Features

- **Playlist Management**: Create, read, and delete playlists
- **Song Management**: Add, view, and remove songs from playlists
- **JWT Authentication**: Secure endpoints with JWT tokens
- **Spotify Integration**: Validate songs against Spotify genre database
- **H2 Database**: In-memory database for development and testing
- **Unit Tests**: Comprehensive test coverage with MockMvc

## Technology Stack

- **Framework**: Spring Boot 2.7.12
- **Build Tool**: Maven 3.9.11
- **ORM**: Spring Data JPA with Hibernate 5.6.15
- **Database**: H2 (in-memory)
- **Security**: Spring Security + JJWT 0.11.5
- **Testing**: JUnit 5, MockMvc
- **Java**: 17+

## Project Structure

```
src/
├── main/
│   ├── java/com/quipux/
│   │   ├── QuipuxApplication.java          # Spring Boot entry point
│   │   ├── model/
│   │   │   ├── Playlist.java               # Playlist entity
│   │   │   └── Cancion.java                # Song entity
│   │   ├── repository/
│   │   │   ├── PlaylistRepository.java     # Playlist JPA repository
│   │   │   └── CancionRepository.java      # Song JPA repository
│   │   ├── controller/
│   │   │   ├── PlaylistController.java     # REST endpoints for playlists/songs
│   │   │   └── AuthController.java         # Authentication endpoint
│   │   ├── service/
│   │   │   └── SpotifyService.java         # Spotify API integration
│   │   ├── config/
│   │   │   └── SecurityConfig.java         # Spring Security configuration
│   │   └── security/
│   │       ├── JwtTokenProvider.java       # JWT token generation/validation
│   │       └── JwtAuthenticationFilter.java # JWT request filter
│   └── resources/
│       └── application.properties           # Application configuration
└── test/
    └── java/com/quipux/
        └── PlaylistControllerTest.java      # Integration tests
```

## API Endpoints

### Authentication

**POST** `/auth/login?username={username}`
- Login with any username to get a JWT token
- Returns: `{ "token": "jwt_token_here", "username": "username" }`
- No authentication required

### Playlists

**POST** `/lists` (Requires JWT)
- Create a new playlist
- Body: `{ "name": "string", "description": "string" }`
- Returns: 201 Created

**GET** `/lists` (Requires JWT)
- Get all playlists
- Returns: 200 OK with playlist array

**GET** `/lists/{listName}` (Requires JWT)
- Get a specific playlist by name
- Returns: 200 OK or 404 Not Found

**DELETE** `/lists/{listName}` (Requires JWT)
- Delete a playlist by name
- Returns: 204 No Content or 404 Not Found

### Songs (Canciones)

**POST** `/lists/{listName}/canciones` (Requires JWT)
- Add a song to a playlist
- Body: `{ "titulo": "string", "artista": "string", "album": "string", "anno": number, "genero": "string" }`
- Returns: 201 Created

**GET** `/lists/{listName}/canciones` (Requires JWT)
- Get all songs in a playlist
- Returns: 200 OK with songs array

**DELETE** `/lists/{listName}/canciones/{cancionId}` (Requires JWT)
- Remove a song from a playlist
- Returns: 204 No Content or 404 Not Found

## Setup and Running

### Prerequisites

- Java 17 or higher
- Maven 3.9+

### Installation

1. Clone the repository:
```bash
git clone <repository-url>
cd Quipux
```

2. Build the project:
```bash
mvn clean package
```

3. Run tests:
```bash
mvn test
```

### Running the Application

**Option 1: Using Maven**
```bash
mvn spring-boot:run
```

**Option 2: Using the compiled JAR**
```bash
java -jar target/quipux-senior-1.0-SNAPSHOT.jar
```

The application will start on `http://localhost:8080`

### Database Access

H2 Database Console is available at: `http://localhost:8080/h2-console`

- **JDBC URL**: `jdbc:h2:mem:quipux`
- **Username**: `sa`
- **Password**: (leave blank)

## Authentication Flow

1. **Get JWT Token**:
   ```bash
   curl -X POST "http://localhost:8080/auth/login?username=admin"
   ```
   Response: `{ "token": "eyJhbGciOiJIUzUxMiJ9...", "username": "admin" }`

2. **Use Token in Requests**:
   ```bash
   curl -H "Authorization: Bearer eyJhbGciOiJIUzUxMiJ9..." \
        http://localhost:8080/lists
   ```

## Spotify Integration

The API validates song genres against Spotify's genre database. To enable live Spotify integration:

1. Go to [Spotify Developer Dashboard](https://developer.spotify.com/dashboard)
2. Create an app and get your `Client ID` and `Client Secret`
3. Set environment variables:
   ```bash
   export SPOTIFY_CLIENT_ID=your_client_id
   export SPOTIFY_CLIENT_SECRET=your_client_secret
   ```
4. Restart the application

**Note**: For testing and development, the SpotifyService is mocked to accept all genres.

## Using Postman

A Postman collection is included: `Quipux-API.postman_collection.json`

1. Open Postman
2. Click "Import" and select `Quipux-API.postman_collection.json`
3. Set the `base_url` environment variable (default: `http://localhost:8080`)
4. Use the "Login" request to get a JWT token (automatically saved to `jwt_token` variable)
5. Use other requests with the authenticated token

## Example Usage

### 1. Login
```bash
curl -X POST "http://localhost:8080/auth/login?username=john"
```

### 2. Create a Playlist
```bash
curl -X POST http://localhost:8080/lists \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Rock Classics",
    "description": "Greatest rock songs"
  }'
```

### 3. Add a Song
```bash
curl -X POST http://localhost:8080/lists/Rock%20Classics/canciones \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "titulo": "Bohemian Rhapsody",
    "artista": "Queen",
    "album": "A Night at the Opera",
    "anno": 1975,
    "genero": "rock"
  }'
```

### 4. Get All Songs in Playlist
```bash
curl http://localhost:8080/lists/Rock%20Classics/canciones \
  -H "Authorization: Bearer YOUR_TOKEN"
```

## Testing

Run all tests:
```bash
mvn test
```

Run specific test class:
```bash
mvn test -Dtest=PlaylistControllerTest
```

Current test coverage:
- Authentication endpoint (login)
- Playlist CRUD operations
- Song CRUD operations
- JWT token validation

## Configuration

Edit `src/main/resources/application.properties` to customize:

```properties
# JWT Configuration
app.jwtSecret=your_256_bit_secret_key
app.jwtExpirationInMs=86400000  # 24 hours in milliseconds

# Spotify Configuration
spotify.client-id=your_spotify_client_id
spotify.client-secret=your_spotify_client_secret

# H2 Database (defaults are fine for development)
spring.datasource.url=jdbc:h2:mem:quipux
spring.jpa.hibernate.ddl-auto=update
```

## Building for Production

```bash
mvn clean package
```

This creates an executable JAR in `target/quipux-senior-1.0-SNAPSHOT.jar`

## Troubleshooting

### Port Already in Use
If port 8080 is in use, change it in `application.properties`:
```properties
server.port=8081
```

### JWT Token Invalid
- Ensure the token is included with "Bearer " prefix
- Token expires after 24 hours by default
- Get a new token by calling `/auth/login`

### Spotify Genre Validation Fails
- Check that Spotify credentials are properly set
- For testing, the service is mocked and accepts all genres
- Verify `SPOTIFY_CLIENT_ID` and `SPOTIFY_CLIENT_SECRET` environment variables

## License

This project is part of a technical assessment. All rights reserved.
