package com.fturizo.demos.session.integration;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.HttpExchange;

import java.util.List;

public interface SpeakerServiceClient {

    @HttpExchange(url = "/speaker?names={names}", method = "HEAD")
    ResponseEntity<Void> checkSpeakers(@RequestParam List<String> names);
}
