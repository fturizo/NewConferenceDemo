package com.fturizo.demos.session.controllers;

import com.fturizo.demos.session.entities.Session;
import com.fturizo.demos.session.services.SessionManagementService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/session")
public class SessionController {

    private final SessionManagementService sessionManagementService;

    public SessionController(SessionManagementService sessionManagementService) {
        this.sessionManagementService = sessionManagementService;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('can-create-sessions')")
    public ResponseEntity<Void> createSession(@RequestBody Session session, UriComponentsBuilder ucb){
        var result = sessionManagementService.createSession(session);
        var location = ucb.path("session/{id}").buildAndExpand(result.getId()).toUri();
        return ResponseEntity.created(location).build();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('can-see-sessions')")
    public Optional<Session> get(@PathVariable long id){
        return sessionManagementService.getSession(id);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('can-create-sessions')")
    public List<Session> getAll(){
        return sessionManagementService.getSessions();
    }

    @GetMapping("/date/{date}")
    @PreAuthorize("hasAuthority('can-create-sessions')")
    public List<Session> forDate(@PathVariable LocalDate date){
        return sessionManagementService.getSessions(date).resultNow();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('can-delete-sessions')")
    public ResponseEntity<Void> delete(@PathVariable long id){
        var result = sessionManagementService.getSession(id);
        if(result.isPresent()){
            sessionManagementService.deleteSession(id);
            return ResponseEntity.noContent().build();
        }else{
            return ResponseEntity.notFound().build();
        }
    }
}
