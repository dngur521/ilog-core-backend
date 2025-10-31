package com.webkit640.ilog_core_backend.domain.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.webkit640.ilog_core_backend.domain.model.Folder;

public interface FolderDAO extends JpaRepository<Folder, Long> {

    List<Folder> findByParentFolder(Folder folder);

    //Lazy 로딩 방지를 위해 participants를 미리 함께 조회
    @Query("SELECT f FROM Folder f "
            + "LEFT JOIN FETCH f.folderParticipants "
            + "WHERE f.id = :folderId")
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
