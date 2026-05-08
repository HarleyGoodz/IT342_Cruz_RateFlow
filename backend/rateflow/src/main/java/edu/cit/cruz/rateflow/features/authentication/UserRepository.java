package edu.cit.cruz.rateflow.features.authentication;

import java.util.Optional;
 
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
 
@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);

    Optional<User> findByEmailOrUsername(String email, String username);
    Optional<User> findByResetToken(String resetToken);
}
