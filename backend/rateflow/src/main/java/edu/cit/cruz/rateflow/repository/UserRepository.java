package edu.cit.cruz.rateflow.repository;

import java.util.Optional;
 
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
 
import edu.cit.cruz.rateflow.entity.User;
 
@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);

    Optional<User> findByEmailOrUsername(String email, String username);
    Optional<User> findByResetToken(String resetToken);
}
