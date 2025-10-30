package com.webkit640.ilog_core_backend.domain.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class Meeting {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    private Member owner;
    private String meetingAddress;
    private LocalDateTime createdAt;
    private MeetingType status;
}
