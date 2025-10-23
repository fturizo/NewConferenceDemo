package com.fturizo.demos.session.entities;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fturizo.demos.session.converters.SpeakersConverter;
import jakarta.persistence.*;

import java.io.Serializable;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name= "t_sessions")
@SuppressWarnings("unused")
public class Session implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String venue;

    @Column(name = "session_date")
    private LocalDate date;
    private Duration duration;

    @Convert(converter = SpeakersConverter.class)
    private List<String> speakers;

    public Session() {
    }

    @JsonCreator
    public Session(@JsonProperty("title") String title,
                   @JsonProperty("venue") String venue,
                   @JsonProperty("date") LocalDate date,
                   @JsonProperty("duration") Duration duration,
                   @JsonProperty("speakers") List<String> speakers) {
        this.title = title;
        this.date = date;
        this.venue = venue;
        this.duration = duration;
        this.speakers = speakers;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public List<String> getSpeakers() {
        return speakers;
    }

    public String getVenue() {
        return venue;
    }

    public LocalDate getDate() {
        return date;
    }

    public Duration getDuration() {
        return duration;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + Objects.hashCode(this.id);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Session other = (Session) obj;
        return Objects.equals(this.id, other.id);
    }
}
