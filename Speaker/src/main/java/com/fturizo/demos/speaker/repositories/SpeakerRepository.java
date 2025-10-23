package com.fturizo.demos.speaker.repositories;

import com.fturizo.demos.speaker.entities.Speaker;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.stream.Collectors;

public interface SpeakerRepository extends MongoRepository<Speaker, Integer> {

    List<Speaker> findByOrganization(String organization);

    default boolean allNamesExists(List<String> names){
        var allNames = findAll()
                .stream()
                .map(Speaker::getName)
                .collect(Collectors.toSet());
        return allNames.containsAll(names);
    }
}
