package com.fturizo.demos.session;

import org.springframework.boot.SpringApplication;

public class TestSessionApplication {

    public static void main(String[] args) {
        SpringApplication.from(SessionApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
