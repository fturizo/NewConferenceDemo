package com.fturizo.demos.speaker;

import com.fturizo.demos.speaker.entities.Speaker;
import com.fturizo.demos.speaker.repositories.SpeakerRepository;
import com.fturizo.demos.speaker.util.MailpitClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("email")
public class SpeakerWithEmailApplicationTests {

    private static final String ORG1 = "Organization 1";

    @Autowired
    private SpeakerRepository speakerRepository;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private MailpitClient mailpitClient;

    @Container
    @ServiceConnection
    static MongoDBContainer mongoDB = new MongoDBContainer("mongo:8.0");

    @Container
    static GenericContainer<?> mailPit = new GenericContainer<>("axllent/mailpit:v1.27")
                .withExposedPorts(1025, 8025)
                .waitingFor(Wait.forLogMessage(".*accessible via.*", 1));

    @DynamicPropertySource
    static void configureMongoDBProperties(DynamicPropertyRegistry registry){
        mailPit.start();
        registry.add("spring.mail.host", mailPit::getHost);
        registry.add("spring.mail.port", () -> mailPit.getMappedPort(1025));
        registry.add("mail.server.web.port}", () -> mailPit.getMappedPort(8025));
    }

    @Test
    public void add_speaker_via_api_check_email(){
        var speaker = new Speaker("Speaker 1", "speaker1@test.com", ORG1);
        var response = restTemplate.postForEntity("/speaker", speaker, Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getHeaders().getLocation()).isNotNull();

        var allSpeakers = speakerRepository.findAll();
        assertThat(allSpeakers).hasSize(1);
        assertThat(allSpeakers.getFirst().getName()).isEqualTo("Speaker 1");
        assertThat(allSpeakers.getFirst().getOrganization()).isEqualTo(ORG1);

        var mailMessage = mailpitClient.findFirstMessage();
        assertSoftly(softly -> {
            softly.assertThat(mailMessage.get("To").get(0).get("Address").asText())
                    .isEqualTo("speaker1@test.com");
            softly.assertThat(mailMessage.get("Subject").asText())
                    .isEqualTo("Speaker Registration Successful!");
        });
    }
}
