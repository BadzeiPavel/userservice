package com.innowise.userservice.service.impl;

import com.innowise.userservice.config.cache.RedisConfig;
import com.innowise.userservice.exception.EntityNotFoundException;
import com.innowise.userservice.mapper.UserMapper;
import com.innowise.userservice.model.dto.UserDto;
import com.innowise.userservice.model.dto.request.UserCreationDto;
import com.innowise.userservice.model.dto.request.UserPatchDto;
import com.innowise.userservice.model.entity.User;
import com.innowise.userservice.repository.UserRepository;
import com.innowise.userservice.repository.specification.SpecificationHelper;
import com.innowise.userservice.repository.specification.UserSpecification;
import com.innowise.userservice.service.UserService;
import com.innowise.userservice.util.UserUtil;
import jakarta.persistence.EntityManager;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.CacheManager;
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
  private final CacheManager cacheManager;

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
    UserUtil.update(user, dto);
    user = userRepository.save(user);
    return userMapper.toUserDto(user);
  }

  @Override
  @CachePut(value = RedisConfig.USER_CACHE, key = "#id")
  public UserDto activateUser(UUID id) {
    int rows = userRepository.updateActiveStatus(id, true);
    if (rows == 0) {
      throw new EntityNotFoundException("User not found with id: " + id);
    }
    return getUserById(id);
  }

  @Override
  @CachePut(value = RedisConfig.USER_CACHE, key = "#id")
  public UserDto deactivateUser(UUID id) {
    int rows = userRepository.updateActiveStatus(id, false);
    if (rows == 0) {
      throw new EntityNotFoundException("User not found with id: " + id);
    }
    return getUserById(id);
  }

  @Override
  @Transactional(readOnly = true)
  public User getUserEntityById(UUID id) {
    return findUserById(id);
  }

  @Override
  @Caching(evict = {
      @CacheEvict(value = RedisConfig.USER_CACHE, key = "#id")
  })
  public UserDto softDeleteUser(UUID id) {
    User user = findUserById(id);
    user.setDeleted(true);
    userRepository.save(user);
    return userMapper.toUserDto(user);
  }

  @Override
  @Caching(evict = {
      @CacheEvict(value = RedisConfig.USER_CACHE, key = "#id")
  })
  public UserDto hardDeleteUser(UUID id) {
    User user = findUserById(id);
    UserDto deletedDto = userMapper.toUserDto(user);

    if (user.getPaymentCards() != null) {
      user.getPaymentCards().forEach(card ->
          Objects.requireNonNull(cacheManager.getCache(RedisConfig.CARD_CACHE)).evict(card.getId())
      );
    }

    userRepository.delete(user);
    return deletedDto;
  }

  private User findUserById(UUID id) {
    return userRepository.findByIdAndDeletedFalse(id)
        .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));
  }
}