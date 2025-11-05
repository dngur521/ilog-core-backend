package com.webkit640.ilog_core_backend.infrastructure.security;

import java.util.List;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.webkit640.ilog_core_backend.domain.model.Member;
import com.webkit640.ilog_core_backend.domain.repository.MemberDAO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final MemberDAO memberDAO;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // DB에서 사용자 조회
        Member member = memberDAO.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found:" + username));

        //role에 role이 없다, 나중에 만들면 써야지
        // DB의 역할 사용 (예: USER, ADMIN 등) + ROLE_ prefix 보장
         String roleName = member.getRole() != null ? member.getRole().name() : "USER";
        // List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_" + roleName));
        return new CustomUserDetails(
                member.getId(),
                member.getEmail(),
                member.getPassword(),
                List.of(() -> "ROLE_" + roleName)
        //authorites
        );
    }
}
