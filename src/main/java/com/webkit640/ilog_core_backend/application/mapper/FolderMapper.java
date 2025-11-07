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

    public FolderResponse.Find toFind(Folder folder, List<FolderResponse.FolderSummary> childFolders, List<FolderResponse.MinutesSummary> minutesList) {
        List<FolderResponse.FolderSummary> folderSummaries = childFolders.stream()
                .map(f->
                        new FolderResponse.FolderSummary(
                        f.getId(),
                        f.getName(),
                        f.getApproachedAt(),
                        f.getCreatedAt(),
                        f.getFolderImage()
                ))
                .toList();
        List<FolderResponse.MinutesSummary> minutesSummaries = minutesList.stream()
                .map(m-> new FolderResponse.MinutesSummary(
                        m.getId(),
                        m.getName(),
                        m.getApproachedAt(),
                        m.getCreatedAt()
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

    public List<FolderResponse.MinutesSummary> toSearch(List<FolderResponse.MinutesSummary> minutesList) {
        return minutesList.stream()
                .map(m-> new FolderResponse.MinutesSummary(
                        m.getId(),
                        m.getName(),
                        m.getApproachedAt(),
                        m.getCreatedAt()
                ))
                .toList();
    }
}
