package com.webkit640.ilog_core_backend.domain.model;

import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class ParticipantLog extends CommonLog {
    private String participantEmail;
    private ParticipantType participantType;
}
