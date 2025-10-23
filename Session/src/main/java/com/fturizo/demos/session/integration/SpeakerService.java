package com.fturizo.demos.session.integration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import java.util.List;

@Configuration
@SuppressWarnings("unused")
public class SpeakerService {

    @Value("${conference.demo.services.speaker.url}")
    private String baseUrl;

    @Bean
    public HttpServiceProxyFactory httpServiceProxyFactory() {
        var client = RestClient.builder()
                .baseUrl(baseUrl)
                .build();

        return HttpServiceProxyFactory.builderFor(RestClientAdapter.create(client)).build();
    }

    @Bean
    public SpeakerServiceClient speakerServiceClient(HttpServiceProxyFactory factory){
        return factory.createClient(SpeakerServiceClient.class);
    }

    public interface SpeakerServiceClient {

        @HttpExchange(url = "/speaker?names={names}", method = "HEAD")
        ResponseEntity<Void> checkSpeakers(@RequestParam List<String> names);
    }
}
