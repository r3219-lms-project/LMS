package com.example.lms.groups.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupDto {
  private UUID id;
  private UUID courseId;
  private LocalDate startDate;
  private List<UUID> usersId;
}
