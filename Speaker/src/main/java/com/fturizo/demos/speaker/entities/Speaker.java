package com.fturizo.demos.speaker.entities;

import com.fasterxml.jackson.annotation.JsonCreator;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDate;
import java.util.Objects;

@SuppressWarnings("unused")
public class Speaker{

    @Id
    private String id;

    private String name;

    @Indexed(unique = true)
    @Field(name = "email_address")
    private String email;

    private String organization;

    private LocalDate registeredAt;

    public Speaker() {
    }

    @JsonCreator
    public Speaker(String name, String email, String organization) {
        this.name = name;
        this.email = email;
        this.organization = organization;
        this.registeredAt = LocalDate.now();
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getOrganization() {
        return organization;
    }

    public LocalDate getRegisteredAt() {
        return registeredAt;
    }

    @Override
    public int hashCode() {
        var hash = 7;
        hash = 79 * hash + Objects.hashCode(this.id);
        return hash;
    }

    protected void verifyRegisteredDate(){
        if(registeredAt == null){
            this.registeredAt = LocalDate.now();
        }
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
        final Speaker other = (Speaker) obj;
        return Objects.equals(this.id, other.id);
    }
}
