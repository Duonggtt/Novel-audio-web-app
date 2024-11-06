package com.spring3.oauth.jwt.controllers;

import com.spring3.oauth.jwt.entity.Hobby;
import com.spring3.oauth.jwt.services.impl.HobbyServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:3388", "https://80ba-14-231-167-47.ngrok-free.app"})
@RequestMapping("/api/v1/hobbies")
public class HobbyController {
    private final HobbyServiceImpl hobbyService;

    @GetMapping("/")
    public ResponseEntity<?> getAllHobbies() {
        return ResponseEntity.ok(hobbyService.getAllHobbies());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getHobbyById(@PathVariable Integer id) {
        return ResponseEntity.ok(hobbyService.getHobbyById(id));
    }

    @PostMapping("/create")
    public ResponseEntity<?> saveHobby(@Valid @RequestBody Hobby hobby) {
        return new ResponseEntity<>(hobbyService.saveHobby(hobby), HttpStatus.CREATED);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateHobby(@PathVariable Integer id, @Valid @RequestBody Hobby hobby) {
        return ResponseEntity.ok(hobbyService.updateHobby(id, hobby));
    }
}
