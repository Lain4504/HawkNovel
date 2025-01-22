package com.backend.dto.request;

import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class PagingRequest {

     int currentPage;

     int pageSize;

     SortRequest sort;

}
