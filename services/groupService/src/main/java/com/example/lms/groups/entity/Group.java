package com.example.lms.groups.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "groups")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Group {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ElementCollection
  @CollectionTable(name = "group_users", joinColumns = @JoinColumn(name = "group_id"))
  @Column(name = "user_id")
  @Builder.Default
  private List<UUID> usersId = new ArrayList<>();

  @Column(nullable = false)
  private UUID courseId;

  private LocalDate startDate;
}
