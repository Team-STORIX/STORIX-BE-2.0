package com.storix.storix_api.domains.user.adaptor;

import com.storix.storix_api.domains.user.domain.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

@Getter
@AllArgsConstructor
public class AuthUserDetails implements UserDetails {

    private Long userId;

    private Role role;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_" + role.getStringValue())
        );
    }

    @Override
    public String getPassword() { return null; }

    @Override
    public String getUsername() { return userId.toString(); }

    // AccountState 분기
    @Override
    public boolean isAccountNonExpired() {  return true; }
}
