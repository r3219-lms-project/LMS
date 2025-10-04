package com.example.lms.groups;

import com.example.lms.groups.controller.GroupController;
import com.example.lms.groups.dto.GroupDto;
import com.example.lms.groups.service.GroupService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.hamcrest.Matchers.notNullValue;

@WebMvcTest(GroupController.class)
class GroupControllerTest {

  @Autowired MockMvc mvc;
  @Autowired ObjectMapper mapper;
  @MockBean GroupService groupService;

  @Test
  void createGroup_endpoint() throws Exception {
    GroupDto req = GroupDto.builder()
        .courseId(UUID.randomUUID())
        .startDate(LocalDate.now())
        .build();

    GroupDto res = GroupDto.builder()
        .id(UUID.randomUUID())
        .courseId(req.getCourseId())
        .startDate(req.getStartDate())
        .build();

    Mockito.when(groupService.createGroup(Mockito.any())).thenReturn(
        com.example.lms.groups.entity.Group.builder()
            .id(res.getId())
            .courseId(res.getCourseId())
            .startDate(res.getStartDate())
            .build()
    );

    mvc.perform(post("/api/groups")
            .contentType("application/json")
            .content(mapper.writeValueAsString(req)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id", notNullValue()));
  }
}
