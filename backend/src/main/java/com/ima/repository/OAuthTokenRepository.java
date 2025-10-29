package com.ima.repository;


import com.ima.entity.OAuthToken;
import com.ima.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository
public interface OAuthTokenRepository extends JpaRepository<OAuthToken, Long> {
    Optional<OAuthToken> findByUserAndProvider(User user, String provider);
    List<OAuthToken> findByUser(User user);
}
