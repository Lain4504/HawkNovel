package com.backend.identityservice.entity;

import com.backend.entity.BaseEntity;
import com.backend.identityservice.enums.UserState;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
public class User extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;
    String username;
    String email;
    String password;
    @Enumerated(EnumType.STRING)
    UserState state;
    @ManyToMany
    Set<Role> roles;
}
