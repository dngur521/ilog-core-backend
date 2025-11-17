package com.webkit640.ilog_core_backend.infrastructure.util;

import com.webkit640.ilog_core_backend.api.exception.CustomException;
import com.webkit640.ilog_core_backend.api.request.ParticipantRequest;
import com.webkit640.ilog_core_backend.domain.model.ErrorCode;
import com.webkit640.ilog_core_backend.domain.model.Member;
import com.webkit640.ilog_core_backend.domain.repository.MemberDAO;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class CreateMemberUtils {
    private final MemberDAO memberDAO;
    public Member getCreateMember(ParticipantRequest.Create request){
        Member createMember = null;

        boolean hasId = request.getCreateMemberId() != null;
        boolean hasEmail =request.getCreateMemberEmail() != null;

        if(hasId && hasEmail){
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }
        if(!hasId && !hasEmail){
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }

        if(hasId){
            createMember = memberDAO.findById(request.getCreateMemberId()).orElseThrow(()->new CustomException(ErrorCode.MEMBER_NOT_FOUND));
        }

        if(hasEmail){
            createMember = memberDAO.findByEmail(request.getCreateMemberEmail()).orElseThrow(()->new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        }
        return createMember;
    }
}
