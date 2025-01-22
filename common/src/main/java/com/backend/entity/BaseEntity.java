package com.backend.entity;

import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public abstract class BaseEntity implements Serializable {
     @Serial
     static final long serialVersionUID = 1L;
     String createdBy;
     String updatedBy;
     LocalDateTime createdAt;
     LocalDateTime updatedAt;
}
