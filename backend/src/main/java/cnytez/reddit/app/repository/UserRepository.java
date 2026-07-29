package cnytez.reddit.app.repository;

import cnytez.reddit.app.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    List<User> findAllByDeletionDateIsNull();

    Optional<User> findByIdAndDeletionDateIsNull(Long id);

    Optional<User> findByUsernameAndDeletionDateIsNull(String username);

    Optional<User> findByEmail(String email);

    boolean existsByUsernameAndDeletionDateIsNull(String username);

    boolean existsByEmail(String email);
}
