package com.ima.repository;

import com.ima.entity.Screen;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.ima.entity.Role;
import java.util.List;
import java.util.Optional;

@Repository
public interface ScreenRepository extends JpaRepository<Screen, Long> {
    Optional<Screen> findByScreenId(String screenId);
    Optional<Screen> findByPath(String path);
    List<Screen> findByAllowedRolesIn(List<Role> roles);
    List<Screen> findByIsActive(boolean isActive);
}
