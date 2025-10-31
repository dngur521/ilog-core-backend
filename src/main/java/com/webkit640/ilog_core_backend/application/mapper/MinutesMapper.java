package com.webkit640.ilog_core_backend.application.mapper;

import com.webkit640.ilog_core_backend.api.response.MinutesResponse;
import com.webkit640.ilog_core_backend.domain.model.Memo;
import com.webkit640.ilog_core_backend.domain.model.Minutes;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MinutesMapper {
    public MinutesResponse.Create toCreate(Minutes minutes){
        return new MinutesResponse.Create(
            minutes.getId(), minutes.getTitle(), minutes.getContent(), minutes.getSummary()
        );
    }

    public MinutesResponse.FindContent toFindContent(Minutes minutes, List<Memo> memoList) {
        List<MinutesResponse.Memos> memos = memoList.stream()
                .map(this::toMinutesInMemos)
                .toList();
        return new MinutesResponse.FindContent(
                minutes.getId(), minutes.getTitle(), minutes.getContent(),memos);
    }
    private MinutesResponse.Memos toMinutesInMemos(Memo entity){
        return new MinutesResponse.Memos(
                entity.getId(),
                entity.getMember().getEmail(),
                entity.getMember().getName(),
                entity.getMemoType(),
                entity.getContent()
        );
    }

    public MinutesResponse.FindSummary toFindSummary(Minutes minutes, List<Memo> memoList) {
        List<MinutesResponse.Memos> memos = memoList.stream()
                .map(this::toMinutesInMemos)
                .toList();
        return new MinutesResponse.FindSummary(
                minutes.getId(), minutes.getTitle(), minutes.getSummary(), memos);
    }

    public MinutesResponse.Update toUpdate(Minutes minutes) {
        return new MinutesResponse.Update(
                minutes.getId(), minutes.getTitle(), minutes.getContent(), minutes.getSummary());
    }
}
