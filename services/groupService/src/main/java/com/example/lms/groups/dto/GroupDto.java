package com.example.lms.groups.dto;

import lombok.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupDto {
  private UUID id;
  private UUID courseId;
  private LocalDate startDate;
  private List<UUID> usersId;
}
