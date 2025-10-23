package com.fturizo.demos.speaker.services;

import com.fturizo.demos.speaker.entities.Speaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("!email")
public class DummyEmailService implements EmailService {

    private final Logger logger = LoggerFactory.getLogger(DummyEmailService.class);

    public void sendEmailToSpeaker(Speaker speaker){
        logger.info("Sending email to speaker: {}", speaker.getName());
        logger.info("Dummy email service enabled, no emails will be sent");
    }
}
