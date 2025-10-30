package com.webkit640.ilog_core_backend.application.mapper;

import com.webkit640.ilog_core_backend.api.response.MemoResponse;
import com.webkit640.ilog_core_backend.domain.model.Memo;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class MemoMapper {
    public MemoResponse.Detail toDetail(List<Memo> memos) {
        List<MemoResponse.Summary> summaries = memos.stream()
                .map(this::toSummary)
                .collect(Collectors.toList());

        return new MemoResponse.Detail(summaries);
    }

    public MemoResponse.Summary toSummary(Memo memo) {
        return new MemoResponse.Summary(
                memo.getId(),
                memo.getMember().getName(),
                memo.getContent(),
                memo.getMemoType(),
                memo.getCreatedAt(),
                memo.getUpdatedAt()
        );
    }
}
