package com.quipux.controller;

import com.quipux.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {
    
    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestParam String username) {
        String token = jwtTokenProvider.generateToken(username);
        
        Map<String, String> response = new HashMap<>();
        response.put("token", token);
        response.put("username", username);
        
        return ResponseEntity.ok(response);
    }
}
