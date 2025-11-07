package com.webkit640.ilog_core_backend.domain.repository;

import com.webkit640.ilog_core_backend.api.response.FolderResponse;
import com.webkit640.ilog_core_backend.domain.model.Folder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FolderDAO extends JpaRepository<Folder, Long> {
    //========================= 루트 폴더 조회 ===========================================
    @Query("""
    SELECT DISTINCT new com.webkit640.ilog_core_backend.api.response.FolderResponse$FolderSummary(
        f.id,
        f.folderName,
        f.createdAt,
        (
            SELECT fp.approachedAt
            FROM FolderParticipant fp
            WHERE fp.folder.id = f.id
              AND fp.participant.id = :userId
            ORDER BY fp.approachedAt DESC
            LIMIT 1
        ),
        f.folderImage
    )
    FROM Folder f
    WHERE 
        (
            f.parentFolder.id = :myRootFolderId
        )
        OR
        (
            f.parentFolder IS NULL
            AND f.owner.id <> :userId
            AND EXISTS (
                SELECT 1 FROM FolderParticipant fp
                WHERE fp.folder.id = f.id
                  AND fp.participant.id = :userId
            )
        )
    ORDER BY f.createdAt ASC
    """)
    List<FolderResponse.FolderSummary> findByRootParentFolderOrderByCreatedAtAsc(
            @Param("myRootFolderId") Long myRootFolderId,
            @Param("userId") Long userId
    );

    @Query("""
    SELECT DISTINCT new com.webkit640.ilog_core_backend.api.response.FolderResponse$FolderSummary(
        f.id,
        f.folderName,
        f.createdAt,
        (
            SELECT fp.approachedAt
            FROM FolderParticipant fp
            WHERE fp.folder.id = f.id
              AND fp.participant.id = :userId
            ORDER BY fp.approachedAt DESC
            LIMIT 1
        ),
        f.folderImage
    )
    FROM Folder f
    WHERE 
        (
            f.parentFolder.id = :myRootFolderId
        )
        OR
        (
            f.parentFolder IS NULL
            AND f.owner.id <> :userId
            AND EXISTS (
                SELECT 1 FROM FolderParticipant fp
                WHERE fp.folder.id = f.id
                  AND fp.participant.id = :userId
            )
        )
    ORDER BY f.createdAt DESC
    """)
    List<FolderResponse.FolderSummary> findByRootParentFolderOrderByCreatedAtDesc(
            @Param("myRootFolderId") Long myRootFolderId,
            @Param("userId") Long userId);

    @Query("""
    SELECT DISTINCT new com.webkit640.ilog_core_backend.api.response.FolderResponse$FolderSummary(
        f.id,
        f.folderName,
        f.createdAt,
        (
            SELECT fp.approachedAt
            FROM FolderParticipant fp
            WHERE fp.folder.id = f.id
              AND fp.participant.id = :userId
            ORDER BY fp.approachedAt DESC
            LIMIT 1
        ),
        f.folderImage
    )
    FROM Folder f
    WHERE 
        (
            f.parentFolder.id = :myRootFolderId
        )
        OR
        (
            f.parentFolder IS NULL
            AND f.owner.id <> :userId
            AND EXISTS (
                SELECT 1 FROM FolderParticipant fp
                WHERE fp.folder.id = f.id
                  AND fp.participant.id = :userId
            )
        )
    ORDER BY f.updatedAt ASC
    """)
    List<FolderResponse.FolderSummary> findByRootParentFolderOrderByUpdatedAtAsc(
            @Param("myRootFolderId") Long myRootFolderId,
            @Param("userId") Long userId);

    @Query("""
    SELECT DISTINCT new com.webkit640.ilog_core_backend.api.response.FolderResponse$FolderSummary(
        f.id,
        f.folderName,
        f.createdAt,
        (
            SELECT fp.approachedAt
            FROM FolderParticipant fp
            WHERE fp.folder.id = f.id
              AND fp.participant.id = :userId
            ORDER BY fp.approachedAt DESC
            LIMIT 1
        ),
        f.folderImage
    )
    FROM Folder f
    WHERE 
        (
            f.parentFolder.id = :myRootFolderId
        )
        OR
        (
            f.parentFolder IS NULL
            AND f.owner.id <> :userId
            AND EXISTS (
                SELECT 1 FROM FolderParticipant fp
                WHERE fp.folder.id = f.id
                  AND fp.participant.id = :userId
            )
        )
    ORDER BY f.updatedAt DESC
    """)
    List<FolderResponse.FolderSummary> findByRootParentFolderOrderByUpdatedAtDesc(
            @Param("myRootFolderId") Long myRootFolderId,
            @Param("userId") Long userId);


    @Query("""
    SELECT DISTINCT new com.webkit640.ilog_core_backend.api.response.FolderResponse$FolderSummary(
        f.id,
        f.folderName,
        f.createdAt,
        (
            SELECT fp.approachedAt
            FROM FolderParticipant fp
            WHERE fp.folder.id = f.id
              AND fp.participant.id = :userId
            ORDER BY fp.approachedAt DESC
            LIMIT 1
        ),
        f.folderImage
    )
    FROM Folder f
    WHERE 
        (
            f.parentFolder.id = :myRootFolderId
        )
        OR
        (
            f.parentFolder IS NULL
            AND f.owner.id <> :userId
            AND EXISTS (
                SELECT 1 FROM FolderParticipant fp
                WHERE fp.folder.id = f.id
                  AND fp.participant.id = :userId
            )
        )
    ORDER BY f.folderName ASC
    """)
    List<FolderResponse.FolderSummary> findByRootParentFolderOrderByNameAsc(
            @Param("myRootFolderId") Long myRootFolderId,
            @Param("userId") Long userId);


    @Query("""
    SELECT DISTINCT new com.webkit640.ilog_core_backend.api.response.FolderResponse$FolderSummary(
        f.id,
        f.folderName,
        f.createdAt,
        (
            SELECT fp.approachedAt
            FROM FolderParticipant fp
            WHERE fp.folder.id = f.id
              AND fp.participant.id = :userId
            ORDER BY fp.approachedAt DESC
            LIMIT 1
        ),
        f.folderImage
    )
    FROM Folder f
    WHERE 
        (
            f.parentFolder.id = :myRootFolderId
        )
        OR
        (
            f.parentFolder IS NULL
            AND f.owner.id <> :userId
            AND EXISTS (
                SELECT 1 FROM FolderParticipant fp
                WHERE fp.folder.id = f.id
                  AND fp.participant.id = :userId
            )
        )
    ORDER BY f.folderName DESC
    """)
    List<FolderResponse.FolderSummary> findByRootParentFolderOrderByNameDesc(
            @Param("myRootFolderId") Long myRootFolderId,
            @Param("userId") Long userId);

    @Query("""
    SELECT DISTINCT new com.webkit640.ilog_core_backend.api.response.FolderResponse$FolderSummary(
        f.id,
        f.folderName,
        f.createdAt,
        (
            SELECT fp.approachedAt
            FROM FolderParticipant fp
            WHERE fp.folder.id = f.id
              AND fp.participant.id = :userId
            ORDER BY fp.approachedAt DESC
            LIMIT 1
        ) AS lastApproach,
        f.folderImage
    )
    FROM Folder f
    WHERE 
        (
            f.parentFolder.id = :myRootFolderId
        )
        OR
        (
            f.parentFolder IS NULL
            AND f.owner.id <> :userId
            AND EXISTS (
                SELECT 1 FROM FolderParticipant fp
                WHERE fp.folder.id = f.id
                  AND fp.participant.id = :userId
            )
        )
    ORDER BY lastApproach ASC NULLS LAST, f.createdAt ASC
    """)
    List<FolderResponse.FolderSummary> findByRootParentFolderOrderByApproachedAtAsc(
            @Param("myRootFolderId") Long myRootFolderId,
            @Param("userId") Long userId);

    @Query("""
    SELECT DISTINCT new com.webkit640.ilog_core_backend.api.response.FolderResponse$FolderSummary(
        f.id,
        f.folderName,
        f.createdAt,
        (
            SELECT fp.approachedAt
            FROM FolderParticipant fp
            WHERE fp.folder.id = f.id
              AND fp.participant.id = :userId
            ORDER BY fp.approachedAt DESC
            LIMIT 1
        ) AS lastApproach,
        f.folderImage
    )
    FROM Folder f
    WHERE 
        (
            f.parentFolder.id = :myRootFolderId
        )
        OR
        (
            f.parentFolder IS NULL
            AND f.owner.id <> :userId
            AND EXISTS (
                SELECT 1 FROM FolderParticipant fp
                WHERE fp.folder.id = f.id
                  AND fp.participant.id = :userId
            )
        )
    ORDER BY lastApproach DESC NULLS LAST, f.createdAt ASC
    """)
    List<FolderResponse.FolderSummary> findByRootParentFolderOrderByApproachedAtDesc(
            @Param("myRootFolderId") Long myRootFolderId,
            @Param("userId") Long userId);

    //========================= 일반 폴더 조회 ===========================================
    @Query("""
        SELECT DISTINCT new com.webkit640.ilog_core_backend.api.response.FolderResponse$FolderSummary(
            f.id, f.folderName, f.createdAt, fp.approachedAt, f.folderImage
        )
        FROM Folder f
        JOIN f.folderParticipants fp
        WHERE f.parentFolder.id = :parentFolderId
          AND fp.participant.id = :userId
        ORDER BY f.createdAt ASC
    """)
    List<FolderResponse.FolderSummary> findByParentFolderOrderByCreatedAtAsc(
            @Param("parentFolderId") Long parentFolderId,
            @Param("userId") Long userId);


    @Query("""
        SELECT DISTINCT new com.webkit640.ilog_core_backend.api.response.FolderResponse$FolderSummary(
            f.id, f.folderName, f.createdAt, fp.approachedAt, f.folderImage
        )
        FROM Folder f
        JOIN f.folderParticipants fp
        WHERE f.parentFolder.id = :parentFolderId
          AND fp.participant.id = :userId
        ORDER BY f.createdAt DESC
    """)
    List<FolderResponse.FolderSummary> findByParentFolderOrderByCreatedAtDesc(
            @Param("parentFolderId") Long parentFolderId,
            @Param("userId") Long userId);

    @Query(value = """
        SELECT DISTINCT new com.webkit640.ilog_core_backend.api.response.FolderResponse$FolderSummary(
            f.id, f.folderName, f.createdAt, fp.approachedAt, f.folderImage
        )
        FROM Folder f
        JOIN f.folderParticipants fp
        WHERE f.parentFolder.id = :parentFolderId
        AND fp.participant.id = :userId
        ORDER BY f.updatedAt ASC""")
    List<FolderResponse.FolderSummary> findByParentFolderOrderByUpdatedAtAsc(
            @Param("parentFolderId") Long parentFolderId,
            @Param("userId") Long userId);

    @Query(value = """
            SELECT DISTINCT new com.webkit640.ilog_core_backend.api.response.FolderResponse$FolderSummary(
                f.id, f.folderName, f.createdAt, fp.approachedAt, f.folderImage
            )
            FROM Folder f
            JOIN f.folderParticipants fp
            WHERE f.parentFolder.id = :parentFolderId
            AND fp.participant.id = :userId
            ORDER BY f.updatedAt DESC""")
    List<FolderResponse.FolderSummary> findByParentFolderOrderByUpdatedAtDesc(
            @Param("parentFolderId") Long parentFolderId,
            @Param("userId") Long userId);
    @Query(value = """
            SELECT DISTINCT new com.webkit640.ilog_core_backend.api.response.FolderResponse$FolderSummary(
                f.id, f.folderName, f.createdAt, fp.approachedAt, f.folderImage
            )
            FROM Folder f
            JOIN f.folderParticipants fp
            WHERE f.parentFolder.id = :parentFolderId
            AND fp.participant.id = :userId
            ORDER BY f.folderName ASC""")
    List<FolderResponse.FolderSummary> findByParentFolderOrderByNameAsc(
            @Param("parentFolderId") Long parentFolderId,
            @Param("userId") Long userId);

    @Query(value = """
            SELECT DISTINCT new com.webkit640.ilog_core_backend.api.response.FolderResponse$FolderSummary(
                f.id, f.folderName, f.createdAt, fp.approachedAt, f.folderImage
            )
            FROM Folder f
            JOIN f.folderParticipants fp
            WHERE f.parentFolder.id = :parentFolderId
            AND fp.participant.id = :userId
            ORDER BY f.folderName DESC""")
    List<FolderResponse.FolderSummary> findByParentFolderOrderByNameDesc(
            @Param("parentFolderId") Long parentFolderId,
            @Param("userId") Long userId);

    @Query(value = """
            SELECT DISTINCT new com.webkit640.ilog_core_backend.api.response.FolderResponse$FolderSummary(
                f.id, f.folderName, f.createdAt, fp.approachedAt, f.folderImage
            )
            FROM Folder f
            JOIN f.folderParticipants fp
            WHERE f.parentFolder.id = :parentFolderId
            AND fp.participant.id = :userId
            ORDER BY fp.approachedAt Asc""")
    List<FolderResponse.FolderSummary> findByParentFolderOrderByApproachedAtAsc(
            @Param("parentFolderId") Long parentFolderId,
            @Param("userId") Long userId);

    @Query(value = """
            SELECT DISTINCT new com.webkit640.ilog_core_backend.api.response.FolderResponse$FolderSummary(
                f.id, f.folderName, f.createdAt, fp.approachedAt, f.folderImage
            )
            FROM Folder f
            JOIN f.folderParticipants fp
            WHERE f.parentFolder.id = :parentFolderId
            AND fp.participant.id = :userId
            ORDER BY fp.approachedAt DESC""")
    List<FolderResponse.FolderSummary> findByParentFolderOrderByApproachedAtDesc(
            @Param("parentFolderId") Long parentFolderId,
            @Param("userId") Long userId);
    //==============================================================================
    //Lazy 로딩 방지를 위해 participants를 미리 함께 조회
    @Query("""
            SELECT DISTINCT f 
            FROM Folder f 
            LEFT JOIN FETCH f.folderParticipants 
            WHERE f.id = :folderId""")
    Optional<Folder> findByIdWithParticipantsAndChildren(@Param("folderId") Long folderId);

    @Query(value = """
    WITH RECURSIVE folder_cte AS (
        SELECT * FROM folder WHERE id = :rootId
        UNION ALL
        SELECT f.* 
        FROM folder f 
        INNER JOIN folder_cte fc ON f.parent_folder_id = fc.id
    )
    SELECT * FROM folder_cte
    """, nativeQuery = true)
    List<Folder> findAllChildren(@Param("rootId") Long rootId);

    @Query("""
    select f from Folder f
    left join fetch f.owner
    left join fetch f.folderParticipants
    where f.id = :id
    """)
    Optional<Folder> findByIdWithOwnerAndParticipants(@Param("id") Long id);
}
