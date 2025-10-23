package com.fturizo.demos.session.repositories;

import com.fturizo.demos.session.entities.Session;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface SessionRepository extends JpaRepository<Session, Long> {
    List<Session> findByDateOrderByTitle(LocalDate date);
}
