package com.example.confessions.repository;

import com.example.confessions.entity.Confessions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for storing and retrieving confessions.
 */
@Repository
public interface ConfessionsRepository extends JpaRepository<Confessions, Long> {
}