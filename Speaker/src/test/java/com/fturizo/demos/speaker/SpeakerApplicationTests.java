package com.fturizo.demos.speaker;

import com.fturizo.demos.speaker.entities.Speaker;
import com.fturizo.demos.speaker.repositories.SpeakerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class SpeakerApplicationTests {

    private static final String ORG1 = "Organization 1";
    private static final String ORG2 = "Organization 2";

    @Autowired
    private SpeakerRepository speakerRepository;

    @Autowired
    private TestRestTemplate restTemplate;

    @Container
    @ServiceConnection
    static MongoDBContainer mongoDB = new MongoDBContainer("mongo:8.0");

    @DynamicPropertySource
    @SuppressWarnings("unused")
    static void configureMongoDBProperties(DynamicPropertyRegistry registry){
        //registry.add("spring.data.mongodb.host", mongoDB::getHost);
        //registry.add("spring.data.mongodb.port", mongoDB::getFirstMappedPort);
    }

    @BeforeEach
    public void clear_speakers(){
        speakerRepository.deleteAll();
    }

    @Test
    public void add_speakers_via_repo(){
        speakerRepository.saveAll(List.of(
                new Speaker("Speaker 1", "speaker1@test.com", ORG1),
                new Speaker("Speaker 2", "speaker2@test.com", ORG1),
                new Speaker("Speaker 3", "speaker3@test.com", ORG2)
                ));
        var org1speakers = speakerRepository.findByOrganization(ORG1);
        assertThat(org1speakers.size()).isEqualTo(2);

        var org2speakers = speakerRepository.findByOrganization(ORG2);
        assertThat(org2speakers.size()).isEqualTo(1);
        assertThat(org2speakers.getFirst().getName()).isEqualTo("Speaker 3");
    }

    @Test
    public void add_speaker_via_api(){
        var speaker = new Speaker("Speaker 1", "speaker1@test.com", ORG1);
        var response = restTemplate.postForEntity("/speaker", speaker, Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getHeaders().getLocation()).isNotNull();

        var allSpeakers = speakerRepository.findAll();
        assertThat(allSpeakers).hasSize(1);
        assertThat(allSpeakers.getFirst().getName()).isEqualTo("Speaker 1");
        assertThat(allSpeakers.getFirst().getOrganization()).isEqualTo(ORG1);
    }

    @Test
    public void check_multiple_speaker_exists(){
        speakerRepository.saveAll(List.of(
                new Speaker("Speaker 1", "speaker1@test.com", ORG1),
                new Speaker("Speaker 2", "speaker2@test.com", ORG1)
        ));

        var response = restTemplate.exchange("/speaker?names={first}&names={second}", HttpMethod.HEAD, null, Void.class, "Speaker 1", "Speaker 2");
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void check_speaker_doesnt_exist(){
        var response = restTemplate.exchange("/speaker?names={first}", HttpMethod.HEAD, null, Void.class, "Speaker 1");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
