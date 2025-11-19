package com.webkit640.ilog_core_backend.domain.model;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Setter
@Getter
public class Memo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long localId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "minutes_id", nullable = false)
    private Minutes minutes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    private MemoType memoType;
    @Column(columnDefinition = "TEXT")
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Integer startIndex;
    private Integer endIndex;
    private String positionContent;
}
