package com.webkit640.ilog_core_backend.domain.repository;

import com.webkit640.ilog_core_backend.api.response.FolderResponse;
import com.webkit640.ilog_core_backend.domain.model.Folder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FolderDAO extends JpaRepository<Folder, Long> {
    @Query(value = """
            SELECT DISTINCT new com.webkit640.ilog_core_backend.api.response.FolderResponse$FolderSummary(
                f.id, f.folderName, fp.approachedAt
            )
            FROM Folder f 
            JOIN f.folderParticipants fp 
            WHERE f.parentFolder = :folder 
            ORDER BY f.id DESC""")
    List<FolderResponse.FolderSummary> findByParentFolder(@Param("folder")Folder folder);

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
