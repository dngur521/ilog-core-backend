package com.webkit640.ilog_core_backend.application.Listener;

import com.webkit640.ilog_core_backend.domain.event.FolderDeletedEvent;
import com.webkit640.ilog_core_backend.domain.event.MinutesDeletedEvent;
import com.webkit640.ilog_core_backend.domain.model.*;
import com.webkit640.ilog_core_backend.domain.repository.ParticipantLogDAO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class ParticipantLogListener {
    private final ParticipantLogDAO participantLogDAO;


    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onFolderDeleted(FolderDeletedEvent event){
        Folder folder = event.getFolder();
        Member owner = folder.getOwner();

        log.info("Folder deleted event received for folderId: {}", folder.getId());

        for (FolderParticipant fp : folder.getFolderParticipants()){
            saveParticipantLog(owner,fp.getParticipant().getEmail(), ParticipantType.FOLDER,"폴더 삭제로 인한 참여자 삭제");
        }

        for(Minutes minutes : folder.getMinutesList()){
            for(MinutesParticipant mp : minutes.getMinutesParticipants()){
                saveParticipantLog(owner, mp.getParticipant().getEmail(), ParticipantType.MINUTES,"폴더 삭제로 인한 회의록 참여자 제거");
            }
        }
    }
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onMinutesDeleted(MinutesDeletedEvent event){
        Minutes minutes = event.getMinutes();
        Member owner = minutes.getFolder().getOwner();

        log.info("Minutes deleted event received for minutesId: {}", minutes.getId());

        for(MinutesParticipant mp : minutes.getMinutesParticipants()){
            saveParticipantLog(owner,mp.getParticipant().getEmail(),ParticipantType.MINUTES,"회의록 삭제로 인한 회의록 참여자 제거");
        }
    }
    private void saveParticipantLog(Member owner, String targetEmail, ParticipantType type, String desc){
        ParticipantLog logEntity = new ParticipantLog();
        logEntity.setUserId(owner.getId());
        logEntity.setEmail(owner.getEmail());
        logEntity.setCreatedAt(LocalDateTime.now());
        logEntity.setParticipantEmail(targetEmail);
        logEntity.setParticipantType(type);
        logEntity.setStatus(ActionType.DELETE);
        logEntity.setDescription(desc);

        participantLogDAO.save(logEntity);
    }
}
