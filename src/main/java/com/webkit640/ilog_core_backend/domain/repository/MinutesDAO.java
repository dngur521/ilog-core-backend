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
        SELECT DISTINCT new com.webkit640.ilog_core_backend.api.response.FolderResponse$MinutesSummary(
            m.id, m.title, mp.approachedAt, m.createdAt, m.updatedAt
        )
        FROM Minutes m
        JOIN m.folder f
        JOIN f.folderParticipants fp
        LEFT JOIN m.minutesParticipants mp 
               ON mp.participant.id = :userId
        WHERE f = :folder
        AND fp.participant.id = :userId
        ORDER BY m.createdAt ASC
    """)
    List<FolderResponse.MinutesSummary> findByFolderAndParticipantsOrderByCreatedAtAsc(
            @Param("folder") Folder folder,
            @Param("userId") Long userId);
    @Query(value = """
            SELECT DISTINCT new com.webkit640.ilog_core_backend.api.response.FolderResponse$MinutesSummary(
                m.id, m.title, mp.approachedAt, m.createdAt, m.updatedAt
            )
            FROM Minutes m
            JOIN m.folder f
            JOIN f.folderParticipants fp
            LEFT JOIN m.minutesParticipants mp 
                   ON mp.participant.id = :userId
            WHERE f = :folder
            AND fp.participant.id = :userId
            ORDER BY m.createdAt DESC""")
    List<FolderResponse.MinutesSummary> findByFolderAndParticipantsOrderByCreatedAtDesc(
            @Param("folder") Folder folder, @Param("userId") Long userId);

    @Query(value = """
            SELECT DISTINCT new com.webkit640.ilog_core_backend.api.response.FolderResponse$MinutesSummary(
                m.id, m.title, mp.approachedAt, m.createdAt, m.updatedAt
            )
            FROM Minutes m
            JOIN m.folder f
            JOIN f.folderParticipants fp
            LEFT JOIN m.minutesParticipants mp 
                   ON mp.participant.id = :userId
            WHERE f = :folder
            AND fp.participant.id = :userId
            ORDER BY m.updatedAt ASC""")
    List<FolderResponse.MinutesSummary> findByFolderAndParticipantsOrderByUpdatedAtAsc(
            @Param("folder") Folder folder, @Param("userId") Long userId);

    @Query(value = """
            SELECT DISTINCT new com.webkit640.ilog_core_backend.api.response.FolderResponse$MinutesSummary(
                m.id, m.title, mp.approachedAt, m.createdAt, m.updatedAt
            )
            FROM Minutes m
            JOIN m.folder f
            JOIN f.folderParticipants fp
            LEFT JOIN m.minutesParticipants mp 
                   ON mp.participant.id = :userId
            WHERE f = :folder
            AND fp.participant.id = :userId
            ORDER BY m.updatedAt DESC""")
    List<FolderResponse.MinutesSummary> findByFolderAndParticipantsOrderByUpdatedAtDesc(
            @Param("folder") Folder folder, @Param("userId") Long userId);

    @Query(value = """
            SELECT DISTINCT new com.webkit640.ilog_core_backend.api.response.FolderResponse$MinutesSummary(
                m.id, m.title, mp.approachedAt, m.createdAt, m.updatedAt
            )
            FROM Minutes m
            JOIN m.folder f
            JOIN f.folderParticipants fp
            LEFT JOIN m.minutesParticipants mp 
                   ON mp.participant.id = :userId
            WHERE f = :folder
            AND fp.participant.id = :userId
            ORDER BY m.title ASC""")
    List<FolderResponse.MinutesSummary> findByFolderAndParticipantsOrderByNameAsc(
            @Param("folder") Folder folder, @Param("userId") Long userId);

    @Query(value = """
            SELECT DISTINCT new com.webkit640.ilog_core_backend.api.response.FolderResponse$MinutesSummary(
                m.id, m.title, mp.approachedAt, m.createdAt, m.updatedAt
            )
            FROM Minutes m
            JOIN m.folder f
            JOIN f.folderParticipants fp
            LEFT JOIN m.minutesParticipants mp 
                   ON mp.participant.id = :userId
            WHERE f = :folder
            AND fp.participant.id = :userId
            ORDER BY m.title DESC""")
    List<FolderResponse.MinutesSummary> findByFolderAndParticipantsOrderByNameDesc(
            @Param("folder") Folder folder, @Param("userId") Long userId);

    @Query(value = """
            SELECT DISTINCT new com.webkit640.ilog_core_backend.api.response.FolderResponse$MinutesSummary(
                m.id, m.title, mp.approachedAt, m.createdAt, m.updatedAt
            )
            FROM Minutes m
            JOIN m.folder f
            JOIN f.folderParticipants fp
            LEFT JOIN m.minutesParticipants mp 
                   ON mp.participant.id = :userId
            WHERE f = :folder
            AND fp.participant.id = :userId
            ORDER BY mp.approachedAt ASC""")
    List<FolderResponse.MinutesSummary> findByFolderAndParticipantsOrderByApproachedAtAsc(
            @Param("folder") Folder folder, @Param("userId") Long userId);

    @Query(value = """
            SELECT DISTINCT new com.webkit640.ilog_core_backend.api.response.FolderResponse$MinutesSummary(
                m.id, m.title, mp.approachedAt, m.createdAt, m.updatedAt
            )
            FROM Minutes m
            JOIN m.folder f
            JOIN f.folderParticipants fp
            LEFT JOIN m.minutesParticipants mp 
                   ON mp.participant.id = :userId
            WHERE f = :folder
            AND fp.participant.id = :userId
            ORDER BY mp.approachedAt DESC""")
    List<FolderResponse.MinutesSummary> findByFolderAndParticipantsOrderByApproachedAtDesc(
            @Param("folder") Folder folder, @Param("userId") Long userId);


    List<Minutes> findByFolder(Folder folder);

    @Query("""
    SELECT new com.webkit640.ilog_core_backend.api.response.FolderResponse$MinutesSummary(
                m.id, m.title, mp.approachedAt, m.createdAt, m.updatedAt
    )
    FROM Minutes m
    JOIN m.minutesParticipants mp
    WHERE m.title LIKE %:keyword%
      AND mp.participant.id = :userId
""")
    List<FolderResponse.MinutesSummary> findByTitleAndParticipant(@Param("keyword") String keyword, @Param("userId") Long userId);

    // 삭제 여부 판단용 (성능 최적화)
    @Query("""
    SELECT COUNT(DISTINCT m.id)
    FROM Minutes m 
    JOIN m.minutesParticipants mp
    WHERE m.folder = :folder
    AND mp.participant.id = :userId
""")
    int countByFolderAndParticipants(@Param("folder") Folder folder, @Param("userId") Long userId);

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
