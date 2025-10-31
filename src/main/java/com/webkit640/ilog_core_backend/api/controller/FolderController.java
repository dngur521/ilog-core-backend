package com.webkit640.ilog_core_backend.api.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.webkit640.ilog_core_backend.api.request.FolderRequest;
import com.webkit640.ilog_core_backend.api.request.ParticipantRequest;
import com.webkit640.ilog_core_backend.api.response.FolderResponse;
import com.webkit640.ilog_core_backend.api.response.ParticipantResponse;
import com.webkit640.ilog_core_backend.application.mapper.FolderMapper;
import com.webkit640.ilog_core_backend.application.mapper.FolderParticipantMapper;
import com.webkit640.ilog_core_backend.application.service.FolderService;
import com.webkit640.ilog_core_backend.domain.model.Folder;
import com.webkit640.ilog_core_backend.domain.model.FolderParticipant;
import com.webkit640.ilog_core_backend.infrastructure.security.CustomUserDetails;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/folders")
public class FolderController {

    private final FolderMapper folderMapper;
    private final FolderService folderService;

    //----------------폴더-------------------
    //폴더 생성
    @PostMapping("/{folderId}")
    public ResponseEntity<FolderResponse.Create> createFolder(
            @PathVariable("folderId") Long folderId,
            @RequestBody FolderRequest.Create request,
            @AuthenticationPrincipal CustomUserDetails owner
    ) {
        Long ownerId = owner.getId();
        Folder folder = folderService.createFolder(folderId, request, ownerId);
        return ResponseEntity.ok(folderMapper.toCreate(folder));
    }

    //폴더 이동 디렉토리 방식
    @GetMapping("/{folderId}")
    public ResponseEntity<FolderResponse.Find> findFolder(
            @PathVariable("folderId") Long folderId,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        Long userId = user.getId();
        //반환값이 복합적이라 mapper를 service계층 내부에서 처리
        FolderResponse.Find response = folderService.getFolderDetail(folderId, userId);
        return ResponseEntity.ok(response);
    }

    //폴더 수정
    @PatchMapping("/{folderId}")
    public ResponseEntity<FolderResponse.Update> updateFolder(
            @PathVariable("folderId") Long folderId,
            @RequestBody FolderRequest.Update request,
            @AuthenticationPrincipal CustomUserDetails owner
    ) {
        Long ownerId = owner.getId();
        Folder folder = folderService.updateFolder(folderId, request, ownerId);
        return ResponseEntity.ok(folderMapper.toUpdate(folder));
    }

    //폴더 삭제 <- 하위 폴더 싹다 삭제 예정
    @DeleteMapping("/{folderId}")
    public ResponseEntity<Void> deleteFolder(
            @PathVariable("folderId") Long folderId,
            @AuthenticationPrincipal CustomUserDetails owner
    ) {
        folderService.deleteFolder(folderId, owner);
        return ResponseEntity.noContent().build();
    }
    //---------------조원 권한 관리-----------------------------
    private final FolderParticipantMapper participantMapper;

    //조원 관리 추가
    @PostMapping("/{folderId}/party")
    public ResponseEntity<ParticipantResponse.Detail> createParticipant(
            @PathVariable("folderId") Long folderId,
            @RequestBody ParticipantRequest.Create request,
            @AuthenticationPrincipal CustomUserDetails owner
    ) {
        List<FolderParticipant> folderParticipantList = folderService.createParticipant(folderId, request, owner);
        return ResponseEntity.ok(participantMapper.toFolderDetail(folderParticipantList));
    }

    //조원 관리 조회
    @GetMapping("{folderId}/party")
    public ResponseEntity<ParticipantResponse.Detail> findParticipant(
            @PathVariable("folderId") Long folderId,
            @AuthenticationPrincipal CustomUserDetails owner
    ) {
        List<FolderParticipant> folderParticipantList = folderService.getParticipant(folderId, owner);
        return ResponseEntity.ok(participantMapper.toFolderDetail(folderParticipantList));
    }

    //조원 관리 삭제
    @DeleteMapping("/{folderId}/party")
    public ResponseEntity<ParticipantResponse.Detail> deleteParticipant(
            @PathVariable("folderId") Long folderId,
            @ModelAttribute ParticipantRequest.Delete request,
            @AuthenticationPrincipal CustomUserDetails owner
    ) {
        List<FolderParticipant> folderParticipantList = folderService.deleteParticipant(folderId, request, owner);
        return ResponseEntity.ok(participantMapper.toFolderDetail(folderParticipantList));
    }
}
