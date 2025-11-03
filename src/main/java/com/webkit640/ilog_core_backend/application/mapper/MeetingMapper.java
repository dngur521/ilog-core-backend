package com.webkit640.ilog_core_backend.application.mapper;

import com.webkit640.ilog_core_backend.api.response.MeetingResponse;
import com.webkit640.ilog_core_backend.api.response.MinutesResponse;
import com.webkit640.ilog_core_backend.domain.model.Memo;
import com.webkit640.ilog_core_backend.domain.model.Minutes;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MeetingMapper {
    public MeetingResponse.End toEnd(Minutes minutes){
        return new MeetingResponse.End(
            minutes.getId(), minutes.getTitle(), minutes.getContent(), minutes.getSummary()
        );
    }

}
