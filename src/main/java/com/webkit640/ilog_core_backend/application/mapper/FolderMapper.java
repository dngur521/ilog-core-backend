package com.webkit640.ilog_core_backend.application.mapper;

import com.webkit640.ilog_core_backend.api.response.FolderResponse;
import com.webkit640.ilog_core_backend.domain.model.*;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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

    public FolderResponse.Find toFind(Folder folder, List<Folder> childFolders, List<Minutes> minutesList, Member participant) {
        Map<Long, LocalDateTime> folderApproaches = new HashMap<>();
        for(Folder child: childFolders){
            LocalDateTime approachedAt = Optional.ofNullable(child.getFolderParticipants())
                    .orElse(List.of())
                    .stream()
                    .filter(fp->fp.getParticipant() != null && participant.getId().equals(fp.getParticipant().getId()))
                    .map(FolderParticipant::getApproachedAt)
                    .findFirst()
                    .orElse(null);

            folderApproaches.put(child.getId(), approachedAt);
        }
        Map<Long, LocalDateTime> minutesApproaches = new HashMap<>();
        for(Minutes minutes : minutesList){
            LocalDateTime approachedAt = Optional.ofNullable(minutes.getMinutesParticipants())
                    .orElse(List.of())
                    .stream()
                    .filter(mp -> mp.getParticipant() != null && participant.getId().equals(mp.getParticipant().getId()))
                    .map(MinutesParticipant::getApproachedAt)
                    .findFirst()
                    .orElse(null);

            minutesApproaches.put(minutes.getId(), approachedAt);
        }
        List<FolderResponse.FolderSummary> folderSummaries = childFolders.stream()
                .map(f-> new FolderResponse.FolderSummary(
                        f.getId(),
                        f.getFolderName(),
                        folderApproaches.get(f.getId())
                ))
                .toList();
        List<FolderResponse.MinutesSummary> minutesSummaries = minutesList.stream()
                .map(m-> new FolderResponse.MinutesSummary(
                        m.getId(),
                        m.getTitle(),
                        minutesApproaches.get(m.getId())
                ))
                .toList();

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
}
