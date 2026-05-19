package com.innowise.userservice.controller;

import com.innowise.userservice.model.dto.UserDto;
import com.innowise.userservice.model.dto.request.UserCreationDto;
import com.innowise.userservice.model.dto.request.UserPatchDto;
import com.innowise.userservice.service.UserService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/users")
public class UserController {

  private final UserService userService;

  @PostMapping
  public ResponseEntity<UserDto> createUser(@Valid @RequestBody UserCreationDto dto) {
    UserDto created = userService.createUser(dto);
    return ResponseEntity.status(HttpStatus.CREATED).body(created);
  }

  @GetMapping("/{id}")
  public ResponseEntity<UserDto> getUserById(@PathVariable UUID id) {
    return ResponseEntity.ok(userService.getUserById(id));
  }

  @GetMapping
  public ResponseEntity<Page<UserDto>> getUsersFiltered(
      @RequestParam(required = false) String name,
      @RequestParam(required = false) String surname,
      Pageable pageable) {
    Page<UserDto> page = userService.getUsersFiltered(name, surname, pageable);
    return ResponseEntity.ok(page);
  }

  @PatchMapping("/{id}")
  public ResponseEntity<UserDto> updateUser(
      @PathVariable UUID id,
      @Valid @RequestBody UserPatchDto dto) {
    return ResponseEntity.ok(userService.updateUser(id, dto));
  }

  @PutMapping("/{id}/activate")
  public ResponseEntity<UserDto> activateUser(@PathVariable UUID id) {
    return ResponseEntity.ok(userService.activateUser(id));
  }

  @PutMapping("/{id}/deactivate")
  public ResponseEntity<UserDto> deactivateUser(@PathVariable UUID id) {
    return ResponseEntity.ok(userService.deactivateUser(id));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<UserDto> softDeleteUser(@PathVariable UUID id) {
    return ResponseEntity.ok(userService.softDeleteUser(id));
  }

  @DeleteMapping("/{id}/hard")
  public ResponseEntity<UserDto> hardDeleteUser(@PathVariable UUID id) {
    return ResponseEntity.ok(userService.hardDeleteUser(id));
  }
}