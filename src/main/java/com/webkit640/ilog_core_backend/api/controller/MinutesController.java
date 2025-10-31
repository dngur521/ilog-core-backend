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

import com.webkit640.ilog_core_backend.api.request.MinutesRequest;
import com.webkit640.ilog_core_backend.api.request.ParticipantRequest;
import com.webkit640.ilog_core_backend.api.response.MinutesResponse;
import com.webkit640.ilog_core_backend.api.response.ParticipantResponse;
import com.webkit640.ilog_core_backend.application.mapper.FolderParticipantMapper;
import com.webkit640.ilog_core_backend.application.mapper.MinutesMapper;
import com.webkit640.ilog_core_backend.application.service.MinutesService;
import com.webkit640.ilog_core_backend.domain.model.Minutes;
import com.webkit640.ilog_core_backend.domain.model.MinutesParticipant;
import com.webkit640.ilog_core_backend.infrastructure.security.CustomUserDetails;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/minutes")
public class MinutesController {

    //--------------회의록------------------
    private final MinutesService minutesService;
    private final MinutesMapper minutesMapper;

    //회의록 생성
    @PostMapping("/{folderId}")
    public ResponseEntity<MinutesResponse.Create> createMinutes(
            @PathVariable("folderId") Long folderId,
            @RequestBody MinutesRequest.Create request,
            @AuthenticationPrincipal CustomUserDetails owner
    ) {
        Long ownerId = owner.getId();
        Minutes minutes = minutesService.createMinutes(folderId, request, ownerId);
        return ResponseEntity.ok(minutesMapper.toCreate(minutes));
    }

    //회의록 본문 조회
    @GetMapping("/{minutesId}")
    public ResponseEntity<MinutesResponse.FindContent> findContentMinutes(
            @PathVariable("minutesId") Long minutesId,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        Long userId = user.getId();
        Minutes minutes = minutesService.getMinutes(minutesId, userId);
        return ResponseEntity.ok(minutesMapper.toFindContent(minutes));
    }

    //회의록 요약 조회
    @GetMapping("/{minutesId}/summary")
    public ResponseEntity<MinutesResponse.FindSummary> findSummaryMinutes(
            @PathVariable("minutesId") Long minutesId,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        Long userId = user.getId();
        Minutes minutes = minutesService.getMinutes(minutesId, userId);
        return ResponseEntity.ok(minutesMapper.toFindSummary(minutes));
    }

    //회의록 수정
    @PatchMapping("/{minutesId}")
    public ResponseEntity<MinutesResponse.Update> updateMinutes(
            @PathVariable("minutesId") Long minutesId,
            @RequestBody MinutesRequest.Update request,
            @AuthenticationPrincipal CustomUserDetails owner
    ) {
        Long ownerId = owner.getId();
        Minutes minutes = minutesService.updateMinutes(minutesId, request, ownerId);
        return ResponseEntity.ok(minutesMapper.toUpdate(minutes));
    }

    //회의록 삭제
    @DeleteMapping("/{minutesId}")
    public ResponseEntity<Void> deleteMinutes(
            @PathVariable("minutesId") Long minutesId,
            @AuthenticationPrincipal CustomUserDetails owner
    ) {
        minutesService.deleteMinutes(minutesId, owner);
        return ResponseEntity.noContent().build();
    }

    //---------------조원 권한 관리-----------------------------
    private final FolderParticipantMapper participantMapper;

    //조원 관리 추가
    @PostMapping("/{minutesId}/party")
    public ResponseEntity<ParticipantResponse.Detail> createParticipant(
            @PathVariable("minutesId") Long minutesId,
            @RequestBody ParticipantRequest.Create request,
            @AuthenticationPrincipal CustomUserDetails owner
    ) {
        List<MinutesParticipant> participants = minutesService.createParticipant(minutesId, request, owner);
        return ResponseEntity.ok(participantMapper.toMinutesDetail(participants));
    }

    //조원 관리 조회
    @GetMapping("{minutesId}/party")
    public ResponseEntity<ParticipantResponse.Detail> findParticipant(
            @PathVariable("minutesId") Long minutesId,
            @AuthenticationPrincipal CustomUserDetails owner
    ) {
        List<MinutesParticipant> participants = minutesService.getParticipant(minutesId, owner);
        return ResponseEntity.ok(participantMapper.toMinutesDetail(participants));
    }

    //조원 관리 삭제
    @DeleteMapping("/{minutesId}/party")
    public ResponseEntity<ParticipantResponse.Detail> deleteParticipant(
            @PathVariable("minutesId") Long minutesId,
            @ModelAttribute ParticipantRequest.Delete request,
            @AuthenticationPrincipal CustomUserDetails owner
    ) {
        List<MinutesParticipant> participants = minutesService.deleteParticipant(minutesId, request, owner);
        return ResponseEntity.ok(participantMapper.toMinutesDetail(participants));
    }
}
