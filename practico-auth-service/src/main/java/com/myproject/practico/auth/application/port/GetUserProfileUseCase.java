package com.myproject.practico.auth.application.port;

import com.myproject.practico.auth.application.dto.UserProfileResponse;

import java.util.Optional;
import java.util.UUID;

public interface GetUserProfileUseCase {
    Optional<UserProfileResponse> getByUserId(UUID userId);
}
