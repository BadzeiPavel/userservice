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

  boolean existsByEmailAndDeletedFalse(String email);

  @Query("SELECT u FROM User u WHERE u.deleted = false AND " +
      "(LOWER(u.name) LIKE LOWER(CONCAT('%', :name, '%')) OR " +
      "LOWER(u.surname) LIKE LOWER(CONCAT('%', :surname, '%')))")
  Page<User> searchByNameOrSurname(@Param("name") String name,
      @Param("surname") String surname,
      Pageable pageable);

  @Query(value = "SELECT * FROM users WHERE id = :id AND deleted = false",
      nativeQuery = true)
  Optional<User> findByIdNative(@Param("id") UUID id);

  @Modifying
  @Query("UPDATE User u SET u.active = :active WHERE u.id = :id")
  int updateActiveStatus(@Param("id") UUID id, @Param("active") boolean active);
}