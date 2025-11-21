package com.webkit640.ilog_core_backend.application.mapper;

import com.webkit640.ilog_core_backend.api.response.FolderResponse;
import com.webkit640.ilog_core_backend.domain.model.*;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class FolderMapper {
    public FolderResponse.Create toCreate(Folder folder){
        return new FolderResponse.Create(
                folder.getId(),
                folder.getFolderName(),
                folder.getCreatedAt(),
                folder.getFolderImage()
        );
    }

    public FolderResponse.Find toFind(
            Folder folder,
            List<FolderResponse.FolderFlatDTO> childFolders,
            List<FolderResponse.MinutesFlatDTO> minutesList,
            Long userId) {

        List<FolderResponse.FolderSummary> folderSummaries = new ArrayList<>();

        if(childFolders != null){
            Map<Long, FolderResponse.FolderSummary> folderMap = new HashMap<>();
            for(FolderResponse.FolderFlatDTO row : childFolders){
                FolderResponse.FolderSummary fs = folderMap.computeIfAbsent(
                        row.getFolderId(),
                        id -> new FolderResponse.FolderSummary(
                                id,
                                row.getFolderName(),
                                new ArrayList<>(),
                                row.getCreatedAt(),
                                row.getUpdatedAt(),
                                row.getFolderImage()
                        )
                );

                fs.getFolderParticipants().add(
                        new FolderResponse.GetFolderParticipant(
                                row.getParticipantId(),
                                row.getParticipantName(),
                                row.getParticipantEmail(),
                                row.getParticipantProfileImage(),
                                row.getApproachedAt()
                        )
                );
            }
            folderSummaries = new ArrayList<>(folderMap.values());

            folderSummaries.sort(Comparator.comparing((FolderResponse.FolderSummary fs) ->
                    fs.getFolderParticipants().stream()
                            .filter(p -> p.getParticipantId().equals(userId))
                            .map(FolderResponse.GetFolderParticipant::getApproachedAt)
                            .filter(Objects::nonNull)
                            .findFirst()
                            .orElse(LocalDateTime.MIN)
            ).reversed());
        }

        List<FolderResponse.MinutesSummary> minutesSummaries = new ArrayList<>();

        if(minutesList != null){
            Map<Long, FolderResponse.MinutesSummary> minuetsMap = new HashMap<>();

            for(FolderResponse.MinutesFlatDTO row : minutesList){
                FolderResponse.MinutesSummary ms = minuetsMap.computeIfAbsent(
                        row.getMinutesId(),
                        id -> new FolderResponse.MinutesSummary(
                                id,
                                row.getMinutesName(),
                                new ArrayList<>(),
                                row.getCreatedAt(),
                                row.getUpdatedAt()
                        )
                );

                ms.getMinutesParticipants().add(
                        new FolderResponse.GetMinutesParticipant(
                                row.getParticipantId(),
                                row.getParticipantName(),
                                row.getParticipantEmail(),
                                row.getParticipantProfileImage(),
                                row.getApproachedAt()
                        )
                );
            }
            minutesSummaries = new ArrayList<>(minuetsMap.values());

            minutesSummaries.sort(
                    Comparator.comparing((FolderResponse.MinutesSummary ms)->
                ms.getMinutesParticipants().stream()
                        .filter(p -> p.getParticipantId().equals(userId))
                        .map(FolderResponse.GetMinutesParticipant::getApproachedAt)
                        .filter(Objects::nonNull)
                        .findFirst()
                        .orElse(LocalDateTime.MIN)
            ).reversed());
        }

        return new FolderResponse.Find(
                folder.getId(),
                folder.getFolderName(),
                folderSummaries,
                minutesSummaries,
                folder.getFolderImage()
        );
    }

    public FolderResponse.Update toUpdate(Folder folder){
        return new FolderResponse.Update(
                folder.getId(),
                folder.getFolderName(),
                folder.getUpdatedAt(),
                folder.getFolderImage()
        );
    }

    public List<FolderResponse.MinutesSummary> toSearch(List<FolderResponse.MinutesSummary> minutesList) {
        return minutesList.stream()
                .map(m-> new FolderResponse.MinutesSummary(
                        m.getId(),
                        m.getName(),
                        m.getMinutesParticipants(),
                        m.getCreatedAt(),
                        m.getUpdatedAt()
                ))
                .toList();
    }
}
