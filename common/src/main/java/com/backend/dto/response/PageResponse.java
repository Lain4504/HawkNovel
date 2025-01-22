package com.backend.dto.response;

import com.backend.dto.request.PagingRequest;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Collections;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PageResponse<T> {

    PagingRequest paging;

    long totalElements;

    int totalPages;

    @Builder.Default
    List<T> data = Collections.emptyList();
}
