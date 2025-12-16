package com.quipux.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import java.util.ArrayList;
import java.util.List;

@Service
public class SpotifyService {
    
    @Value("3075e115c1314d338f113cf7a6283953")
    private String clientId;
    
    @Value("6888d64c0a8143e28b92839bb90a4086")
    private String clientSecret;
    
    private String accessToken;
    private Long tokenExpirationTime;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    private static final String TOKEN_URL = "https://accounts.spotify.com/api/token";
    private static final String CATEGORIES_URL = "https://api.spotify.com/v1/browse/categories";
    
    /**
     * Obtiene un access token de Spotify usando client credentials flow
     */
    private void refreshAccessToken() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "client_credentials");
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);
        
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);
        
        try {
            ResponseEntity<String> response = restTemplate.postForEntity(TOKEN_URL, request, String.class);
            JsonNode jsonNode = objectMapper.readTree(response.getBody());
            this.accessToken = jsonNode.get("access_token").asText();
            long expiresIn = jsonNode.get("expires_in").asLong();
            this.tokenExpirationTime = System.currentTimeMillis() + (expiresIn * 1000);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get Spotify access token: " + e.getMessage());
        }
    }
    
    /**
     * Obtiene la lista de géneros disponibles de Spotify
     */
    public List<String> getAvailableGenres() {
        if (accessToken == null || System.currentTimeMillis() >= tokenExpirationTime) {
            refreshAccessToken();
        }
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(
                    CATEGORIES_URL + "?limit=50&offset=0",
                    String.class,
                    entity
            );
            
            JsonNode jsonNode = objectMapper.readTree(response.getBody());
            List<String> genres = new ArrayList<>();
            
            JsonNode items = jsonNode.get("categories").get("items");
            if (items != null && items.isArray()) {
                items.forEach(item -> {
                    String name = item.get("name").asText();
                    genres.add(name.toLowerCase());
                });
            }
            
            return genres;
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch genres from Spotify: " + e.getMessage());
        }
    }
    
    /**
     * Valida si un género es válido según Spotify
     */
    public boolean isValidGenre(String genre) {
        return getAvailableGenres().stream()
                .anyMatch(g -> g.equalsIgnoreCase(genre));
    }
}
