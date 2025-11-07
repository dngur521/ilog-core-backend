package com.webkit640.ilog_core_backend.domain.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.webkit640.ilog_core_backend.domain.model.Folder;
import com.webkit640.ilog_core_backend.domain.model.FolderParticipant;
import com.webkit640.ilog_core_backend.domain.model.Member;

public interface FolderParticipantDAO extends JpaRepository<FolderParticipant, Long> {

    List<FolderParticipant> findByFolder(Folder folder);

    Optional<FolderParticipant> findByFolderAndParticipant(Folder folder, Member participant);

    //삭제할 폴더의 참가자 조회
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("delete from FolderParticipant fp where fp.folder = :folder and fp.participant = :participant")
    void deleteByFolderAndParticipant(@Param("folder") Folder folder,
            @Param("participant") Member participant);

    boolean existsByFolderAndParticipant(Folder folder, Member participant);

    void deleteAllByParticipant(Member member);
}
