package com.webkit640.ilog_core_backend.domain.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Setter
@Getter
public class Minutes {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    private String content;
    private String summary;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private MinutesType status;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "folder_id")
    private Folder folder;
    @OneToMany(mappedBy = "minutes", cascade = CascadeType.ALL, orphanRemoval = false)
    private List<MinutesParticipant> minutesParticipants = new ArrayList<>();
}
