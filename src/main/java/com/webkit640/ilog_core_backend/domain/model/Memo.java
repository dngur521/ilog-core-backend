package com.webkit640.ilog_core_backend.domain.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Setter
@Getter
public class Memo {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    private Minutes minutes;
    @ManyToOne
    private Member member;
    private MemoType memoType;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
