package com.innowise.userservice.util;

import com.innowise.userservice.model.dto.request.UserPatchDto;
import com.innowise.userservice.model.entity.User;

public final class UserUtil {

  public static void update(User user, UserPatchDto dto) {
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
  }
}