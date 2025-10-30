package com.webkit640.ilog_core_backend.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Setter
@Getter
public class MinutesParticipant extends CommonParticipant{
    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "minutes_id")
    private Minutes minutes;
}
