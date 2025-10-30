package com.webkit640.ilog_core_backend.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Setter
@Getter
public class Folder {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne @JsonIgnore
    private Folder parentFolder;
    @OneToMany(mappedBy = "parentFolder", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Folder> childFolders = new ArrayList<>();
    @ManyToOne
    private Member owner;
    private String folderName;
    @OneToMany(mappedBy = "folder", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FolderParticipant> folderParticipants = new ArrayList<>();
    @OneToMany(mappedBy = "folder", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Minutes> minutesList = new ArrayList<>();
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String imageUrl;
}
