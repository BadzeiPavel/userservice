package com.innowise.userservice.repository;

import com.innowise.userservice.model.entity.PaymentCard;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PaymentCardRepository extends JpaRepository<PaymentCard, UUID> {

  Optional<PaymentCard> findByIdAndDeletedFalse(UUID id);

  List<PaymentCard> findByUserIdAndDeletedFalse(UUID userId);

  Page<PaymentCard> findByDeletedFalse(Pageable pageable);

  long countByUserIdAndDeletedFalse(UUID userId);

  boolean existsByNumberAndDeletedFalse(String number);

  @Query("SELECT c FROM PaymentCard c WHERE c.user.id = :userId AND c.deleted = false")
  List<PaymentCard> findAllCardsByUserId(@Param("userId") UUID userId);

  @Query(value = "SELECT * FROM payment_cards WHERE user_id = :userId AND deleted = false",
      nativeQuery = true)
  List<PaymentCard> findAllCardsByUserIdNative(@Param("userId") UUID userId);

  @Modifying
  @Query("UPDATE PaymentCard c SET c.active = :active WHERE c.id = :id")
  int updateActiveStatus(@Param("id") UUID id, @Param("active") boolean active);
}