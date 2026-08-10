package com.cnytez.app.repository;

import com.cnytez.app.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByUsernameAndDeletionDateIsNull(String username);

    Optional<User> findByEmail(String email);

    boolean existsByUsernameAndDeletionDateIsNull(String username);

    boolean existsByEmail(String email);
}
