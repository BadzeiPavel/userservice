package com.innowise.userservice.service.impl;

import com.innowise.commonstarter.model.dto.UserDto;
import com.innowise.commonstarter.model.dto.request.UserCreationDto;
import com.innowise.userservice.config.cache.RedisConfig;
import com.innowise.userservice.exception.UserServiceException;
import com.innowise.userservice.mapper.UserMapper;
import com.innowise.userservice.model.dto.request.UserPatchDto;
import com.innowise.userservice.model.entity.User;
import com.innowise.userservice.repository.UserRepository;
import com.innowise.userservice.repository.specification.SpecificationHelper;
import com.innowise.userservice.repository.specification.UserSpecification;
import com.innowise.userservice.service.UserService;
import jakarta.persistence.EntityManager;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
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
  @CachePut(value = RedisConfig.USER_CACHE, key = "#result.id")
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
  @Cacheable(value = RedisConfig.USER_CACHE, key = "#id")
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
  @CachePut(value = RedisConfig.USER_CACHE, key = "#id")
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
  @CachePut(value = RedisConfig.USER_CACHE, key = "#id")
  public UserDto changeUserActiveStatus(UUID id, boolean active) {
    return active ? activateUser(id) : deactivateUser(id);
  }

  private UserDto activateUser(UUID id) {
    int rows = userRepository.updateActiveStatus(id, true);
    if (rows == 0) {
      throw new UserServiceException("User not found with id: " + id);
    }
    return userMapper.toUserDto(findUserById(id));
  }

  private UserDto deactivateUser(UUID id) {
    int rows = userRepository.updateActiveStatus(id, false);
    if (rows == 0) {
      throw new UserServiceException("User not found with id: " + id);
    }
    return userMapper.toUserDto(findUserById(id));
  }

  @Override
  @Caching(evict = {
      @CacheEvict(value = RedisConfig.USER_CACHE, key = "#id")
  })
  public UserDto deleteUser(UUID id, boolean hardDeletion) {
    return hardDeletion ? hardDeleteUser(id) : softDeleteUser(id);
  }

  private UserDto softDeleteUser(UUID id) {
    User user = findUserById(id);
    user.setDeleted(true);
    userRepository.save(user);
    return userMapper.toUserDto(user);
  }

  private UserDto hardDeleteUser(UUID id) {
    User user = findUserById(id);
    UserDto deletedDto = userMapper.toUserDto(user);
    userRepository.delete(user);
    return deletedDto;
  }

  private User findUserById(UUID id) {
    return userRepository.findByIdAndDeletedFalse(id)
        .orElseThrow(() -> new UserServiceException("User not found with id: " + id));
  }
}