package com.webkit640.ilog_core_backend.application.service;

import com.webkit640.ilog_core_backend.api.exception.CustomException;
import com.webkit640.ilog_core_backend.api.request.MemberRequest;
import com.webkit640.ilog_core_backend.domain.model.Folder;
import com.webkit640.ilog_core_backend.domain.model.FolderParticipant;
import com.webkit640.ilog_core_backend.domain.repository.FolderDAO;
import com.webkit640.ilog_core_backend.domain.repository.FolderParticipantDAO;
import com.webkit640.ilog_core_backend.infrastructure.security.JwtTokenProvider;
import com.webkit640.ilog_core_backend.domain.model.ErrorCode;
import com.webkit640.ilog_core_backend.domain.repository.MemberDAO;
import com.webkit640.ilog_core_backend.domain.model.Member;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberService {
    private final FileService fileService;
    private final MemberDAO memberDAO;
    private final FolderDAO folderDAO;
    private final FolderParticipantDAO folderParticipantDAO;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    //회원 가입
    public Member registerMember(MemberRequest.Create request, MultipartFile profileImage){
        //-------------------이메일 중복 체크-------------------
        checkDuplicateEmail(request.getEmail());

        //-------------------회원 객체 생성 및 저장-------------------
        Member member = new Member();
        member.setEmail(request.getEmail());

        //-------------패스워드, 패스워드 확인 서로 같은지 확인------------
        if(!request.getPassword().equals(request.getCheckPassword())){
            throw new CustomException(ErrorCode.PASSWORD_NOT_MATCH);
        }else{
            member.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        member.setName(request.getName());
        member.setPhoneNum(request.getPhoneNum());
        member.setJoinedAt(LocalDateTime.now());
        member.setRootFolderId(null);

        //프로필 사진 수정
        if(profileImage != null && !profileImage.isEmpty()) {
            String uploadedUrl = fileService.upload(profileImage);
            member.setProfileImage(uploadedUrl);
        }
        memberDAO.save(member);

        //root폴더 생성
        Folder root = new Folder();

        root.setParentFolder(null);
        root.setOwner(member);
        root.setFolderName("Root");
        root.setCreatedAt(LocalDateTime.now());
        root.setUpdatedAt(null);

        folderDAO.save(root);

        //자신의 root 폴더 접근 권한 생성
        FolderParticipant participant =new FolderParticipant();
        participant.setParticipant(member);
        participant.setFolder(root);

        folderParticipantDAO.save(participant);

        member.setRootFolderId(root.getId());

        return member;
    }
    
    //이메일 중복 체크
    private void checkDuplicateEmail(String email){
        memberDAO.findByEmail(email).ifPresent(m->{
            throw new CustomException(ErrorCode.EMAIL_ALREADY_EXISTS);
        });
    }

    //회원 조회
    public Member getMember(Long id) {
        return memberDAO.findById(id).orElseThrow(()-> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
    }

    //회원 정보 수정
    @Transactional
    public Member updateMember(MemberRequest.Update request, Long currentMemberId, MultipartFile profileImage) {
        //-------------------회원 조회-------------------
        Member member = getMember(currentMemberId);

        //-------------------본인 검증-------------------
        if(!member.getId().equals(currentMemberId)){
            throw new CustomException(ErrorCode.UNAUTHORIZED_UPDATE);
        }

        //-------------------입력 값이 있을때만 수정-------------------
        if(request.getName() != null){
            member.setName(request.getName());
        }

        //-----------------비밀번호가 존재하고 입력한 비밀번호들이 같아야 변경 ------------------
        if(request.getNewPassword() != null && request.getCheckPassword() != null){
            if(!request.getNewPassword().equals(request.getCheckPassword())){
                throw new CustomException(ErrorCode.PASSWORD_NOT_MATCH);
            }
            member.setPassword(passwordEncoder.encode(request.getNewPassword()));
        }

        //프로필 사진 수정
        if(profileImage != null && !profileImage.isEmpty()) {
            if(member.getProfileImage() != null && !member.getProfileImage().isBlank()){
                fileService.delete(member.getProfileImage());
            }
            String uploadedUrl = fileService.upload(profileImage);
            member.setProfileImage(uploadedUrl);
        }
        //-------------------수정된 내용 DB에 반영-------------------
        memberDAO.save(member);
        return member;
    }

    //회원 삭제
    @Transactional
    public void deleteMember(Long memberId, Long currentMemberId) {
        //-------------------회원 조회-------------------
        Member member = getMember(memberId);

        //-------------------본인 검증-------------------
        if(!member.getId().equals(currentMemberId)){
            throw new CustomException(ErrorCode.UNAUTHORIZED_DELETE);
        }
        //---------------------이미지 삭제--------------------
        fileService.delete(member.getProfileImage());
        //-------------------회원 삭제 DB 반영-------------------
        memberDAO.delete(member);
    }

    //회원 이메일 조회 (전화 번호 -> 이메일)
    public String getEmail(String phoneNum) {
        Member member = memberDAO.findByPhoneNum(phoneNum).orElseThrow(()->new CustomException(ErrorCode.MEMBER_NOT_FOUND));
        return member.getEmail();
    }

    //이메일과 전화번호로 회원이 맞는지 검증
    public String verifyAccount(MemberRequest.Verify request) {
        //-------------------email로 회원 찾기-------------------
        Member member = memberDAO.findByEmail(request.getEmail())
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        //-------------------전화번호 다르면 에러처리-------------------
        if(!member.getPhoneNum().equals(request.getPhoneNum())){
            throw new CustomException(ErrorCode.PHONE_NUM_NOT_MATCH);
        }
        long resetTtlMs = 15 * 60 * 1000L;
        return jwtTokenProvider.buildToken(member.getId(),member.getEmail(), List.of(), "RESET", resetTtlMs);
    }

    //입력받은 비밀번호를 새 비밀번호로 입력
    public Member resetPassword(MemberRequest.Reset request) {
        String token = request.getResetToken();

        //-------------------토큰이 들어있는지 확인-------------------
        if(!jwtTokenProvider.isTokenValid(token)){
            throw new CustomException(ErrorCode.TOKEN_EXPIRED);
        }
        //-------------------토큰 값이 Reset이 맞는지 확인-------------------
        if(!"RESET".equals(jwtTokenProvider.getType(token))){
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }

        String email = jwtTokenProvider.getUsername(token);
        Member member = memberDAO.findByEmail(email)
                .orElseThrow(()-> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        //------------------- 비밀번호 중복 체크 -------------------
        if(!request.getNewPassword().equals(request.getCheckPassword())){
            throw new CustomException(ErrorCode.PASSWORD_NOT_MATCH);
        }
        //------------------- 비밀번호 암호화 및 저장 -------------------
        member.setPassword(passwordEncoder.encode(request.getNewPassword()));
        //------------------- DB에 반영 -------------------
        memberDAO.save(member);

        return member;
    }
}
