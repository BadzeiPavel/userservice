package com.innowise.userservice.model.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
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
@Table(name = "users")
public class User extends BaseEntity {

  @Id
  @GeneratedValue
  @EqualsAndHashCode.Include
  @Column(name = "id", nullable = false, updatable = false)
  private UUID id;

  @NotBlank(message = "Name is required")
  @Size(min = 1, max = 50, message = "Name must be between 1 and 50 characters")
  @Column(name = "name", nullable = false, length = 50)
  private String name;

  @NotBlank(message = "Surname is required")
  @Size(min = 1, max = 50, message = "Surname must be between 1 and 50 characters")
  @Column(name = "surname", nullable = false, length = 50)
  private String surname;

  @NotNull(message = "Birth date is required")
  @Past(message = "Birth date must be in the past")
  @Column(name = "birth_date", nullable = false)
  private LocalDate birthDate;

  @NotBlank(message = "Email is required")
  @Email(message = "Email must be valid")
  @Size(max = 100, message = "Email must not exceed 100 characters")
  @Column(name = "email", nullable = false, unique = true, length = 100)
  private String email;

  @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  @Builder.Default
  private Set<PaymentCard> paymentCards = new HashSet<>();
}
