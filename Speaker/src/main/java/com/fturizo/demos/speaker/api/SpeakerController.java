package com.fturizo.demos.speaker.api;

import com.fturizo.demos.speaker.services.EmailService;
import com.fturizo.demos.speaker.entities.Speaker;
import com.fturizo.demos.speaker.repositories.SpeakerRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@RestController
@RequestMapping("/speaker")
class SpeakerController {

    private final SpeakerRepository speakerRepository;
    private final EmailService emailService;

    public SpeakerController(SpeakerRepository speakerRepository, EmailService emailService) {
        this.speakerRepository = speakerRepository;
        this.emailService = emailService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Speaker> getSpeaker(@PathVariable Integer id){
        var result = speakerRepository.findById(id);
        return result.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Void> addSpeaker(@RequestBody Speaker speaker, UriComponentsBuilder ucb){
        var result = speakerRepository.save(speaker);
        emailService.sendEmailToSpeaker(speaker);
        var location = ucb.path("speaker/{id}").buildAndExpand(result.getId()).toUri();
        return ResponseEntity.created(location).build();
    }

    @GetMapping
    public List<Speaker> getAllSpeakers(){
        return speakerRepository.findAll();
    }

    @RequestMapping(method = RequestMethod.HEAD)
    public ResponseEntity<Void> checkSpeakers(@RequestParam("names") List<String> names){
        return (speakerRepository.allNamesExists(names) ? ResponseEntity.ok() : ResponseEntity.notFound()).build();
    }
}
