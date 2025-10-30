package com.webkit640.ilog_core_backend.application.mapper;

import com.webkit640.ilog_core_backend.api.response.MinutesResponse;
import com.webkit640.ilog_core_backend.domain.model.Minutes;
import org.springframework.stereotype.Component;

@Component
public class MinutesMapper {
    public MinutesResponse.Create toCreate(Minutes minutes){
        return new MinutesResponse.Create(
            minutes.getId(), minutes.getTitle(), minutes.getContent(), minutes.getSummary()
        );
    }

    public MinutesResponse.FindContent toFindContent(Minutes minutes) {
        return new MinutesResponse.FindContent(
                minutes.getId(), minutes.getTitle(), minutes.getContent());
    }

    public MinutesResponse.FindSummary toFindSummary(Minutes minutes) {
        return new MinutesResponse.FindSummary(
                minutes.getId(), minutes.getTitle(), minutes.getSummary());
    }

    public MinutesResponse.Update toUpdate(Minutes minutes) {
        return new MinutesResponse.Update(
                minutes.getId(), minutes.getTitle(), minutes.getContent(), minutes.getSummary());
    }
}
