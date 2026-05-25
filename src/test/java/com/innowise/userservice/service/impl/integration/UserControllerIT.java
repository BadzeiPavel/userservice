package com.innowise.userservice.service.impl.integration;

import com.innowise.userservice.model.dto.UserDto;
import com.innowise.userservice.model.dto.request.UserCreationDto;
import com.innowise.userservice.model.dto.request.UserPatchDto;
import com.innowise.userservice.repository.UserRepository;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

class UserControllerIT extends BaseControllerIT {

  @Autowired
  private UserRepository userRepository;

  @BeforeEach
  void cleanUp() {
    userRepository.deleteAll();
  }

  @Test
  void createUser_shouldReturn201() {
    UserCreationDto dto = TestDataFactory.createUserCreationDto("Alice", "Smith");
    ResponseEntity<UserDto> response = restTemplate.postForEntity(
        baseUrl() + "/api/v1/users", dto, UserDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().email()).isEqualTo("alice@example.com");
  }

  @Test
  void getUserById_shouldReturn200() {
    UserDto created = restTemplate.postForObject(
        baseUrl() + "/api/v1/users",
        TestDataFactory.createUserCreationDto("John", "Doe"),
        UserDto.class);

    ResponseEntity<UserDto> response = restTemplate.getForEntity(
        baseUrl() + "/api/v1/users/" + created.id(), UserDto.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody().email()).isEqualTo("john@example.com");
  }

  @Test
  void getUsersFiltered_shouldReturnPage() {
    restTemplate.postForObject(baseUrl() + "/api/v1/users",
        TestDataFactory.createUserCreationDto("John", "Smith"), UserDto.class);
    restTemplate.postForObject(baseUrl() + "/api/v1/users",
        TestDataFactory.createUserCreationDto("Alice", "Johnson"), UserDto.class);

    ResponseEntity<String> response = restTemplate.exchange(
        baseUrl() + "/api/v1/users?name=john&page=0&size=10",
        HttpMethod.GET, null, String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

    DocumentContext json = JsonPath.parse(response.getBody());
    assertThat(json.read("totalElements", Integer.class)).isEqualTo(1);
    assertThat(json.read("content[0].name", String.class)).isEqualTo("John");
  }

  @Test
  void updateUser_shouldReturn200() {
    UserDto created = restTemplate.postForObject(baseUrl() + "/api/v1/users",
        TestDataFactory.createUserCreationDto("John", "Doe"), UserDto.class);

    UserPatchDto patch = new UserPatchDto("Jane", null, null, "jane@example.com");
    HttpEntity<UserPatchDto> request = createEntity(patch);
    ResponseEntity<UserDto> response = restTemplate.exchange(
        baseUrl() + "/api/v1/users/" + created.id(), HttpMethod.PATCH, request, UserDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody().name()).isEqualTo("Jane");
    assertThat(response.getBody().email()).isEqualTo("jane@example.com");
  }

  @Test
  void activateUser_shouldReturn200() {
    UserDto created = restTemplate.postForObject(baseUrl() + "/api/v1/users",
        TestDataFactory.createUserCreationDto("John", "Doe"), UserDto.class);

    ResponseEntity<UserDto> response = restTemplate.exchange(
        baseUrl() + "/api/v1/users/" + created.id() + "?active=true",
        HttpMethod.PUT, null, UserDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody().active()).isTrue();
  }

  @Test
  void deactivateUser_shouldReturn200() {
    UserDto created = restTemplate.postForObject(baseUrl() + "/api/v1/users",
        TestDataFactory.createUserCreationDto("John", "Doe"), UserDto.class);

    ResponseEntity<UserDto> response = restTemplate.exchange(
        baseUrl() + "/api/v1/users/" + created.id() + "?active=false",
        HttpMethod.PUT, null, UserDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody().active()).isFalse();
  }

  @Test
  void softDeleteUser_shouldReturn204() {
    UserDto created = restTemplate.postForObject(baseUrl() + "/api/v1/users",
        TestDataFactory.createUserCreationDto("John", "Doe"), UserDto.class);

    ResponseEntity<Void> response = restTemplate.exchange(
        baseUrl() + "/api/v1/users/" + created.id(),
        HttpMethod.DELETE, null, Void.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    assertThat(userRepository.findByIdAndDeletedFalse(created.id())).isEmpty();
  }

  @Test
  void hardDeleteUser_shouldReturn204() {
    UserDto created = restTemplate.postForObject(
        baseUrl() + "/api/v1/users",
        TestDataFactory.createUserCreationDto("John", "Doe"),
        UserDto.class);

    ResponseEntity<Void> deleteResponse = restTemplate.exchange(
        baseUrl() + "/api/v1/users/" + created.id() + "?hardDeletion=true",
        HttpMethod.DELETE, null, Void.class);
    assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

    ResponseEntity<Void> getResponse = restTemplate.exchange(
        baseUrl() + "/api/v1/users/" + created.id(),
        HttpMethod.GET, null, Void.class);
    assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
  }
}