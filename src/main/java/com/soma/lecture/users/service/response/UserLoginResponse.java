package com.soma.lecture.users.service.response;

import com.soma.lecture.users.domain.Role;
import java.util.UUID;

public record UserLoginResponse(
        UUID userUuid,
        Role role
) {
}
