package com.webkit640.ilog_core_backend.domain.model;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

@Entity
@Setter
@Getter
public class Memo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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
