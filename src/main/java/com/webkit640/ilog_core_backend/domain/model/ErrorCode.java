package com.webkit640.ilog_core_backend.domain.model;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    //인증/인가 관련
    UNAUTHORIZED_MEMBER(HttpStatus.UNAUTHORIZED, "인증되지 않은 사용자입니다."),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "토큰이 만료되었습니다."),
    //회원 관련
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "회원을 찾을 수 없습니다."),
    MEETING_NOT_FOUND(HttpStatus.NOT_FOUND, "화상회의를 찾을 수 없습니다."),
    FOLDER_NOT_FOUND(HttpStatus.NOT_FOUND, "경로를 찾을 수 없습니다."),
    MEMO_NOT_FOUND(HttpStatus.NOT_FOUND, "메모를 찾을 수 없습니다."),
    EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 존재하는 이메일입니다."),
    UNAUTHORIZED_UPDATE(HttpStatus.FORBIDDEN, "본인만 수정할 수 있습니다."),
    UNAUTHORIZED_DELETE(HttpStatus.FORBIDDEN, "본인만 삭제할 수 있습니다."),
    INVALID_PASSWORD(HttpStatus.UNAUTHORIZED, "비밀번호가 일치하지 않습니다."),
    PHONE_NUM_NOT_MATCH(HttpStatus.UNAUTHORIZED, "전화번호가 일치하지 않습니다."),
    PASSWORD_NOT_MATCH(HttpStatus.UNAUTHORIZED, "비밀번호와 비밀번호 확인이 일치하지 않습니다."),
    ADDRESS_NOT_MATCH(HttpStatus.BAD_REQUEST, "회의 주소가 올바르지 않습니다."),
    MINUTES_NOT_FOUND(HttpStatus.NOT_FOUND, "회의록이 없습니다."),
    ALREADY_PARTICIPANT(HttpStatus.CONFLICT, "이미 참여자로 등록된 회원입니다."),
    PARTICIPANT_NOT_FOUND(HttpStatus.CONFLICT, "참여자를 찾을 수 없습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다.");

    private final HttpStatus status;
    private final String message;

}
