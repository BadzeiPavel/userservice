package com.innowise.userservice.service.impl.unit;

import com.innowise.userservice.exception.UserServiceException;
import com.innowise.userservice.mapper.UserMapper;
import com.innowise.commonstarter.model.dto.UserDto;
import com.innowise.commonstarter.model.dto.request.UserCreationDto;
import com.innowise.userservice.model.dto.request.UserPatchDto;
import com.innowise.userservice.model.entity.PaymentCard;
import com.innowise.userservice.model.entity.User;
import com.innowise.userservice.repository.UserRepository;
import com.innowise.userservice.service.impl.UserServiceImpl;
import jakarta.persistence.EntityManager;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.CacheManager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

  @Mock
  private UserRepository userRepository;
  @Mock
  private UserMapper userMapper;
  @Mock
  private EntityManager entityManager;
  @Mock
  private CacheManager cacheManager;

  @InjectMocks
  private UserServiceImpl userService;

  private final UUID userId = UUID.randomUUID();
  private final User userEntity = new User();
  private final UserDto userDto = new UserDto(userId, "John", "Doe", null, "john@example.com", true,
      null, null, null);

  @Test
  void createUser_shouldSaveAndReturnDto() {
    UserCreationDto creationDto = new UserCreationDto("John", "Doe", null, "john@example.com");
    when(userRepository.existsByEmailAndDeletedFalse("john@example.com")).thenReturn(false);
    when(userMapper.toUser(creationDto)).thenReturn(userEntity);
    when(userRepository.save(userEntity)).thenReturn(userEntity);
    when(userMapper.toUserDto(userEntity)).thenReturn(userDto);

    UserDto result = userService.createUser(creationDto);
    assertThat(result.email()).isEqualTo("john@example.com");
    verify(userRepository).save(userEntity);
  }

  @Test
  void createUser_shouldThrowWhenEmailExists() {
    UserCreationDto dto = new UserCreationDto("John", "Doe", null, "john@example.com");
    when(userRepository.existsByEmailAndDeletedFalse("john@example.com")).thenReturn(true);
    assertThatThrownBy(() -> userService.createUser(dto))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void getUserById_shouldReturnUser() {
    when(userRepository.findByIdAndDeletedFalse(userId)).thenReturn(Optional.of(userEntity));
    when(userMapper.toUserDto(userEntity)).thenReturn(userDto);
    UserDto result = userService.getUserById(userId);
    assertThat(result).isEqualTo(userDto);
  }

  @Test
  void getUserById_shouldThrowWhenNotFound() {
    when(userRepository.findByIdAndDeletedFalse(userId)).thenReturn(Optional.empty());
    assertThatThrownBy(() -> userService.getUserById(userId))
        .isInstanceOf(UserServiceException.class);
  }

  @Test
  void updateUser_shouldApplyPatchAndSave() {
    UserPatchDto patch = new UserPatchDto("Jane", null, null, null);
    when(userRepository.findByIdAndDeletedFalse(userId)).thenReturn(Optional.of(userEntity));
    when(userRepository.save(userEntity)).thenReturn(userEntity);
    when(userMapper.toUserDto(userEntity)).thenReturn(userDto);

    userService.updateUser(userId, patch);
    verify(userRepository).save(userEntity);
  }

  @Test
  void activateUser_shouldSetActiveTrue() {
    when(userRepository.updateActiveStatus(userId, true)).thenReturn(1);
    when(userRepository.findByIdAndDeletedFalse(userId)).thenReturn(Optional.of(userEntity));
    when(userMapper.toUserDto(userEntity)).thenReturn(userDto);
    UserDto result = userService.changeUserActiveStatus(userId, true);
    assertThat(result.active()).isTrue();
  }

  @Test
  void deactivateUser_shouldSetActiveFalse() {
    User deactivatedEntity = new User();
    deactivatedEntity.setId(userId);
    deactivatedEntity.setActive(false);
    UserDto deactivatedDto = new UserDto(
        userId, "John", "Doe", null, "john@example.com",
        false, null, null, null
    );

    when(userRepository.updateActiveStatus(userId, false)).thenReturn(1);
    when(userRepository.findByIdAndDeletedFalse(userId))
        .thenReturn(Optional.of(deactivatedEntity));
    when(userMapper.toUserDto(deactivatedEntity)).thenReturn(deactivatedDto);

    UserDto result = userService.changeUserActiveStatus(userId, false);
    assertThat(result.active()).isFalse();
  }

  @Test
  void softDeleteUser_shouldMarkDeleted() {
    when(userRepository.findByIdAndDeletedFalse(userId)).thenReturn(Optional.of(userEntity));
    when(userRepository.save(userEntity)).thenReturn(userEntity);
    when(userMapper.toUserDto(userEntity)).thenReturn(userDto);

    UserDto result = userService.deleteUser(userId, false);
    assertThat(userEntity.isDeleted()).isTrue();
  }

  @Test
  void hardDeleteUser_shouldDelete() {
    User userWithCards = new User();
    userWithCards.setId(userId);
    PaymentCard card = new PaymentCard();
    card.setId(UUID.randomUUID());
    userWithCards.setPaymentCards(Set.of(card));

    when(userRepository.findByIdAndDeletedFalse(userId)).thenReturn(Optional.of(userWithCards));
    when(userMapper.toUserDto(userWithCards)).thenReturn(userDto);

    userService.deleteUser(userId, true);
    verify(userRepository).delete(userWithCards);
  }
}