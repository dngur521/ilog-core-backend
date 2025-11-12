package com.webkit640.ilog_core_backend.application.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.webkit640.ilog_core_backend.api.response.ParticipantResponse;
import com.webkit640.ilog_core_backend.application.mapper.ParticipantMapper;
import com.webkit640.ilog_core_backend.domain.event.FolderDeletedEvent;
import com.webkit640.ilog_core_backend.domain.model.*;
import com.webkit640.ilog_core_backend.domain.repository.*;
import com.webkit640.ilog_core_backend.infrastructure.security.LinkTokenService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.webkit640.ilog_core_backend.api.exception.CustomException;
import com.webkit640.ilog_core_backend.api.request.FolderRequest;
import com.webkit640.ilog_core_backend.api.request.ParticipantRequest;
import com.webkit640.ilog_core_backend.api.response.FolderResponse;
import com.webkit640.ilog_core_backend.application.mapper.FolderMapper;
import com.webkit640.ilog_core_backend.infrastructure.security.CustomUserDetails;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class FolderService {
    private final FileService fileService;
    private final MemberService memberService;
    private final PermissionPropagationService permissionPropagationService;
    private final FolderDAO folderDAO;
    private final MinutesDAO minutesDAO;
    private final MemberDAO memberDAO;
    private final FolderParticipantDAO folderParticipantDAO;
    private final FolderMapper folderMapper;
    private final ParticipantMapper participantMapper;
    private final FolderLogDAO folderLogDAO;
    private final ParticipantLogDAO participantLogDAO;
    private final ApplicationEventPublisher eventPublisher;
    private final LinkTokenService linkTokenService;
    //폴더 생성
    @Transactional
    public Folder createFolder(Long folderId, FolderRequest.Create request, Long ownerId, MultipartFile folderImage) {
        Folder folder = getFolder(folderId);
        Member owner = memberService.getMember(ownerId);

        //-------------------본인 인증-------------------
        identityVerification(folder, ownerId);
        //-------------------폴더 생성-------------------
        Folder newFolder = new Folder();

        newFolder.setParentFolder(folder);
        //-------------------동기화 추가-------------------
        folder.getChildFolders().add(newFolder);
        newFolder.setOwner(owner);
        newFolder.setFolderName(request.getFolderName());
        LocalDateTime createdAt = LocalDateTime.now();
        newFolder.setCreatedAt(createdAt);
        newFolder.setUpdatedAt(null);
        //부모의 참여자 리스트를 참조하기에 참여자 변경 시 부모/자식 폴더가 같이 바뀜
//        List<FolderParticipant> clonedParticipants = cloneFolderParticipants(newFolder, folder, createdAt);
        //------------------------ 폴더 주인만 참가자 -------------------------------
        List<FolderParticipant> clonedParticipants = folderParticipants(newFolder, folder, createdAt);
        newFolder.setFolderParticipants(clonedParticipants);

        //폴더 사진 생성
        if(folderImage != null && !folderImage.isEmpty()) {
            String uploadedUrl = fileService.upload(folderImage);
            newFolder.setFolderImage(uploadedUrl);
        }

        folderDAO.save(newFolder);

        //------------------------ 로그 -------------------------
        folderLogging(owner.getId(),owner.getEmail(),createdAt,newFolder.getId(), ActionType.CREATE,"정상 생성");

        return newFolder;
    }

    //루트 폴더 조회
    public FolderResponse.Find getRootFolderDetail(Long userId, FolderRequest.Order request){
        Member user = memberService.getMember(userId);
        Folder folder = getFolder(user.getRootFolderId());

        //-------------------폴더 조회-------------------
        FolderResponse.Find find = getChild(request.getOrder(),folder, userId, true);
        //참여일시
        approachedTime(folder, user);

        Folder refreshed = folderDAO.findByIdWithParticipantsAndChildren(folder.getId())
                .orElseThrow(()-> new CustomException(ErrorCode.FOLDER_NOT_FOUND));

        return folderMapper.toFind(refreshed, find.getChildFolders(), find.getMinutesList());
    }

    //폴더 조회
    public FolderResponse.Find getFolderDetail(Long folderId, Long userId, FolderRequest.Order request){
        Folder folder = getFolder(folderId);
        Member participant = memberService.getMember(userId);
        //-------------------참여자 인증-------------------
        participantVerification(folder, userId);
        //-------------------폴더 조회-------------------
        FolderResponse.Find find = getChild(request.getOrder(),folder,userId, false);
        //참여일시
        approachedTime(folder, participant);

        Folder refreshed = folderDAO.findByIdWithParticipantsAndChildren(folderId)
                .orElseThrow(()-> new CustomException(ErrorCode.FOLDER_NOT_FOUND));

        return folderMapper.toFind(refreshed, find.getChildFolders(), find.getMinutesList());
    }

    //폴더 수정
    @Transactional
    public Folder updateFolder(Long folderId, FolderRequest.Update request, Long ownerId, MultipartFile folderImage) {
        Folder folder = getFolder(folderId);
        Member owner = memberService.getMember(ownerId);
        //-------------------본인 인증-------------------
        identityVerification(folder, owner.getId());
        //----------------참가자 인증--------------------
        //identityParticipant(folder, ownerId);
        //-------------------폴더 수정-------------------
        if(request.getFolderName() != null && !request.getFolderName().isBlank()){
            folder.setFolderName(request.getFolderName());
        }
        //폴더 사진 수정
        if(folderImage != null && !folderImage.isEmpty()) {
            if(folder.getFolderImage() != null && !folder.getFolderImage().isBlank()){
                fileService.delete(folder.getFolderImage());
            }
            String uploadedUrl = fileService.upload(folderImage);
            folder.setFolderImage(uploadedUrl);
        }
        //변경 사항이 있을 때만
        if(request.getFolderName() != null || request.getImageUrl() != null){
            folder.setUpdatedAt(LocalDateTime.now());

            folderDAO.save(folder);

            //참여일시
            approachedTime(folder, owner);

            //-------------------로그 남기기------------------
            folderLogging(owner.getId(), owner.getEmail(),folder.getUpdatedAt(),folder.getId(),ActionType.UPDATE,"정상 수정");
        }

        return folder;
    }

    //폴더 삭제
    @Transactional
    public void deleteFolder(Long folderId, CustomUserDetails owner) {
        Folder folder = getFolder(folderId);

        //-------------------본인 인증-------------------
        identityVerification(folder, owner.getId());
        //------------ 본인의 루트 폴더인지 확인 -------------
        if(folder.getOwner().getRootFolderId().equals(folder.getId())){
            throw new CustomException(ErrorCode.ROOT_FOLDER_DELETE_DENIED);
        }
        //-------------------재귀적으로 삭제-------------------
        deleteFolderRecursively(folder);
    }

    //폴더 이미지 삭제
    @Transactional
    public void deleteFolderImage(Long folderId, CustomUserDetails owner) {
        Folder folder = getFolder(folderId);
        //-------------------본인 인증-------------------
        identityVerification(folder, owner.getId());

        if(folder.getFolderImage() == null || folder.getFolderImage().isBlank()){
            throw new CustomException(ErrorCode.FILE_IMAGE_NOT_FOUND);
        }
        log.warn(folder.getFolderImage());
        fileService.delete(folder.getFolderImage());
        folder.setFolderImage(null);
    }


    //원하는 파일 조회 <- 회의록 조회지만 폴더단에 넣어놨음, 이유는 폴더에서 작동을 하니까
    public List<FolderResponse.MinutesSummary> getSearchMinutes(FolderRequest.Search request, Long userId) {
        return minutesDAO.findByTitleAndParticipant(request.getMinutesName(),userId);
    }

    //부모의 참여자 리스트를 참조하기에 참여자 변경 시 부모/자식 폴더가 같이 바뀜
    private List<FolderParticipant> cloneFolderParticipants(Folder newFolder, Folder folder, LocalDateTime createdAt){
        return folder.getFolderParticipants().stream()
                .map(fp -> {
                    FolderParticipant copy = new FolderParticipant();
                    copy.setFolder(newFolder);
                    copy.setParticipant(fp.getParticipant());
                    copy.setApproachedAt(createdAt);
                    return copy;
                }).toList();
    }

    //부모 참여자 리스트 참조 X ver
    private List<FolderParticipant> folderParticipants(Folder newFolder, Folder folder, LocalDateTime createdAt){
        List<FolderParticipant> participants = new ArrayList<>(List.of());

        FolderParticipant copy = new FolderParticipant();
        copy.setFolder(newFolder);
        copy.setParticipant(folder.getOwner());
        copy.setApproachedAt(createdAt);

        participants.add(copy);

        return participants;
    }

    //폴더가 있는지 확인
    private Folder getFolder(Long folderId){
        return folderDAO.findByIdWithParticipantsAndChildren(folderId)
                .orElseThrow(() -> new CustomException(ErrorCode.FOLDER_NOT_FOUND));
    }
    //주인인지 확인
    private void identityVerification(Folder folder,Long ownerId){
        if(!folder.getOwner().getId().equals(ownerId)){
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }
    }
    //참여한 사람 인지 확인 => 접근 권한 확인
    private void participantVerification(Folder folder, Long userId){
        boolean hasAccess = folder.getFolderParticipants().stream().anyMatch(
                fp->fp.getParticipant().getId().equals(userId));

        if(!hasAccess){
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }
    }

    private void deleteFolderRecursively(Folder folder){
        if(folder.getFolderImage() != null && !folder.getFolderImage().isBlank()){
            log.warn(folder.getFolderImage());
            fileService.delete(folder.getFolderImage());
        }
        for(Folder child : folder.getChildFolders()){
            deleteFolderRecursively(child);
        }
        //---------------- 미리 연관 폴더 전부 가져오기 -------------------
        Folder fullyLoadedFolder = folderDAO.findByIdWithOwnerAndParticipants(folder.getId())
                        .orElseThrow(()->new CustomException(ErrorCode.FOLDER_NOT_FOUND));

        eventPublisher.publishEvent(new FolderDeletedEvent(this, fullyLoadedFolder));

        //------------------- 로그 -------------------
        folderLogging(folder.getOwner().getId(), folder.getOwner().getEmail(),LocalDateTime.now(),folder.getId(),ActionType.DELETE,"정상 삭제");
        folderDAO.delete(folder);
    }

    //-----------------------------------------권한---------------------------------------
    @Transactional
    public List<FolderParticipant> createParticipant(Long folderId, ParticipantRequest.Create request, Long ownerId) {
        Folder folder = getFolder(folderId);
        Member createMember = memberDAO.findByEmail(request.getCreateMemberEmail()).orElseThrow(()->new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        //---------------본인 인증-----------------------
        identityVerification(folder, ownerId);
        //-------------------request에 맴버가 들어왔는지 확인-------------------
        folderParticipantRequestIsNull(createMember);
        //-------------------이미 참여되어 있는지 검증-------------------
        alreadyParticipant(folder, createMember);
        //-------------------참여자 추가-------------------
        permissionPropagationService.grantToFolders(folderId, createMember.getId());
        //--------------수정된 회원 리스트 리턴-------------
        return folderParticipantDAO.findByFolder(folder);
    }

    //------------------- 링크로 초대받았을때 자동으로 참가자 등록 -------------------------
    @Transactional
    public void joinByInvite(Long folderId, Long userId) {
        Folder folder = folderDAO.findById(folderId)
                .orElseThrow(() -> new CustomException(ErrorCode.FOLDER_NOT_FOUND));
        Member member = memberService.getMember(userId);

        var existing = folderParticipantDAO.findByFolderAndParticipant(folder,member);
        if (existing.isPresent()) return;

        permissionPropagationService.grantToFolders(folderId, userId);
    }

    //참여자 조회
    public ParticipantResponse.DetailLink<ParticipantResponse.FolderParticipant> getParticipant(
            Long folderId, Long participantId
    ){
        Folder folder = getFolder(folderId);
        identityParticipant(folder, participantId);
        var participants = folderParticipantDAO.findByFolder(folder);

        String link = linkTokenService.createLink("FOLDER",folderId);
        return participantMapper.toFolderDetailLink(participants, link);
    }
    //참여자 삭제
    @Transactional
    public List<FolderParticipant> deleteParticipant(Long folderId, ParticipantRequest.Delete request, Long ownerId) {
        Folder folder = getFolder(folderId);
        Member deleteMember = memberService.getMember(request.getDeleteMemberId());

        //---------------본인 인증-----------------------
        identityVerification(folder, ownerId);
        //-------------삭제할 회원 확인-------------------
        folderParticipantRequestIsNull(deleteMember);
        //----------------삭제-------------------------
        permissionPropagationService.removeToFoldersAndMinutes(folderId, request.getDeleteMemberId(),ownerId);
        //--------------수정된 회원 리스트 리턴-----------
        return folderParticipantDAO.findByFolder(folder);
    }

    //회원이 잘 들어왔는지 확인
    private void folderParticipantRequestIsNull(Member member){
        if(member == null){
            throw new CustomException(ErrorCode.MEMBER_NOT_FOUND);
        }
    }
    //이미 등록되었는지 확인
    private void alreadyParticipant(Folder folder, Member user){
        boolean exists = folderParticipantDAO.existsByFolderAndParticipant(folder, user);
        if (exists) {
            throw new CustomException(ErrorCode.ALREADY_PARTICIPANT);
        }
    }
    //참여자에 포함되어있다면 조회가능
    private void identityParticipant(Folder folder, Long userId) {
        boolean isParticipant = folder.getFolderParticipants().stream()
                .anyMatch(fp ->
                        fp.getParticipant().getId().equals(userId));
        if (!isParticipant) {
            throw new CustomException(ErrorCode.VIEW_DENIED);
        }
    }

    //참여 일시 기록
    private void approachedTime(Folder folder, Member owner){
        FolderParticipant folderParticipant = folderParticipantDAO.findByFolderAndParticipant(folder,owner)
                .orElseThrow(()-> new CustomException(ErrorCode.PARTICIPANT_NOT_FOUND));
        folderParticipant.setApproachedAt(LocalDateTime.now());
        folderParticipantDAO.save(folderParticipant);
    }

    //정렬 관리
    private FolderResponse.Find getChild(
            OrderType orderType, Folder folder, Long userId, boolean isRoot){

        //-------------------폴더 조회-------------------
        List<FolderResponse.FolderSummary> childFolders;
        if(isRoot){
            List<FolderResponse.MinutesSummary> minutesList = switch (orderType) {
                //생성일 오래된순
                case OrderType.CREATED_AT_ASC -> {
                    childFolders = folderDAO.findByRootParentFolderOrderByCreatedAtAsc(folder.getId(), userId);
                    yield minutesDAO.findByFolderAndParticipantsOrderByCreatedAtAsc(folder, userId);
                }
                //생성일 최신순
                case OrderType.CREATED_AT_DESC -> {
                    childFolders = folderDAO.findByRootParentFolderOrderByCreatedAtDesc(folder.getId(),userId);
                    yield minutesDAO.findByFolderAndParticipantsOrderByCreatedAtDesc(folder, userId);
                }
                //변경일 오래된순
                case OrderType.UPDATED_AT_ASC -> {
                    childFolders = folderDAO.findByRootParentFolderOrderByUpdatedAtAsc(folder.getId(),userId);
                    yield minutesDAO.findByFolderAndParticipantsOrderByUpdatedAtAsc(folder, userId);
                }
                //변경일 최신순
                case OrderType.UPDATED_AT_DESC -> {
                    childFolders = folderDAO.findByRootParentFolderOrderByUpdatedAtDesc(folder.getId(),userId);
                    yield minutesDAO.findByFolderAndParticipantsOrderByUpdatedAtDesc(folder, userId);
                }
                //이름 오름차순
                case OrderType.TITLE_ASC -> {
                    childFolders = folderDAO.findByRootParentFolderOrderByNameAsc(folder.getId(),userId);
                    yield minutesDAO.findByFolderAndParticipantsOrderByNameAsc(folder, userId);
                }
                //이름 내림차순
                case OrderType.TITLE_DESC -> {
                    childFolders = folderDAO.findByRootParentFolderOrderByNameDesc(folder.getId(),userId);
                    yield minutesDAO.findByFolderAndParticipantsOrderByNameDesc(folder, userId);
                }
                //접근일 오래된순
                case OrderType.APPROACHED_AT_ASC -> {
                    childFolders = folderDAO.findByRootParentFolderOrderByApproachedAtAsc(folder.getId(),userId);
                    yield minutesDAO.findByFolderAndParticipantsOrderByApproachedAtAsc(folder, userId);
                }
                //접근일 최신순
                case OrderType.APPROACHED_AT_DESC -> {
                    childFolders = folderDAO.findByRootParentFolderOrderByApproachedAtDesc(folder.getId(),userId);
                    yield minutesDAO.findByFolderAndParticipantsOrderByApproachedAtDesc(folder, userId);
                }
            };
            return new FolderResponse.Find(
                    null,
                    null,
                    childFolders,
                    minutesList,
                    null
            );
        }else{
            List<FolderResponse.MinutesSummary> minutesList = switch (orderType) {
                //생성일 오래된순
                case OrderType.CREATED_AT_ASC -> {
                    childFolders = folderDAO.findByParentFolderOrderByCreatedAtAsc(folder.getId(), userId);
                    yield minutesDAO.findByFolderAndParticipantsOrderByCreatedAtAsc(folder, userId);
                }
                //생성일 최신순
                case OrderType.CREATED_AT_DESC -> {
                    childFolders = folderDAO.findByParentFolderOrderByCreatedAtDesc(folder.getId(),userId);
                    yield minutesDAO.findByFolderAndParticipantsOrderByCreatedAtDesc(folder, userId);
                }
                //변경일 오래된순
                case OrderType.UPDATED_AT_ASC -> {
                    childFolders = folderDAO.findByParentFolderOrderByUpdatedAtAsc(folder.getId(),userId);
                    yield minutesDAO.findByFolderAndParticipantsOrderByUpdatedAtAsc(folder, userId);
                }
                //변경일 최신순
                case OrderType.UPDATED_AT_DESC -> {
                    childFolders = folderDAO.findByParentFolderOrderByUpdatedAtDesc(folder.getId(),userId);
                    yield minutesDAO.findByFolderAndParticipantsOrderByUpdatedAtDesc(folder, userId);
                }
                //이름 오름차순
                case OrderType.TITLE_ASC -> {
                    childFolders = folderDAO.findByParentFolderOrderByNameAsc(folder.getId(),userId);
                    yield minutesDAO.findByFolderAndParticipantsOrderByNameAsc(folder, userId);
                }
                //이름 내림차순
                case OrderType.TITLE_DESC -> {
                    childFolders = folderDAO.findByParentFolderOrderByNameDesc(folder.getId(),userId);
                    yield minutesDAO.findByFolderAndParticipantsOrderByNameDesc(folder, userId);
                }
                //접근일 오래된순
                case OrderType.APPROACHED_AT_ASC -> {
                    childFolders = folderDAO.findByParentFolderOrderByApproachedAtAsc(folder.getId(),userId);
                    yield minutesDAO.findByFolderAndParticipantsOrderByApproachedAtAsc(folder, userId);
                }
                //접근일 최신순
                case OrderType.APPROACHED_AT_DESC -> {
                    childFolders = folderDAO.findByParentFolderOrderByApproachedAtDesc(folder.getId(),userId);
                    yield minutesDAO.findByFolderAndParticipantsOrderByApproachedAtDesc(folder, userId);
                }
            };
            return new FolderResponse.Find(
                    null,
                    null,
                    childFolders,
                    minutesList,
                    null
            );
        }
    }

    private void folderLogging(Long userId, String email, LocalDateTime createdAt, Long folderId, ActionType status, String description){
        FolderLog folderLog = new FolderLog();
        folderLog.setUserId(userId);
        folderLog.setEmail(email);
        folderLog.setCreatedAt(createdAt);
        folderLog.setFolderId(folderId);
        folderLog.setStatus(status);
        folderLog.setDescription(description);

        folderLogDAO.save(folderLog);
    }

    //참여자 로그 남기기
    public void participantLogging(Long userId, String email, LocalDateTime createdAt, String folderParticipantEmail, ParticipantType participantType, ActionType status, String description){
        ParticipantLog minutesParticipantLog = new ParticipantLog();
        minutesParticipantLog.setUserId(userId);
        minutesParticipantLog.setEmail(email);
        minutesParticipantLog.setCreatedAt(createdAt);
        minutesParticipantLog.setParticipantEmail(folderParticipantEmail);
        minutesParticipantLog.setParticipantType(participantType);
        minutesParticipantLog.setStatus(status);
        minutesParticipantLog.setDescription(description);

        participantLogDAO.save(minutesParticipantLog);
    }
}
