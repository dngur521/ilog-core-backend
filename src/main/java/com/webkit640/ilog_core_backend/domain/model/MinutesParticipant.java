package com.webkit640.ilog_core_backend.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(
        name = "minutes_participant",
        uniqueConstraints = @UniqueConstraint(columnNames = {"minutes_id","participant_id"})
)
@Setter
@Getter
public class MinutesParticipant extends CommonParticipant {

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "minutes_id")
    private Minutes minutes;
}
