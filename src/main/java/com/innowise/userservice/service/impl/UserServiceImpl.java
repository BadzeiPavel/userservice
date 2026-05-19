package com.innowise.userservice.service.impl;

import com.innowise.userservice.exception.EntityNotFoundException;
import com.innowise.userservice.mapper.UserMapper;
import com.innowise.userservice.model.dto.UserCreationDto;
import com.innowise.userservice.model.dto.UserDto;
import com.innowise.userservice.model.dto.UserPatchDto;
import com.innowise.userservice.model.entity.User;
import com.innowise.userservice.repository.UserRepository;
import com.innowise.userservice.repository.specification.SpecificationHelper;
import com.innowise.userservice.repository.specification.UserSpecification;
import com.innowise.userservice.service.UserService;
import jakarta.persistence.EntityManager;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

  private final UserRepository userRepository;
  private final EntityManager entityManager;
  private final UserMapper userMapper;

  @Override
  public UserDto createUser(UserCreationDto dto) {
    if (userRepository.existsByEmailAndDeletedFalse(dto.email())) {
      throw new IllegalArgumentException("Email already in use: " + dto.email());
    }
    User user = userMapper.toUser(dto);
    user = userRepository.save(user);
    return userMapper.toUserDto(user);
  }

  @Override
  @Transactional(readOnly = true)
  public UserDto getUserById(UUID id) {
    User user = findUserById(id);
    return userMapper.toUserDto(user);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<UserDto> getUsersFiltered(String name, String surname, Pageable pageable) {
    Specification<User> spec = UserSpecification.withFilters(name, surname);
    Page<User> page = SpecificationHelper.findPage(entityManager, User.class, spec, pageable);
    return page.map(userMapper::toUserDto);
  }

  @Override
  public UserDto updateUser(UUID id, UserPatchDto dto) {
    User user = findUserById(id);
    if (dto.name() != null) {
      user.setName(dto.name());
    }
    if (dto.surname() != null) {
      user.setSurname(dto.surname());
    }
    if (dto.birthDate() != null) {
      user.setBirthDate(dto.birthDate());
    }
    if (dto.email() != null) {
      user.setEmail(dto.email());
    }
    user = userRepository.save(user);
    return userMapper.toUserDto(user);
  }

  @Override
  public void activateUser(UUID id) {
    int rows = userRepository.updateActiveStatus(id, true);
    if (rows == 0) {
      throw new EntityNotFoundException("User not found with id: " + id);
    }
  }

  @Override
  public void deactivateUser(UUID id) {
    int rows = userRepository.updateActiveStatus(id, false);
    if (rows == 0) {
      throw new EntityNotFoundException("User not found with id: " + id);
    }
  }

  @Override
  @Transactional(readOnly = true)
  public User getUserEntityById(UUID id) {
    return findUserById(id);
  }

  private User findUserById(UUID id) {
    return userRepository.findByIdAndDeletedFalse(id)
        .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));
  }
}