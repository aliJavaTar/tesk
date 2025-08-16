package com.ali.reservation.infrastructure.persistence.repository;

import com.ali.reservation.infrastructure.persistence.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
}
