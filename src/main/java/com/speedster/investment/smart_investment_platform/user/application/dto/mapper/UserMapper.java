package com.speedster.investment.smart_investment_platform.user.application.dto.mapper;

import com.speedster.investment.smart_investment_platform.user.application.dto.UserResponse;
import com.speedster.investment.smart_investment_platform.user.domain.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserResponse toResponse(User user);
}
