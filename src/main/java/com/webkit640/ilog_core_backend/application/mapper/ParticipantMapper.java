package com.webkit640.ilog_core_backend.application.mapper;

import com.webkit640.ilog_core_backend.api.response.ParticipantResponse;
import com.webkit640.ilog_core_backend.domain.model.FolderParticipant;
import com.webkit640.ilog_core_backend.domain.model.MinutesParticipant;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ParticipantMapper {
    // FolderParticipant → Response.Participant 변환
    private ParticipantResponse.FolderParticipant toFolderParticipant(FolderParticipant entity) {
        return new ParticipantResponse.FolderParticipant(
                entity.getId(),
                entity.getFolder().getId(),
                entity.getParticipant().getId(),
                entity.getParticipant().getName()
        );
    }
    private ParticipantResponse.MinutesParticipant toMinutesParticipant(MinutesParticipant entity) {
        return new ParticipantResponse.MinutesParticipant(
                entity.getId(),
                entity.getMinutes().getId(),
                entity.getParticipant().getId(),
                entity.getParticipant().getName()
        );
    }
    public ParticipantResponse.Detail<ParticipantResponse.FolderParticipant> toFolderDetail(List<FolderParticipant> folderParticipantList) {
        List<ParticipantResponse.FolderParticipant> participants = folderParticipantList.stream()
                .map(this::toFolderParticipant)
                .toList();
        return new ParticipantResponse.Detail<>(participants);
    }

    public ParticipantResponse.Detail<ParticipantResponse.MinutesParticipant> toMinutesDetail(List<MinutesParticipant> minutesParticipantList) {
        List<ParticipantResponse.MinutesParticipant> participants = minutesParticipantList.stream()
                .map(this::toMinutesParticipant)
                .toList();
        return new ParticipantResponse.Detail<>(participants);
    }

}
