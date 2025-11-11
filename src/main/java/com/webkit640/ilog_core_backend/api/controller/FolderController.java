package com.webkit640.ilog_core_backend.api.controller;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.webkit640.ilog_core_backend.api.request.FolderRequest;
import com.webkit640.ilog_core_backend.api.request.ParticipantRequest;
import com.webkit640.ilog_core_backend.api.response.FolderResponse;
import com.webkit640.ilog_core_backend.api.response.ParticipantResponse;
import com.webkit640.ilog_core_backend.application.mapper.FolderMapper;
import com.webkit640.ilog_core_backend.application.mapper.ParticipantMapper;
import com.webkit640.ilog_core_backend.application.service.FolderService;
import com.webkit640.ilog_core_backend.domain.model.Folder;
import com.webkit640.ilog_core_backend.domain.model.FolderParticipant;
import com.webkit640.ilog_core_backend.infrastructure.security.CustomUserDetails;

import lombok.RequiredArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/folders")
public class FolderController {
    private final FolderMapper folderMapper;
    private final FolderService folderService;
    //----------------폴더-------------------
    //폴더 생성
    @PostMapping(value = "/{folderId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FolderResponse.Create> createFolder(
            @PathVariable("folderId") Long folderId,
            @ModelAttribute FolderRequest.Create request,
            @RequestPart(value = "folderImage",required=false) MultipartFile folderImage,
            @AuthenticationPrincipal CustomUserDetails owner
    ){
        Long ownerId = owner.getId();
        Folder folder = folderService.createFolder(folderId, request,ownerId, folderImage);
        return ResponseEntity.ok(folderMapper.toCreate(folder));
    }
    //루트 프로젝트 조회
    @GetMapping
    public ResponseEntity<FolderResponse.Find> findRootFolder(
            @ModelAttribute FolderRequest.Order order,
            @AuthenticationPrincipal CustomUserDetails user
    ){
        Long userId = user.getId();
        FolderResponse.Find response = folderService.getRootFolderDetail(userId, order);
        return ResponseEntity.ok(response);
    }

    //일반 폴더 조회
    @GetMapping("/{folderId}")
    public ResponseEntity<FolderResponse.Find> findFolder(
            @PathVariable("folderId") Long folderId,
            @ModelAttribute FolderRequest.Order order,
            @AuthenticationPrincipal CustomUserDetails user
    ){
        Long userId = user.getId();
        FolderResponse.Find response = folderService.getFolderDetail(folderId,userId, order);
        return ResponseEntity.ok(response);
    }
    
    //폴더 수정
    @PatchMapping(value = "/{folderId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FolderResponse.Update> updateFolder(
            @PathVariable("folderId") Long folderId,
            @ModelAttribute FolderRequest.Update request,
            @RequestPart(value = "folderImage",required=false) MultipartFile folderImage,
            @AuthenticationPrincipal CustomUserDetails owner
    ){
        Long ownerId = owner.getId();
        Folder folder = folderService.updateFolder(folderId, request,ownerId, folderImage);
        return ResponseEntity.ok(folderMapper.toUpdate(folder));
    }
    //폴더 삭제 <- 하위 폴더 싹다 삭제 예정
    @DeleteMapping("/{folderId}")
    public ResponseEntity<Void> deleteFolder(
            @PathVariable("folderId") Long folderId,
            @AuthenticationPrincipal CustomUserDetails owner
    ){
        folderService.deleteFolder(folderId, owner);
        return ResponseEntity.noContent().build();
    }

    //폴더 이미지 삭제
    @DeleteMapping("/{folderId}/image")
    public ResponseEntity<Void> deleteFolderImage(
            @PathVariable("folderId") Long folderId,
            @AuthenticationPrincipal CustomUserDetails owner
    ){
        folderService.deleteFolderImage(folderId, owner);
        return ResponseEntity.noContent().build();
    }


    //검색어로 회의록 조회
    @GetMapping("/search")
    public ResponseEntity<List<FolderResponse.MinutesSummary>> searchFolder(
            @ModelAttribute FolderRequest.Search request,
            @AuthenticationPrincipal CustomUserDetails user
    ){
        Long userId = user.getId();
        List<FolderResponse.MinutesSummary> response = folderService.getSearchMinutes(request, userId);
        return ResponseEntity.ok(folderMapper.toSearch(response));
    }
    //---------------조원 권한 관리-----------------------------
    private final ParticipantMapper participantMapper;
    //조원 관리 추가
    @PostMapping("/{folderId}/party")
    public ResponseEntity<ParticipantResponse.Detail<ParticipantResponse.FolderParticipant>> createParticipant(
            @PathVariable("folderId") Long folderId,
            @RequestBody ParticipantRequest.Create createMemberEmail,
            @AuthenticationPrincipal CustomUserDetails owner
    ){
        Long ownerId = owner.getId();
        List<FolderParticipant> folderParticipantList = folderService.createParticipant(folderId, createMemberEmail, ownerId);
        return ResponseEntity.ok(participantMapper.toFolderDetail(folderParticipantList));
    }

    //조원 관리 조회
    @GetMapping("/{folderId}/party")
    public ResponseEntity<ParticipantResponse.DetailLink<ParticipantResponse.FolderParticipant>> findParticipant(
            @PathVariable("folderId") Long folderId,
            @AuthenticationPrincipal CustomUserDetails owner
    ){
        Long ownerId = owner.getId();
        ParticipantResponse.DetailLink<ParticipantResponse.FolderParticipant> folderParticipantList = folderService.getParticipant(folderId, ownerId);
        return ResponseEntity.ok(folderParticipantList);
    }

    //조원 관리 삭제
    @DeleteMapping("/{folderId}/party")
    public ResponseEntity<ParticipantResponse.Detail<ParticipantResponse.FolderParticipant>> deleteParticipant(
            @PathVariable("folderId") Long folderId,
            @ModelAttribute ParticipantRequest.Delete deleteMemberId,
            @AuthenticationPrincipal CustomUserDetails owner
    ){
        Long ownerId = owner.getId();
        List<FolderParticipant> folderParticipantList = folderService.deleteParticipant(folderId, deleteMemberId, ownerId);
        return ResponseEntity.ok(participantMapper.toFolderDetail(folderParticipantList));
    }
}
