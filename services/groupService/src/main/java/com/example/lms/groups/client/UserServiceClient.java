package com.example.lms.groups.client;

import com.example.lms.groups.dto.UserDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserServiceClient {

  private final RestTemplate restTemplate;

  @Value("${services.user.url:http://localhost:8082}")
  private String userServiceUrl;

  public UserDto getUserById(UUID userId) {
    try {
      String url = userServiceUrl + "/api/users/" + userId;
      log.debug("Fetching user from: {}", url);
      return restTemplate.getForObject(url, UserDto.class);
    } catch (Exception e) {
      log.error("Error fetching user with id: {}", userId, e);
      throw new RuntimeException("User service unavailable", e);
    }
  }

  public boolean validateUser(UUID userId) {
    try {
      UserDto user = getUserById(userId);
      return user != null;
    } catch (Exception e) {
      log.error("Error validating user with id: {}", userId, e);
      return false;
    }
  }
}
