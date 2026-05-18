package com.innowise.userservice.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Entity
@Table(name = "payment_cards")
public class PaymentCard extends BaseEntity {

  @Id
  @GeneratedValue
  @EqualsAndHashCode.Include
  @Column(name = "id", nullable = false, updatable = false)
  private UUID id;

  @NotNull(message = "User is required")
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @NotBlank(message = "Card number is required")
  @Pattern(regexp = "\\d{16}", message = "Card number must be exactly 16 digits")
  @Column(name = "number", nullable = false, length = 16)
  private String number;

  @NotBlank(message = "Card holder name is required")
  @Size(min = 1, max = 100, message = "Card holder name must be between 1 and 100 characters")
  @Column(name = "holder", nullable = false, length = 100)
  private String holder;

  @NotNull(message = "Expiration date is required")
  @Future(message = "Expiration date must be in the future")
  @Column(name = "expiration_date", nullable = false)
  private LocalDate expirationDate;
}
