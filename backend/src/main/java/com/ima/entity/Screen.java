package com.ima.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "screen")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class Screen {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, name = "screen_id")
    private String screenId; // e.g., SCR_DASHBOARD_001

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(nullable = false)
    private String path; // URL path

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "screen_roles",
            joinColumns = @JoinColumn(name = "screen_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> allowedRoles = new HashSet<>();

    @Column(name = "is_active")
    private boolean isActive = true;
}
