package com.webkit640.ilog_core_backend.application.mapper;

import com.webkit640.ilog_core_backend.api.response.FolderResponse;
import com.webkit640.ilog_core_backend.domain.model.Folder;
import com.webkit640.ilog_core_backend.domain.model.Minutes;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class FolderMapper {
    public FolderResponse.Create toCreate(Folder folder){
        return new FolderResponse.Create(
                folder.getId(),
                folder.getFolderName(),
                folder.getCreatedAt()
        );
    }

    public FolderResponse.Find toFind(Folder folder, List<Folder> childFolders, List<Minutes> minutesList) {
        return new FolderResponse.Find(
                folder.getId(),
                folder.getFolderName(),
                childFolders.stream().map(f-> new FolderResponse.FolderSummary(f.getId(),f.getFolderName()))
                        .toList(),
                minutesList.stream()
                        .map(m -> new FolderResponse.MinutesSummary(m.getId(), m.getTitle()))
                        .toList()
        );
    }

    public FolderResponse.Update toUpdate(Folder folder){
        return new FolderResponse.Update(
                folder.getId(),
                folder.getFolderName(),
                folder.getUpdatedAt()
        );
    }
}
