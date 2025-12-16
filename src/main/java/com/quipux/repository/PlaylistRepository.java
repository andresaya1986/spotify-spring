package com.quipux.repository;

import com.quipux.model.Playlist;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PlaylistRepository extends JpaRepository<Playlist, Long> {
    Optional<Playlist> findByName(String name);
    void deleteByName(String name);
    boolean existsByName(String name);
}
