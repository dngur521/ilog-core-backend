package com.webkit640.ilog_core_backend.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(
        name = "folder_participant",
        uniqueConstraints = @UniqueConstraint(columnNames = {"folder_id", "participant_id"})
)
@Setter
@Getter
public class FolderParticipant extends CommonParticipant {

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "folder_id")
    private Folder folder;
}
