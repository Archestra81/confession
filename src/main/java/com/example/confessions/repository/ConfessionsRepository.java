package com.example.confessions.repository;

import com.example.confessions.entity.Confession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConfessionRepository extends JpaRepository<Confession, Long> {
}
