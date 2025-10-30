package com.webkit640.ilog_core_backend.infrastructure.security;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

@Getter
@AllArgsConstructor
public class CustomUserDetails implements UserDetails {
    private final Long id;
    private final String username;

    @JsonIgnore //직렬화 시 비밀번호 노출 방지
    private final String password;

    private final Collection<? extends GrantedAuthority> authorities;

    @Override public Collection<? extends GrantedAuthority> getAuthorities() {return authorities;}

    @Override public String getPassword() { return password; }

    @Override public String getUsername() { return username; }

    // ✅ 계정 만료 여부
    @Override public boolean isAccountNonExpired() { return true; }

    // ✅ 계정 잠금 여부
    @Override public boolean isAccountNonLocked() { return true; }

    // ✅ 비밀번호 만료 여부
    @Override public boolean isCredentialsNonExpired() { return true; }

    // ✅ 계정 활성화 여부
    @Override public boolean isEnabled() { return true; }
}