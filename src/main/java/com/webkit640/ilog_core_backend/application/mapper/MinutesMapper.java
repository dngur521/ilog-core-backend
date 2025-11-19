package com.webkit640.ilog_core_backend.application.mapper;

import com.webkit640.ilog_core_backend.api.response.MinutesResponse;
import com.webkit640.ilog_core_backend.domain.model.Memo;
import com.webkit640.ilog_core_backend.domain.model.MemoHistory;
import com.webkit640.ilog_core_backend.domain.model.Minutes;
import com.webkit640.ilog_core_backend.domain.model.MinutesHistory;
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
                minutes.getId(), minutes.getTitle(), minutes.getContent(),minutes.getCreatedAt(), minutes.getUpdatedAt(), memos);
    }
    private MinutesResponse.Memos toMinutesInMemos(Memo entity){
        String writerEmail = (entity.getMember() != null)
                ? entity.getMember().getEmail()
                : "탈퇴한 사용자";
        String writerName = (entity.getMember() != null)
                ? entity.getMember().getName()
                : "탈퇴한 사용자";

        return new MinutesResponse.Memos(
                entity.getLocalId(),
                writerEmail,
                writerName,
                entity.getMemoType(),
                entity.getContent(),
                entity.getStartIndex(),
                entity.getEndIndex(),
                entity.getPositionContent()
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


    //------------------- history mapper ---------------------------
    public List<MinutesResponse.FindHistory> toFindHistoryList(
            List<MinutesHistory> minutesHistories,
            List<MemoHistory> memoList
    ) {
        return minutesHistories.stream()
                .map(mh ->
                {
                    List<MemoHistory> filteredMemos = memoList.stream()
                            .filter(m ->
                                    m.getMinutesHistory().getMinutes().getId().equals(mh.getMinutes().getId()) &&
                                    m.getMinutesHistory().getHistoryId().equals(mh.getHistoryId()))
                            .toList();
                    return toFindHistory(mh, filteredMemos);
                })
                .toList();

       }

    public MinutesResponse.FindHistory toFindHistory(MinutesHistory minutesHistory, List<MemoHistory> memoList) {
        List<MinutesResponse.Memos> memos = memoList.stream()
                .map(this::toMinutesHistoryInMemos)
                .toList();
        return new MinutesResponse.FindHistory(

                minutesHistory.getId(), minutesHistory.getMinutes().getId(), minutesHistory.getHistoryId(),minutesHistory.getTitle(), minutesHistory.getContent(),minutesHistory.getSummary(),minutesHistory.getCreatedAt(), minutesHistory.getUpdatedAt(), memos);
    }
    private MinutesResponse.Memos toMinutesHistoryInMemos(MemoHistory entity){
        String writerEmail = (entity.getMember() != null)
                ? entity.getMember().getEmail()
                : "탈퇴한 사용자";
        String writerName = (entity.getMember() != null)
                ? entity.getMember().getName()
                : "탈퇴한 사용자";

        return new MinutesResponse.Memos(
                entity.getLocalId(),
                writerEmail,
                writerName,
                entity.getMemoType(),
                entity.getContent(),
                entity.getStartIndex(),
                entity.getEndIndex(),
                entity.getPositionContent()
        );
    }

    public MinutesResponse.Lock toToken(String token) {
        return new MinutesResponse.Lock(token);
    }
}
