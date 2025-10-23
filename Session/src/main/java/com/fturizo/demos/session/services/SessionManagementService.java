package com.fturizo.demos.session.services;

import com.fturizo.demos.session.entities.Session;
import com.fturizo.demos.session.repositories.SessionRepository;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import org.hibernate.exception.JDBCConnectionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Service
@SuppressWarnings("unused")
public class SessionManagementService {

    private final Logger logger = LoggerFactory.getLogger(SessionManagementService.class);
    private final SessionRepository sessionRepository;

    private final Map<Long, Session> cachedSessions = new HashMap<>();
    private final List<Session> tempSessions = new ArrayList<>();

    public SessionManagementService(SessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
        this.loadCachedSessions();
    }

    private void loadCachedSessions(){
        var sessions = sessionRepository.findAll();
        sessions.forEach(session -> cachedSessions.put(session.getId(), session));
    }

    @Transactional
    @Retry(name = "SessionManagementService", fallbackMethod = "createSessionOnDBFailure")
    public Session createSession(Session session){
        /*
        var response = speakerServiceClient.checkSpeakers(session.getSpeakers());
        if(response.getStatusCode().is2xxSuccessful()){
            entityManager.persist(session);
            return session;
        }else{
            throw new IllegalArgumentException("Invalid speakers");
        }
        */
        sessionRepository.save(session);
        cachedSessions.put(session.getId(), session);
        return session;
    }

    @Transactional
    public List<Session> createSessions(List<Session> sessions){
        sessions.forEach(this::createSession);
        return sessions;
    }

    public Session createSessionOnDBFailure(Session session, Throwable exception){
        if(exception.getCause() instanceof JDBCConnectionException){
            logger.warn("Connection to database lost temporarily. Creating session in cache.");
        }
        tempSessions.add(session);
        return session;
    }

    @Retry(name = "SessionManagementService", fallbackMethod = "getSessionOnFailure")
    public Optional<Session> getSession(long id){
        return sessionRepository.findById(id);
    }

    public Optional<Session> getSessionOnFailure(long id, Throwable exception){
        if(exception.getCause() instanceof JDBCConnectionException){
            logger.warn("Connection to database lost temporarily. Getting session from cache.");
        }
        return Optional.ofNullable(cachedSessions.get(id));
    }

    public List<Session> getSessions(){
        var currentSessions = sessionRepository.findAll();
        currentSessions.addAll(tempSessions);
        return currentSessions;
    }

    @TimeLimiter(name = "SessionManagementService", fallbackMethod = "getSessionsAfterTimeLimit")
    public CompletableFuture<List<Session>> getSessions(LocalDate date){
        return CompletableFuture.supplyAsync(() -> sessionRepository.findByDateOrderByTitle(date));
    }

    public CompletableFuture<List<Session>> getSessionsAfterTimeLimit(LocalDate date, Throwable exception){
        logger.warn("Time limit for query exceeded. Getting sessions from cache.");
        var results = cachedSessions.values().stream()
                .filter(session -> session.getDate().isEqual(date))
                .toList();
        return CompletableFuture.completedFuture(results);
    }

    public void deleteSession(long id){
        sessionRepository.deleteById(id);
    }

    public void clearSessions(){
        sessionRepository.deleteAll();
        cachedSessions.clear();
        tempSessions.clear();
    }
}
