package com.quipux.repository;

import com.quipux.model.Cancion;
import com.quipux.model.Playlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CancionRepository extends JpaRepository<Cancion, Long> {
    List<Cancion> findByPlaylist(Playlist playlist);
    Optional<Cancion> findByIdAndPlaylist(Long id, Playlist playlist);
}
