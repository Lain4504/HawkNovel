package com.backend.entity;

import lombok.experimental.FieldDefaults;

import java.io.Serializable;
import java.time.LocalDateTime;

@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class BaseEntity implements Serializable {
     static final long serialVersionUID = 1L;
     String id;
     String createdBy;
     String updatedBy;
     LocalDateTime createdAt;
     LocalDateTime updatedAt;

}
