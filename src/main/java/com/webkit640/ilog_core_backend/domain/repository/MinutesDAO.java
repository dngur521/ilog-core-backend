package com.webkit640.ilog_core_backend.domain.repository;

import java.util.List;

import com.webkit640.ilog_core_backend.api.response.FolderResponse;
import com.webkit640.ilog_core_backend.api.response.MinutesResponse;
import org.springframework.data.jpa.repository.JpaRepository;

import com.webkit640.ilog_core_backend.domain.model.Folder;
import com.webkit640.ilog_core_backend.domain.model.Minutes;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MinutesDAO extends JpaRepository<Minutes, Long> {
    @Query("""
        SELECT new com.webkit640.ilog_core_backend.api.response.FolderResponse$MinutesFlatDTO(
            m.id,
            m.title,
            mp2.participant.id,
            mp2.participant.name,
            mp2.participant.email,
            mp2.participant.profileImage,
            mp2.approachedAt,
            m.createdAt,
            m.updatedAt,
            f.folderImage
        )
        FROM Minutes m
        JOIN m.folder f
        JOIN f.folderParticipants fp
        LEFT JOIN m.minutesParticipants mp2
        WHERE fp.participant.id = :userId
          AND f.id = :folderId
    """)
    List<FolderResponse.MinutesFlatDTO> findByFolderAndParticipantsOrderByApproachedAtAsc(
            @Param("folderId") Long folderId,
            @Param("userId") Long userId
    );


    List<Minutes> findByFolder(Folder folder);

//    @Query("""
//    SELECT new com.webkit640.ilog_core_backend.api.response.FolderResponse$MinutesSummary(
//                m.id, m.title, mp.approachedAt, m.createdAt, m.updatedAt
//    )
//    FROM Minutes m
//    JOIN m.minutesParticipants mp
//    WHERE m.title LIKE %:keyword%
//      AND mp.participant.id = :userId
//""")
//    List<FolderResponse.MinutesSummary> findByTitleAndParticipant(@Param("keyword") String keyword, @Param("userId") Long userId);

    @Query("""
        SELECT new com.webkit640.ilog_core_backend.api.response.MinutesResponse$Calender(
            m.id,
            m.title,
            m.createdAt,
            m.updatedAt,
            f.folderName
        )
        FROM Minutes m
        JOIN m.folder f
        JOIN f.folderParticipants fp
        WHERE fp.participant.id = :participantId
    """)
    List<MinutesResponse.Calender> findAllCalendarByParticipantId(@Param("participantId") Long participantId);

}
