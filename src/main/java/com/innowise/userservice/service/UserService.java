package com.innowise.userservice.service;

import com.innowise.userservice.model.dto.UserCreationDto;
import com.innowise.userservice.model.dto.UserDto;
import com.innowise.userservice.model.dto.UserPatchDto;
import com.innowise.userservice.model.entity.User;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {

  UserDto createUser(UserCreationDto dto);

  UserDto getUserById(UUID id);

  Page<UserDto> getUsersFiltered(String name, String surname, Pageable pageable);

  UserDto updateUser(UUID id, UserPatchDto dto);

  void activateUser(UUID id);

  void deactivateUser(UUID id);

  User getUserEntityById(UUID id);
}