package com.webkit640.ilog_core_backend.application.mapper;

import java.util.List;

import org.springframework.stereotype.Component;

import com.webkit640.ilog_core_backend.api.response.ParticipantResponse;
import com.webkit640.ilog_core_backend.domain.model.FolderParticipant;
import com.webkit640.ilog_core_backend.domain.model.MinutesParticipant;

@Component
public class FolderParticipantMapper {

    // FolderParticipant → Response.Participant 변환
    private ParticipantResponse.Participant toFolderParticipant(FolderParticipant entity) {
        return new ParticipantResponse.Participant(
                entity.getId(),
                entity.getFolder().getId(),
                entity.getParticipant().getId()
        );
    }

    private ParticipantResponse.Participant toMinutesParticipant(MinutesParticipant entity) {
        return new ParticipantResponse.Participant(
                entity.getId(),
                entity.getMinutes().getId(),
                entity.getParticipant().getId()
        );
    }

    public ParticipantResponse.Detail toFolderDetail(List<FolderParticipant> folderParticipantList) {
        List<ParticipantResponse.Participant> participants = folderParticipantList.stream()
                .map(this::toFolderParticipant)
                .toList();
        return new ParticipantResponse.Detail(participants);
    }

//    public ParticipantResponse.Detail toFolderDelete(List<FolderParticipant> folderParticipantList) {
//        List<ParticipantResponse.Participant> participants = folderParticipantList.stream()
//                .map(this::toFolderParticipant)
//                .toList();
//        return new ParticipantResponse.Detail(participants);
//    }
    public ParticipantResponse.Detail toMinutesDetail(List<MinutesParticipant> minutesParticipantList) {
        List<ParticipantResponse.Participant> participants = minutesParticipantList.stream()
                .map(this::toMinutesParticipant)
                .toList();
        return new ParticipantResponse.Detail(participants);
    }

//    public ParticipantResponse.Detail toMinutesDelete(List<MinutesParticipant> minutesParticipantList) {
//        List<ParticipantResponse.Participant> participants = minutesParticipantList.stream()
//                .map(this::toMinutesParticipant)
//                .toList();
//        return new ParticipantResponse.Detail(participants);
//    }
}
