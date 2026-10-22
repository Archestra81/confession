package com.example.confessions.controller;

import com.example.confessions.entity.Confessions;
import com.example.confessions.repository.ConfessionsRepository;
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

/**
 * REST controller for creating and administrating confessions.
 *
 * Public endpoint:
 * POST /api/confessions
 *
 * Admin endpoints:
 * GET /api/admin/confessions
 * DELETE /api/admin/confessions/{id}
 *
 * Admin authentication uses the X-Admin-Passcode request header.
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ConfessionsController {

    private static final String ADMIN_PASSCODE_HEADER = "X-Admin-Passcode";

    private final ConfessionsRepository confessionRepository;

    /**
     * Admin passcode loaded from application.properties.
     *
     * In production, provide it through the ADMIN_PASSCODE environment variable.
     */
    @Value("${app.admin.passcode}")
    private String adminPasscode;

    /**
     * Creates a new anonymous confession.
     *
     * @param request request body containing confession content
     * @return persisted confession
     */
    @PostMapping("/confessions")
    public ResponseEntity<Confessions> createConfession(
            @Valid @RequestBody CreateConfessionRequest request) {

        Confessions confession = Confessions.builder()
                .content(request.content().trim())
                .build();

        Confessions savedConfession = confessionRepository.save(confession);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(savedConfession);
    }

    /**
     * Returns all confessions for an authenticated administrator.
     *
     * @param providedPasscode passcode supplied through X-Admin-Passcode
     * @return confessions ordered from newest to oldest
     */
    @GetMapping("/admin/confessions")
    public ResponseEntity<?> getAllConfessions(
            @RequestHeader(value = ADMIN_PASSCODE_HEADER, required = false)
            String providedPasscode) {

        if (!isValidAdminPasscode(providedPasscode)) {
            return unauthorizedResponse();
        }

        List<Confessions> confessions = confessionRepository.findAll(
                Sort.by(Sort.Direction.DESC, "timestamp")
        );

        return ResponseEntity.ok(confessions);
    }

    /**
     * Deletes a confession for an authenticated administrator.
     *
     * @param id confession identifier
     * @param providedPasscode passcode supplied through X-Admin-Passcode
     * @return no-content response when deletion succeeds
     */
    @DeleteMapping("/admin/confessions/{id}")
    public ResponseEntity<?> deleteConfession(
            @PathVariable Long id,
            @RequestHeader(value = ADMIN_PASSCODE_HEADER, required = false)
            String providedPasscode) {

        if (!isValidAdminPasscode(providedPasscode)) {
            return unauthorizedResponse();
        }

        if (!confessionRepository.existsById(id)) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Confession not found"));
        }

        confessionRepository.deleteById(id);

        return ResponseEntity.noContent().build();
    }

    /**
     * Compares the supplied passcode with the configured passcode.
     *
     * String.equals is intentionally called on the configured value to avoid
     * a NullPointerException when the request header is missing.
     */
    private boolean isValidAdminPasscode(String providedPasscode) {
        return providedPasscode != null
                && !providedPasscode.isBlank()
                && adminPasscode.equals(providedPasscode);
    }

    /**
     * Provides a consistent response for failed administrator authentication.
     */
    private ResponseEntity<Map<String, String>> unauthorizedResponse() {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "Invalid or missing admin passcode"));
    }

    /**
     * Request DTO for creating a confession.
     *
     * Records are immutable and prevent accidental modification after
     * deserialization.
     */
    public record CreateConfessionRequest(
            @NotBlank(message = "Confession content must not be blank")
            @Size(
                    max = 5000,
                    message = "Confession content must not exceed 5000 characters"
            )
            String content
    ) {
    }
}
