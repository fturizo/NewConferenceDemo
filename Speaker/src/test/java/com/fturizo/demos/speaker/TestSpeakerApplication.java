package com.fturizo.demos.speaker;

import org.springframework.boot.SpringApplication;

public class TestSpeakerApplication {

    public static void main(String[] args) {
        SpringApplication.from(SpeakerApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
