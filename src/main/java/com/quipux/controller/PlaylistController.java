package com.quipux.controller;

import com.quipux.model.Cancion;
import com.quipux.model.Playlist;
import com.quipux.repository.CancionRepository;
import com.quipux.repository.PlaylistRepository;
import com.quipux.service.SpotifyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/lists")
public class PlaylistController {

    private final PlaylistRepository repository;
    private final CancionRepository cancionRepository;
    
    @Autowired
    private SpotifyService spotifyService;

    public PlaylistController(PlaylistRepository repository, CancionRepository cancionRepository) {
        this.repository = repository;
        this.cancionRepository = cancionRepository;
    }

    // POST /lists
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> create(@RequestBody Playlist payload) {
        if (payload.getName() == null || payload.getName().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("List name is required");
        }
        if (repository.existsByName(payload.getName())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("List with that name already exists");
        }
        Playlist saved = repository.save(new Playlist(payload.getName().trim(), payload.getDescription()));
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{name}").buildAndExpand(saved.getName()).toUri();
        return ResponseEntity.created(location).body(saved);
    }

    // GET /lists
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public List<Playlist> all() {
        return repository.findAll();
    }

    // GET /lists/{listName}
    @GetMapping("/{listName}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getByName(@PathVariable String listName) {
        Optional<Playlist> opt = repository.findByName(listName);
        if (opt.isPresent()) {
            return ResponseEntity.ok(opt.get());
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not Found");
    }

    // DELETE /lists/{listName}
    @DeleteMapping("/{listName}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> deleteByName(@PathVariable String listName) {
        Optional<Playlist> opt = repository.findByName(listName);
        if (opt.isPresent()) {
            repository.delete(opt.get());
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not Found");
    }

    // POST /lists/{listName}/canciones
    @PostMapping("/{listName}/canciones")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> addCancion(@PathVariable String listName, @RequestBody Cancion cancion) {
        Optional<Playlist> opt = repository.findByName(listName);
        if (opt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Playlist not found");
        }
        
        if (cancion.getTitulo() == null || cancion.getTitulo().trim().isEmpty() ||
            cancion.getArtista() == null || cancion.getArtista().trim().isEmpty() ||
            cancion.getGenero() == null || cancion.getGenero().trim().isEmpty() ||
            cancion.getAlbum() == null || cancion.getAlbum().trim().isEmpty() ||
            cancion.getAnno() == null || cancion.getAnno().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Title, artist, album, year and genre are required");
        }

        // Validate genre against Spotify (optional, can be mocked in tests)
        if (spotifyService != null) {
            try {
                if (!spotifyService.isValidGenre(cancion.getGenero())) {
                    return ResponseEntity.badRequest().body("Invalid genre: " + cancion.getGenero());
                }
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body("Genre validation service unavailable");
            }
        }
        
        Playlist playlist = opt.get();
        cancion.setPlaylist(playlist);
        Cancion saved = cancionRepository.save(cancion);
        
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(saved.getId())
                .toUri();
        return ResponseEntity.created(location).body(saved);
    }

    // GET /lists/{listName}/canciones
    @GetMapping("/{listName}/canciones")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getCanciones(@PathVariable String listName) {
        Optional<Playlist> opt = repository.findByName(listName);
        if (opt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Playlist not found");
        }
        
        List<Cancion> canciones = cancionRepository.findByPlaylist(opt.get());
        return ResponseEntity.ok(canciones);
    }

    // DELETE /lists/{listName}/canciones/{cancionId}
    @DeleteMapping("/{listName}/canciones/{cancionId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> deleteCancion(@PathVariable String listName, @PathVariable Long cancionId) {
        Optional<Playlist> opt = repository.findByName(listName);
        if (opt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Playlist not found");
        }
        
        Optional<Cancion> cancionOpt = cancionRepository.findByIdAndPlaylist(cancionId, opt.get());
        if (cancionOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Song not found");
        }
        
        cancionRepository.delete(cancionOpt.get());
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
