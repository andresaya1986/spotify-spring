package com.quipux;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quipux.model.Playlist;
import com.quipux.model.Cancion;
import com.quipux.repository.PlaylistRepository;
import com.quipux.security.JwtTokenProvider;
import com.quipux.service.SpotifyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class PlaylistControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PlaylistRepository repository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private SpotifyService spotifyService;

    private String token;

    @BeforeEach
    void beforeEach() {
        repository.deleteAll();
        token = jwtTokenProvider.generateToken("testuser");
        // Mock Spotify service to return valid genres
        when(spotifyService.isValidGenre(anyString())).thenReturn(true);
        when(spotifyService.getAvailableGenres()).thenReturn(Arrays.asList("rock", "pop", "jazz", "blues"));
    }

    @Test
    void createList_ok() throws Exception {
        Playlist p = new Playlist("rock", "Rock songs");
        mockMvc.perform(post("/lists")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(p)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", org.hamcrest.Matchers.containsString("/lists/rock")))
                .andExpect(jsonPath("$.name").value("rock"));
    }

    @Test
    void createList_invalidName_badRequest() throws Exception {
        Playlist p = new Playlist(null, "desc");
        mockMvc.perform(post("/lists")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(p)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAllAndGetByName_andDelete() throws Exception {
        Playlist p = repository.save(new Playlist("pop", "Pop songs"));

        mockMvc.perform(get("/lists")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("pop"));

        mockMvc.perform(get("/lists/pop")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Pop songs"));

        mockMvc.perform(delete("/lists/pop")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/lists/pop")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void authLogin_ok() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .param("username", "testuser"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.username").value("testuser"));
    }

        @Test
        void addCancion_whenSpotifyFails_allowsLocalAdd() throws Exception {
                // simulate spotify service failure
                when(spotifyService.isValidGenre(anyString())).thenThrow(new RuntimeException("No token"));

                // create playlist
                repository.save(new Playlist("testlist", "desc"));

                Cancion c = new Cancion("Song 1", "Artist", "Album", "2020", "rock");

                mockMvc.perform(post("/lists/testlist/canciones")
                                                .header("Authorization", "Bearer " + token)
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(objectMapper.writeValueAsString(c)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.titulo").value("Song 1"))
                                .andExpect(jsonPath("$.artista").value("Artist"));
        }
}
