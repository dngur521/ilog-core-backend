package com.webkit640.ilog_core_backend.domain.event;

import com.webkit640.ilog_core_backend.domain.model.Folder;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class FolderDeletedEvent extends ApplicationEvent {
    private final Folder folder;

    public FolderDeletedEvent(Object source, Folder folder){
        super(source);
        this.folder = folder;
    }
}
