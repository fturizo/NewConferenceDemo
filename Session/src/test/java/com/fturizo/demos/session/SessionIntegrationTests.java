package com.fturizo.demos.session;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
public class SessionIntegrationTests {

    //TODO - Fully test integrated services
}
