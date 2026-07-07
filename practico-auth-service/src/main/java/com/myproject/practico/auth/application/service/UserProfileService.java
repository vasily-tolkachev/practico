package com.myproject.practico.auth.application.service;

import com.myproject.practico.auth.application.dto.UserProfileResponse;
import com.myproject.practico.auth.application.port.GetUserProfileUseCase;
import com.myproject.practico.auth.application.port.IdentityRepository;
import com.myproject.practico.auth.application.port.UpdateUserProfileUseCase;
import com.myproject.practico.auth.application.port.UserRepository;
import com.myproject.practico.auth.domain.Identity;
import com.myproject.practico.auth.domain.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserProfileService implements GetUserProfileUseCase, UpdateUserProfileUseCase {

    private final UserRepository userRepository;
    private final IdentityRepository identityRepository;

    public UserProfileService(UserRepository userRepository, IdentityRepository identityRepository) {
        this.userRepository = userRepository;
        this.identityRepository = identityRepository;
    }

    @Override
    public Optional<UserProfileResponse> getByUserId(UUID userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            return Optional.empty();
        }
        User user = userOptional.get();
        String email = identityRepository.findFirstByUserId(userId)
                .map(Identity::email)
                .orElse(null);
        return Optional.of(new UserProfileResponse(
                user.id(),
                user.displayName(),
                email,
                user.createdAt()
        ));
    }

    @Override
    @Transactional
    public Optional<UserProfileResponse> updateDisplayName(UUID userId, String displayName) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            return Optional.empty();
        }
        User user = userOptional.get();
        Instant now = Instant.now();
        User updated = userRepository.save(new User(
                user.id(),
                displayName,
                user.createdAt(),
                now
        ));
        String email = identityRepository.findFirstByUserId(userId)
                .map(identity -> {
                    Identity updatedIdentity = new Identity(
                            identity.id(),
                            identity.userId(),
                            identity.provider(),
                            identity.providerSubject(),
                            identity.email(),
                            displayName,
                            identity.avatarUrl(),
                            identity.createdAt()
                    );
                    return identityRepository.save(updatedIdentity).email();
                })
                .orElse(null);
        return Optional.of(new UserProfileResponse(
                updated.id(),
                updated.displayName(),
                email,
                updated.createdAt()
        ));
    }
}
