package com.fturizo.demos.speaker;

import com.fturizo.demos.speaker.util.MailpitClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.web.client.RestClient;

@TestConfiguration(proxyBeanMethods = false)
@Profile("email")
class TestcontainersConfiguration {

    @Bean
    public ApplicationRunner logMailServerAccess(@Value("${spring.mail.host}") String host, @Value("${mail.server.web.port}") int port) {
        Logger log = LoggerFactory.getLogger(getClass());
        return args -> log.info("Mail service web console accessible in http://{}:{}", host, port);
    }

    @Bean
    RestClient mailpitRestClient(RestClient.Builder builder,
                                 @Value("${spring.mail.host}") String host,
                                 @Value("${mail.server.web.port}") int port) {
        return builder
                .baseUrl("http://%s:%d/api/v1".formatted(host, port))
                .build();
    }

    @Bean
    MailpitClient mailpitClient(RestClient mailpitRestClient) {
        return new MailpitClient(mailpitRestClient);
    }
}
