package com.webkit640.ilog_core_backend.domain.repository;

import java.util.List;

import com.webkit640.ilog_core_backend.api.response.FolderResponse;
import com.webkit640.ilog_core_backend.infrastructure.security.CustomUserDetails;
import org.springframework.data.jpa.repository.JpaRepository;

import com.webkit640.ilog_core_backend.domain.model.Folder;
import com.webkit640.ilog_core_backend.domain.model.Minutes;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MinutesDAO extends JpaRepository<Minutes, Long> {

    @Query(value = """
            SELECT DISTINCT new com.webkit640.ilog_core_backend.api.response.FolderResponse$MinutesSummary(
                m.id, m.title, m.createdAt, mp.approachedAt
            )
            FROM Minutes m 
            JOIN m.minutesParticipants mp
            WHERE m.folder = :folder 
            ORDER BY m.createdAt ASC""")
    List<FolderResponse.MinutesSummary> findByFolderAndParticipantsOrderByCreatedAtAsc(@Param("folder") Folder folder);

    @Query(value = """
            SELECT DISTINCT new com.webkit640.ilog_core_backend.api.response.FolderResponse$MinutesSummary(
                m.id, m.title, m.createdAt, mp.approachedAt
            )
            FROM Minutes m 
            JOIN m.minutesParticipants mp
            WHERE m.folder = :folder 
            ORDER BY m.createdAt DESC""")
    List<FolderResponse.MinutesSummary> findByFolderAndParticipantsOrderByCreatedAtDesc(@Param("folder") Folder folder);

    @Query(value = """
            SELECT DISTINCT new com.webkit640.ilog_core_backend.api.response.FolderResponse$MinutesSummary(
                m.id, m.title, m.createdAt, mp.approachedAt
            )
            FROM Minutes m 
            JOIN m.minutesParticipants mp
            WHERE m.folder = :folder 
            ORDER BY m.updatedAt ASC""")
    List<FolderResponse.MinutesSummary> findByFolderAndParticipantsOrderByUpdatedAtAsc(@Param("folder") Folder folder);

    @Query(value = """
            SELECT DISTINCT new com.webkit640.ilog_core_backend.api.response.FolderResponse$MinutesSummary(
                m.id, m.title, m.createdAt, mp.approachedAt
            )
            FROM Minutes m 
            JOIN m.minutesParticipants mp
            WHERE m.folder = :folder 
            ORDER BY m.updatedAt DESC""")
    List<FolderResponse.MinutesSummary> findByFolderAndParticipantsOrderByUpdatedAtDesc(@Param("folder") Folder folder);

    @Query(value = """
            SELECT DISTINCT new com.webkit640.ilog_core_backend.api.response.FolderResponse$MinutesSummary(
                m.id, m.title, m.createdAt, mp.approachedAt
            )
            FROM Minutes m 
            JOIN m.minutesParticipants mp
            WHERE m.folder = :folder 
            ORDER BY m.title ASC""")
    List<FolderResponse.MinutesSummary> findByFolderAndParticipantsOrderByNameAsc(@Param("folder") Folder folder);

    @Query(value = """
            SELECT DISTINCT new com.webkit640.ilog_core_backend.api.response.FolderResponse$MinutesSummary(
                m.id, m.title, m.createdAt, mp.approachedAt
            )
            FROM Minutes m 
            JOIN m.minutesParticipants mp
            WHERE m.folder = :folder 
            ORDER BY m.title DESC""")
    List<FolderResponse.MinutesSummary> findByFolderAndParticipantsOrderByNameDesc(@Param("folder") Folder folder);

    @Query(value = """
            SELECT DISTINCT new com.webkit640.ilog_core_backend.api.response.FolderResponse$MinutesSummary(
                m.id, m.title, m.createdAt, mp.approachedAt
            )
            FROM Minutes m 
            JOIN m.minutesParticipants mp
            WHERE m.folder = :folder 
            ORDER BY mp.approachedAt ASC""")
    List<FolderResponse.MinutesSummary> findByFolderAndParticipantsOrderByApproachedAtAsc(@Param("folder") Folder folder);

    @Query(value = """
            SELECT DISTINCT new com.webkit640.ilog_core_backend.api.response.FolderResponse$MinutesSummary(
                m.id, m.title, m.createdAt, mp.approachedAt
            )
            FROM Minutes m 
            JOIN m.minutesParticipants mp
            WHERE m.folder = :folder 
            ORDER BY mp.approachedAt DESC""")
    List<FolderResponse.MinutesSummary> findByFolderAndParticipantsOrderByApproachedAtDesc(@Param("folder") Folder folder);


    List<Minutes> findByFolder(Folder folder);

    @Query("""
    SELECT new com.webkit640.ilog_core_backend.api.response.FolderResponse$MinutesSummary(
                m.id, m.title, m.createdAt, mp.approachedAt
    )
    FROM Minutes m
    JOIN m.minutesParticipants mp
    WHERE m.title LIKE %:keyword%
      AND mp.participant.id = :userId
""")
    List<FolderResponse.MinutesSummary> findByTitleAndParticipant(@Param("keyword") String keyword, @Param("userId") Long userId);
}
