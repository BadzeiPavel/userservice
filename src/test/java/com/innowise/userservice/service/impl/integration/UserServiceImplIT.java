package com.innowise.userservice.service.impl.integration;

import com.innowise.userservice.exception.UserServiceException;
import com.innowise.userservice.model.dto.UserDto;
import com.innowise.userservice.model.dto.request.UserCreationDto;
import com.innowise.userservice.model.dto.request.UserPatchDto;
import com.innowise.userservice.repository.UserRepository;
import com.innowise.userservice.service.UserService;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserServiceImplIT extends BaseIntegrationTest {

  @Autowired
  private UserService userService;

  @Autowired
  private UserRepository userRepository;

  @BeforeEach
  void cleanUp() {
    userRepository.deleteAll();
  }

  @Test
  void createUser_shouldPersistAndReturnDto() {
    UserCreationDto dto = TestDataFactory.createUserCreationDto("Alice", "Smith");
    UserDto result = userService.createUser(dto);
    assertThat(result.id()).isNotNull();
    assertThat(result.email()).isEqualTo("alice@example.com");
  }

  @Test
  void getUserById_shouldReturnUser() {
    UserDto user = userService.createUser(TestDataFactory.createUserCreationDto("John", "Doe"));
    UserDto found = userService.getUserById(user.id());
    assertThat(found.email()).isEqualTo(user.email());
  }

  @Test
  void getUsersFiltered_shouldReturnPageFilteredByName() {
    userService.createUser(TestDataFactory.createUserCreationDto("John", "Smith"));
    userService.createUser(TestDataFactory.createUserCreationDto("Johnny", "Doe"));
    userService.createUser(TestDataFactory.createUserCreationDto("Alice", "Johnson"));

    Page<UserDto> page = userService.getUsersFiltered("john", null, PageRequest.of(0, 10));
    assertThat(page.getTotalElements()).isEqualTo(2);
    assertThat(page.getContent()).extracting(UserDto::name).containsOnly("John", "Johnny");
  }

  @Test
  void updateUser_shouldChangeFields() {
    UserDto user = userService.createUser(TestDataFactory.createUserCreationDto("John", "Doe"));
    UserPatchDto patch = new UserPatchDto("Jane", null, null, "jane@example.com");
    UserDto updated = userService.updateUser(user.id(), patch);
    assertThat(updated.name()).isEqualTo("Jane");
    assertThat(updated.email()).isEqualTo("jane@example.com");
  }

  @Test
  void activateDeactivateUser_shouldToggleActive() {
    UserDto user = userService.createUser(TestDataFactory.createUserCreationDto("John", "Doe"));
    userService.changeUserActiveStatus(user.id(), false);
    UserDto deactivated = userService.getUserById(user.id());
    assertThat(deactivated.active()).isFalse();

    userService.changeUserActiveStatus(user.id(), true);
    UserDto activated = userService.getUserById(user.id());
    assertThat(activated.active()).isTrue();
  }

  @Test
  void softDeleteUser_shouldMarkDeleted() {
    UserDto user = userService.createUser(TestDataFactory.createUserCreationDto("John", "Doe"));
    userService.deleteUser(user.id(), false);
    assertThat(userRepository.findByIdAndDeletedFalse(user.id())).isEmpty();
  }

  @Test
  void hardDeleteUser_shouldRemoveFromDb() {
    UserDto user = userService.createUser(TestDataFactory.createUserCreationDto("John", "Doe"));
    UUID userId = user.id();
    userService.deleteUser(userId, true);
    assertThat(userRepository.findById(userId)).isEmpty();
  }

  @Test
  void hardDeleteUser_shouldThrowIfNotFound() {
    assertThatThrownBy(() -> userService.deleteUser(UUID.randomUUID(), true))
        .isInstanceOf(UserServiceException.class);
  }
}