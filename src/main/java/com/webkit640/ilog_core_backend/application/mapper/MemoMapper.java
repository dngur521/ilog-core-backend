package com.webkit640.ilog_core_backend.application.mapper;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.webkit640.ilog_core_backend.api.response.MemoResponse;
import com.webkit640.ilog_core_backend.domain.model.Memo;

@Component
public class MemoMapper {

    public MemoResponse.Detail toDetail(List<Memo> memos) {
        List<MemoResponse.Summary> summaries = memos.stream()
                .map(this::toSummary)
                .collect(Collectors.toList());

        return new MemoResponse.Detail(summaries);
    }

    public MemoResponse.Summary toSummary(Memo memo) {
        String writerName = (memo.getMember() != null)
                ? memo.getMember().getName()
                : "탈퇴한 사용자";
        return new MemoResponse.Summary(
                memo.getLocalId(),
                writerName,
                memo.getContent(),
                memo.getMemoType(),
                memo.getCreatedAt(),
                memo.getUpdatedAt(),
                memo.getStartIndex(),
                memo.getEndIndex(),
                memo.getPositionContent()
        );
    }
}
