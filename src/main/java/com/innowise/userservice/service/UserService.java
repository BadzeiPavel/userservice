package com.innowise.userservice.service;

import com.innowise.commonstarter.model.dto.request.UserCreationDto;
import com.innowise.commonstarter.model.dto.UserDto;
import com.innowise.userservice.model.dto.request.UserPatchDto;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {

  UserDto createUser(UserCreationDto dto);

  UserDto getUserById(UUID id);

  UserDto getUserByEmail(String email);

  Page<UserDto> getUsersFiltered(String name, String surname, Pageable pageable);

  UserDto updateUser(UUID id, UserPatchDto dto);

  UserDto changeUserActiveStatus(UUID id, boolean active);

  UserDto deleteUser(UUID id, boolean hardDeletion);
}