package com.fturizo.demos.session;

import com.fturizo.demos.session.entities.Session;
import com.fturizo.demos.session.services.SessionManagementService;
import org.junit.jupiter.api.AutoClose;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("integration")
public class SessionIntegrationTests {

    static Network network = Network.newNetwork();

    @Container
    static MongoDBContainer mongoDB = new MongoDBContainer("mongo:8.0")
            .withNetwork(network)
            .withNetworkAliases("mongo");

    @Container
    static GenericContainer<?> speakerService = new GenericContainer<>("docker.io/library/speaker:latest")
            .withExposedPorts(8080)
            .withNetwork(network)
            .dependsOn(mongoDB)
            .withEnv("SPRING_DATA_MONGODB_HOST", "mongo")
            .withEnv("SPRING_DATA_MONGODB_PORT", "27017")
            .waitingFor(Wait.forLogMessage(".*Started SpeakerApplication.*", 1));

    @Container
    @ServiceConnection
    private static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(DockerImageName.parse("postgres:18-alpine"))
            .withNetwork(network)
            .withNetworkAliases("postgres");

    @Autowired
    private SessionManagementService sessionManagementService;

    private static String speakerServiceUrl;

    @BeforeAll
    public static void prepareSpeakerService(){
        speakerService.start();
        speakerServiceUrl = "http://%s:%d/".formatted(speakerService.getHost(), speakerService.getFirstMappedPort());

        var url = "%s/speaker".formatted(speakerServiceUrl);
        var restTemplate = new RestTemplate();

        var speaker1 = new Speaker("Speaker 1", "speaker1@test.com", "Sample Organization 1");
        var speaker2 = new Speaker("Speaker 2", "speaker2@test.com", "Sample Organization 2");
        restTemplate.postForEntity(url, speaker1, Void.class);
        restTemplate.postForEntity(url, speaker2, Void.class);
    }

    @DynamicPropertySource
    public static void configureIntegration(DynamicPropertyRegistry registry){
        registry.add("conference.demo.services.speaker.url", () -> speakerServiceUrl);
    }

    @Test
    public void check_session_create(){
        var session = new Session("Test Session 1", "OCARINA", LocalDate.now().plusDays(1), Duration.ofHours(1), List.of("Speaker 1", "Speaker 2"));
        sessionManagementService.createSession(session);
        var currentSessions = sessionManagementService.getSessions();
        assertThat(currentSessions).hasSize(1);
    }

    record Speaker(String name, String email, String organization) {
    }
}
