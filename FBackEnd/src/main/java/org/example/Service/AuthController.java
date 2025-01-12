package org.example.Service;

import org.example.Domain.DTO.UserDTO;
import org.example.Domain.User;
import org.example.Repository.UserRepository;
import org.example.Security.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@CrossOrigin
public class AuthController {
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    public AuthController(UserRepository userRepository, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil; // Spring injectează automat bean-ul JwtUtil
    }
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User loginRequest) {
        User user = userRepository.getByCredentials(loginRequest.getUsername(), loginRequest.getPassword());
        if (user != null) {
            String token = jwtUtil.generateToken(user.getUsername());
            return ResponseEntity.ok().body("{ \"token\":\""+token+"\"}");
        }
        return ResponseEntity.status(401).body("{\"status\":\"Invalid username or password\"}");
    }

    @CrossOrigin(origins = "http://localhost:8100")
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody UserDTO registerRequest) {
        if (userRepository.getByUsername(registerRequest.getUsername()) != null) {
            return ResponseEntity.status(409).body("{\"status\":\"Username already exists\"}");
        }
        User user = new User(userRepository.incrementId(), registerRequest.getUsername(), registerRequest.getPassword(), registerRequest.getEmail());
        userRepository.add(user);
        return ResponseEntity.ok("{\"status\":\"ok\"}");
    }

    @GetMapping("/validate")
    public ResponseEntity<?> validateToken(@RequestParam String token) {
        String username = jwtUtil.validateToken(token);
        if (username != null) {
            return ResponseEntity.ok().body("{Token is valid for user: " + username + "}");
        }
        return ResponseEntity.status(401).body("{Invalid token}");
    }
}