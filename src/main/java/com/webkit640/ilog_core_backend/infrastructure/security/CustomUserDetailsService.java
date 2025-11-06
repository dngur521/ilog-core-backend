package com.webkit640.ilog_core_backend.infrastructure.security;

import java.util.List;

import com.webkit640.ilog_core_backend.api.exception.CustomException;
import com.webkit640.ilog_core_backend.domain.model.ErrorCode;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.webkit640.ilog_core_backend.domain.model.Member;
import com.webkit640.ilog_core_backend.domain.repository.MemberDAO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService{

    private final MemberDAO memberDAO;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        throw new UsernameNotFoundException("Username login is not supported. Use loadUserById().");
    }

    public UserDetails loadUserById(Long userId){
        Member member = memberDAO.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
        return createUserDetails(member);
    }
    private UserDetails createUserDetails(Member member){
        //role에 role이 없다, 나중에 만들면 써야지
        // DB의 역할 사용 (예: USER, ADMIN 등) + ROLE_ prefix 보장
        // List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_" + roleName));
        String roleName = member.getRole() != null ? member.getRole().name() : "USER";

        return new CustomUserDetails(
                    member.getId(),
                    member.getId().toString(),
                    member.getPassword(),
                    List.of(() -> "ROLE_" + roleName)
            //authorites
            );
    }
}
