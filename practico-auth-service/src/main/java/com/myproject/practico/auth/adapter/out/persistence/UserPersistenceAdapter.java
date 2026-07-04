package com.myproject.practico.auth.adapter.out.persistence;

import com.myproject.practico.auth.adapter.out.persistence.entity.UserJpaEntity;
import com.myproject.practico.auth.application.port.UserRepository;
import com.myproject.practico.auth.domain.User;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class UserPersistenceAdapter implements UserRepository {

    private final UserJpaRepository repository;

    public UserPersistenceAdapter(UserJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<User> findById(UUID id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    public User save(User user) {
        return toDomain(repository.save(toEntity(user)));
    }

    private UserJpaEntity toEntity(User user) {
        return new UserJpaEntity(
                user.id(),
                user.displayName(),
                user.createdAt(),
                user.updatedAt()
        );
    }

    private User toDomain(UserJpaEntity entity) {
        return new User(
                entity.getId(),
                entity.getDisplayName(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
