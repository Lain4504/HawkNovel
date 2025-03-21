package com.backend.profileservice.mapper;

import com.backend.profileservice.dto.request.UserReadHistoryRequest;
import com.backend.profileservice.dto.response.UserReadHistoryResponse;
import com.backend.profileservice.entity.UserReadHistory;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UserReadHistoryMapper {
    UserReadHistory toUserReadHistory(UserReadHistoryRequest request);

    UserReadHistoryResponse toUserReadHistoryResponse(UserReadHistory userReadHistory);

    void updateUserReadHistory(@MappingTarget UserReadHistory userReadHistory, UserReadHistoryRequest request);
}
