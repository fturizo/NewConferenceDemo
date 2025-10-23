package com.fturizo.demos.speaker.entities;

import org.bson.Document;
import org.springframework.data.mongodb.core.mapping.event.BeforeSaveCallback;
import org.springframework.stereotype.Component;

@Component
public class SpeakerSaver implements BeforeSaveCallback<Speaker> {

    @Override
    public Speaker onBeforeSave(Speaker entity, Document document, String collection) {
        entity.verifyRegisteredDate();
        return entity;
    }
}
