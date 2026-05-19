package com.innowise.userservice.service;

import com.innowise.userservice.model.dto.UserDto;
import com.innowise.userservice.model.dto.request.UserCreationDto;
import com.innowise.userservice.model.dto.request.UserPatchDto;
import com.innowise.userservice.model.entity.User;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {

  UserDto createUser(UserCreationDto dto);

  UserDto getUserById(UUID id);

  User getUserEntityById(UUID id);

  Page<UserDto> getUsersFiltered(String name, String surname, Pageable pageable);

  UserDto updateUser(UUID id, UserPatchDto dto);

  UserDto activateUser(UUID id);

  UserDto deactivateUser(UUID id);

  UserDto softDeleteUser(UUID id);

  UserDto hardDeleteUser(UUID id);
}