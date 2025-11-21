package com.webkit640.ilog_core_backend.application.mapper;

import com.webkit640.ilog_core_backend.api.response.MemberResponse;
import com.webkit640.ilog_core_backend.domain.model.Member;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MemberMapper {
    public MemberResponse.Create toCreate(Member member){
        return new MemberResponse.Create(member.getId(),member.getEmail(),member.getName(), member.getPhoneNum(), member.getJoinedAt(), member.getRootFolderId(), member.getProfileImage());

    }
    public MemberResponse.Detail toFind(Member member){
        return new MemberResponse.Detail(member.getId(),member.getEmail(),member.getName(), member.getPhoneNum(), member.getJoinedAt(), member.getProfileImage());
    }

    public List<MemberResponse.Email> toEmail(List<String> email) {
        return email.stream().map(MemberResponse.Email::new).toList();
    }
}
