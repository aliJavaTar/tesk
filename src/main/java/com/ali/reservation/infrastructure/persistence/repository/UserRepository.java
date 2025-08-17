package com.ali.reservation.infrastructure.persistence.repository;

import com.ali.reservation.infrastructure.persistence.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByUsername(String username);

    @Query("""
            SELECT CASE WHEN COUNT(u) > 0
                        THEN true
                        ELSE false
                   END
            FROM UserEntity u
            WHERE u.username = :username OR u.email = :email
            """)
    boolean existsByUsernameOrEmail(@Param("username") String username,
                                    @Param("email") String email);

}
