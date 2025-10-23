package com.fturizo.demos.session;

import com.fturizo.demos.session.entities.Session;
import com.fturizo.demos.session.services.SessionManagementService;
import eu.rekawek.toxiproxy.Proxy;
import eu.rekawek.toxiproxy.ToxiproxyClient;
import eu.rekawek.toxiproxy.model.ToxicDirection;
import org.hibernate.exception.JDBCConnectionException;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.toxiproxy.ToxiproxyContainer;
import org.testcontainers.utility.DockerImageName;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SessionToxicTests {

    @AutoClose
    public static Network network = Network.newNetwork();

    @Container
    @AutoClose
    private static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(DockerImageName.parse("postgres:18-alpine"))
            .withNetwork(network)
            .withNetworkAliases("postgres");

    @Container
    @AutoClose
    private static ToxiproxyContainer toxiProxy = new ToxiproxyContainer(DockerImageName.parse("ghcr.io/shopify/toxiproxy:latest"))
            .dependsOn(postgres)
            .withNetwork(network);

    @Autowired
    private SessionManagementService sessionManagementService;

    private static Proxy postgresProxy;

    @DynamicPropertySource
    static void configurePostgres(DynamicPropertyRegistry registry) throws IOException {
        toxiProxy.start();
        postgres.start();

        var toxiProxyClient = new ToxiproxyClient(toxiProxy.getHost(), toxiProxy.getControlPort());
        postgresProxy = toxiProxyClient.createProxy("postgres", "0.0.0.0:8666", "postgres:5432");

        registry.add("spring.datasource.url", () -> "jdbc:postgresql://%s:%d/%s".formatted(toxiProxy.getHost(), toxiProxy.getMappedPort(8666), postgres.getDatabaseName()));
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @BeforeEach
    public void clearSessions(){
        sessionManagementService.clearSessions();
    }

    @AfterEach
    public void clearToxics() throws IOException {
        for(var toxic : postgresProxy.toxics().getAll()){
            toxic.remove();
        }
    }

    @Test
    @Order(1)
    public void check_session_create(){
        var session = new Session("Test Session 1", "OCARINA", LocalDate.now().plusDays(1), Duration.ofHours(1), List.of("Speaker 1"));
        sessionManagementService.createSession(session);
        var currentSessions = sessionManagementService.getSessions();
        assertThat(currentSessions).hasSize(1);
    }

    @Test
    @Order(2)
    @Disabled("With @Retry should not pass")
    public void check_session_creation_error_on_db_failure() throws IOException, InterruptedException {
        postgresProxy.toxics().bandwidth("CUT_CONNECTION_DOWN", ToxicDirection.DOWNSTREAM, 0);
        postgresProxy.toxics().bandwidth("CUT_CONNECTION_UP", ToxicDirection.UPSTREAM, 0);

        TimeUnit.SECONDS.sleep(1);

        var session = new Session("Test Session 1", "OCARINA", LocalDate.now().plusDays(1), Duration.ofHours(1), List.of("Speaker 1"));
        assertThat(
                catchThrowable(() -> sessionManagementService.createSession(session))
        ).hasCauseInstanceOf(JDBCConnectionException.class);
    }

    @Test
    @Order(3)
    public void check_session_get_retry_on_db_failure() throws IOException, InterruptedException {
        var session = new Session("Test Session 2", "ESPERANZA", LocalDate.now().plusDays(1), Duration.ofHours(1), List.of("Speaker 2"));
        sessionManagementService.createSession(session);
        long sessionId = session.getId();

        postgresProxy.toxics().bandwidth("cut-connection-downstream", ToxicDirection.DOWNSTREAM, 0);
        postgresProxy.toxics().bandwidth("cut-connection-upstream", ToxicDirection.UPSTREAM, 0);

        TimeUnit.SECONDS.sleep(1);

        var result = sessionManagementService.getSession(sessionId);
        assertThat(result).isPresent();
        assertThat(result.get().getTitle()).isEqualTo(session.getTitle());
        assertThat(result.get().getVenue()).isEqualTo(session.getVenue());
    }

    @Test
    @Order(4)
    public void check_session_creation_on_db_failure() throws IOException, InterruptedException {
        var session = new Session("Test Session 3", "ESPERANZA", LocalDate.now().plusDays(1), Duration.ofHours(1), List.of("Speaker 1"));

        postgresProxy.toxics().bandwidth("cut-connection-downstream", ToxicDirection.DOWNSTREAM, 0);
        postgresProxy.toxics().bandwidth("cut-connection-upstream", ToxicDirection.UPSTREAM, 0);

        TimeUnit.SECONDS.sleep(1);

        var createdSession = sessionManagementService.createSession(session);

        assertThat(createdSession.getTitle()).isEqualTo(session.getTitle());
        assertThat(createdSession.getVenue()).isEqualTo(session.getVenue());
    }

    @Test
    @Order(5)
    public void get_sessions_using_date_with_latency() throws IOException, InterruptedException, ExecutionException {
        var tomorrow = LocalDate.now().plusDays(1);
        var session1 = new Session("Test Session 1", "ESPERANZA", tomorrow, Duration.ofHours(1), List.of("Speaker 1"));
        var session2 = new Session("Test Session 2", "ESPERANZA", tomorrow, Duration.ofHours(1), List.of("Speaker 1", "Speaker 2"));
        var session3 = new Session("Test Session 3", "OCARINA", tomorrow.plusDays(1), Duration.ofHours(1), List.of("Speaker 3"));
        sessionManagementService.createSessions(List.of(session1, session2, session3));

        postgresProxy.toxics().latency("latency-upstream", ToxicDirection.UPSTREAM, 5_000);
        postgresProxy.toxics().latency("latency-downstream", ToxicDirection.DOWNSTREAM, 5_000);

        TimeUnit.MILLISECONDS.sleep(125);

        var result = sessionManagementService.getSessions(tomorrow);
        assertThat(result).succeedsWithin(Duration.ofSeconds(10));
        assertThat(result.get()).hasSize(2);
    }
}
