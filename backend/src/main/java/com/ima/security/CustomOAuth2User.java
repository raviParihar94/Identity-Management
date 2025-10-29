package com.ima.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public class CustomOAuth2User implements OAuth2User {

    private final Map<String, Object> attributes;
    private final Collection<? extends GrantedAuthority> authorities;

    public CustomOAuth2User(Map<String, Object> attributes) {
        this(attributes, Collections.emptyList());
    }

    public CustomOAuth2User(Map<String, Object> attributes, Collection<? extends GrantedAuthority> authorities) {
        this.attributes = attributes;
        this.authorities = authorities;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getName() {
        // Prefer ID if available; fall back to name
        return attributes.getOrDefault("id", attributes.get("name")).toString();
    }

    public String getEmail() {
        // Some providers (like GitHub) return "login" instead of "email"
        Object email = attributes.get("email");
        if (email == null && attributes.containsKey("login")) {
            email = attributes.get("login");
        }
        return email != null ? email.toString() : "unknown";
    }

    public String getPicture() {
        Object picture = attributes.get("picture");
        if (picture == null && attributes.containsKey("avatar_url")) {
            picture = attributes.get("avatar_url"); // GitHub avatar
        }
        return picture != null ? picture.toString() : null;
    }
}
