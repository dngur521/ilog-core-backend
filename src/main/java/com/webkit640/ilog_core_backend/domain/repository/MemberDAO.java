package com.webkit640.ilog_core_backend.domain.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.webkit640.ilog_core_backend.domain.model.Member;

public interface MemberDAO extends JpaRepository<Member, Long> {

    Optional<Member> findByEmail(String email);

    Optional<Member> findByPhoneNum(String phoneNum);
}
