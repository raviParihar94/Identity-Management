package com.ima.repository;

import com.ima.entity.ErrorCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ErrorCodeRepository extends JpaRepository<ErrorCode, Long> {
    Optional<ErrorCode> findByErrorCode(String errorCode);
    List<ErrorCode> findByScreenId(String screenId);
}


