package com.ima.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Getter
@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Setter
@EntityListeners(AuditingEntityListener.class)
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(name = "mfa_enabled")
    private boolean mfaEnabled = false;

    @Column(name = "mfa_secret")
    private String mfaSecret;

    @Column(name = "account_locked")
    private boolean accountLocked = false;

    @Column(name = "account_enabled")
    private boolean accountEnabled = true;

    @Column(name = "failed_login_attempts")
    private int failedLoginAttempts = 0;

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;


    public void setId(Long id) {
        this.id = id;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setMfaEnabled(boolean mfaEnabled) {
        this.mfaEnabled = mfaEnabled;
    }

    public void setMfaSecret(String mfaSecret) {
        this.mfaSecret = mfaSecret;
    }

    public void setAccountLocked(boolean accountLocked) {
        this.accountLocked = accountLocked;
    }

    public void setAccountEnabled(boolean accountEnabled) {
        this.accountEnabled = accountEnabled;
    }

    public void setFailedLoginAttempts(int failedLoginAttempts) {
        this.failedLoginAttempts = failedLoginAttempts;
    }

    public void setLastLogin(LocalDateTime lastLogin) {
        this.lastLogin = lastLogin;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public boolean isMfaEnabled() {
        return mfaEnabled;
    }

    public String getMfaSecret() {
        return mfaSecret;
    }

    public boolean isAccountLocked() {
        return accountLocked;
    }

    public boolean isAccountEnabled() {
        return accountEnabled;
    }

    public int getFailedLoginAttempts() {
        return failedLoginAttempts;
    }

    public LocalDateTime getLastLogin() {
        return lastLogin;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
