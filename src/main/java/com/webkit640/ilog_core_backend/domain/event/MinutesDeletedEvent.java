package com.webkit640.ilog_core_backend.domain.event;

import com.webkit640.ilog_core_backend.domain.model.Folder;
import com.webkit640.ilog_core_backend.domain.model.Minutes;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class MinutesDeletedEvent extends ApplicationEvent {
    private final Minutes minutes;

    public MinutesDeletedEvent(Object source, Minutes minutes){
        super(source);
        this.minutes = minutes;
    }
}
