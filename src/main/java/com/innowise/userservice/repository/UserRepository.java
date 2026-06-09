package com.innowise.userservice.repository;

import com.innowise.userservice.model.entity.User;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, UUID> {

  Optional<User> findByIdAndDeletedFalse(UUID id);

  Optional<User> findByEmailAndDeletedFalse(String email);

  boolean existsByEmailAndDeletedFalse(String email);

  @Modifying
  @Query("UPDATE User u SET u.active = :active WHERE u.id = :id")
  int updateActiveStatus(@Param("id") UUID id, @Param("active") boolean active);
}