package com.example.confessions.controller;

import com.example.confessions.entity.Confession;
import com.example.confessions.repository.ConfessionRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ConfessionController {

    private static final String ADMIN_HEADER = "X-Admin-Passcode";

    private final ConfessionRepository confessionRepository;

    @Value("${app.admin.passcode}")
    private String configuredAdminPasscode;

    @PostMapping("/confessions")
    public ResponseEntity<Confession> createConfession(
            @Valid @RequestBody CreateConfessionRequest request) {

        Confession confession = Confession.builder()
                .content(request.content().trim())
                .build();

        Confession saved = confessionRepository.save(confession);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(saved);
    }

    @GetMapping("/admin/confessions")
    public ResponseEntity<?> getConfessions(
            @RequestHeader(value = ADMIN_HEADER, required = false)
            String providedPasscode) {

        if (!isValidPasscode(providedPasscode)) {
            return unauthorized();
        }

        List<Confession> confessions = confessionRepository.findAll(
                Sort.by(Sort.Direction.DESC, "timestamp")
        );

        return ResponseEntity.ok(confessions);
    }

    @DeleteMapping("/admin/confessions/{id}")
    public ResponseEntity<?> deleteConfession(
            @PathVariable Long id,
            @RequestHeader(value = ADMIN_HEADER, required = false)
            String providedPasscode) {

        if (!isValidPasscode(providedPasscode)) {
            return unauthorized();
        }

        if (!confessionRepository.existsById(id)) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Confession not found"));
        }

        confessionRepository.deleteById(id);

        return ResponseEntity.noContent().build();
    }

    private boolean isValidPasscode(String providedPasscode) {
        return providedPasscode != null
                && !providedPasscode.isBlank()
                && configuredAdminPasscode.equals(providedPasscode);
    }

    private ResponseEntity<Map<String, String>> unauthorized() {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "Invalid or missing admin passcode"));
    }

    public record CreateConfessionRequest(
            @NotBlank(message = "Confession content must not be blank")
            @Size(
                    max = 500,
                    message = "Confession content must not exceed 500 characters"
            )
            String content
    ) {
    }
}