package com.webkit640.ilog_core_backend.application.mapper;

import org.springframework.stereotype.Component;

import com.webkit640.ilog_core_backend.api.response.MeetingResponse;
import com.webkit640.ilog_core_backend.domain.model.Meeting;
import com.webkit640.ilog_core_backend.domain.model.Minutes;

@Component
public class MeetingMapper {

    public MeetingResponse.Create toCreate(Meeting meeting) {
        return new MeetingResponse.Create(
                meeting.getMeetingAddress(),
                meeting.getStatus());
    }

    public MeetingResponse.Join toJoin(Meeting meeting) {
        //-----------------join에 넣을거 미정-----------------------
        return new MeetingResponse.Join(
                meeting.getId()
        );
    }

    public MeetingResponse.End toEnd(Minutes minutes) {
        return new MeetingResponse.End(
                minutes.getId()
        );
    }
}
