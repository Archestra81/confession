package com.example.confessions.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * Represents an anonymous confession submitted by a user.
 */
@Entity
@Table(name = "confessions")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Confessions {

    /**
     * Database-generated primary key.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The confession text.
     */
    @Column(nullable = false, length = 5000)
    private String content;

    /**
     * Time at which the confession was created.
     */
    @Column(nullable = false, updatable = false)
    private Instant timestamp;

    /**
     * Automatically assigns the creation timestamp before persistence.
     */
    @PrePersist
    protected void onCreate() {
        if (timestamp == null) {
            timestamp = Instant.now();
        }
    }
}
