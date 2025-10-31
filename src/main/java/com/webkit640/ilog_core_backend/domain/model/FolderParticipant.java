package com.webkit640.ilog_core_backend.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

@Entity
@Setter
@Getter
public class FolderParticipant extends CommonParticipant {

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "folder_id")
    private Folder folder;
}
