package com.ima.security;



import com.ima.entity.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;
import java.util.stream.Collectors;

@Getter
public class UserPrincipal implements UserDetails {

    private final Long id;
    private final String username;
    private final String password;
    private final String email;
    private final boolean enabled;
    private final boolean accountNonLocked;
    private final Set<GrantedAuthority> authorities;

    public UserPrincipal(Long id,
                         String username,
                         String password,
                         String email,
                         boolean enabled,
                         boolean accountNonLocked,
                         Set<GrantedAuthority> authorities) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.email = email;
        this.enabled = enabled;
        this.accountNonLocked = accountNonLocked;
        this.authorities = authorities;
    }

    public static UserPrincipal fromUser(User user) {
        Set<GrantedAuthority> auths = Optional.ofNullable(user.getRoles())
                .orElse(Collections.emptySet())
                .stream()
                .map(r -> new SimpleGrantedAuthority("ROLE_" + r.getName()))
                .collect(Collectors.toSet());

        return new UserPrincipal(
                user.getId(),
                user.getUsername(),
                user.getPassword() == null ? "" : user.getPassword(),
                user.getEmail(),
                user.isAccountEnabled(),
                !user.isAccountLocked(),
                auths
        );
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    // username is subject in JWT, so keep getUsername() as user name
    @Override
    public String getPassword() { return password; }

    @Override
    public String getUsername() { return username; }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return accountNonLocked; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return enabled; }
}
