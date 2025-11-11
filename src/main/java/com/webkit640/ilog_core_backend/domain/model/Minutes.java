package com.webkit640.ilog_core_backend.domain.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Setter
@Getter
public class Minutes {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    @Column(columnDefinition = "TEXT")
    private String content;
    @Column(columnDefinition = "TEXT")
    private String summary;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    @Column(nullable = false)
    private MinutesType status;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "folder_id")
    private Folder folder;
    @OneToMany(mappedBy = "minutes", cascade = CascadeType.ALL, orphanRemoval = false)
    private List<MinutesParticipant> minutesParticipants = new ArrayList<>();
    @OneToMany(mappedBy = "minutes", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Memo> memos = new ArrayList<>();
}
