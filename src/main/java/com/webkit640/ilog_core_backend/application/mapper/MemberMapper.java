package com.webkit640.ilog_core_backend.application.mapper;

import com.webkit640.ilog_core_backend.api.response.MemberResponse;
import com.webkit640.ilog_core_backend.domain.model.Member;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MemberMapper {
    public MemberResponse.Detail toDetail(Member member){

        List<MemberResponse.FolderSummary> folders =
                member.getFolders() == null ? List.of()
                        : member.getFolders().stream()
                        .map(f -> new MemberResponse.FolderSummary(
                                f.getId(),
                                f.getFolderName(),
                                f.getCreatedAt()
                        ))
                        .toList();

        return new MemberResponse.Detail(member.getId(),member.getEmail(),member.getName(), member.getPhoneNum(), member.getJoinedAt(), folders);
    }

    public MemberResponse.Email toEmail(String email) {
        return new MemberResponse.Email(email);

    }
}
