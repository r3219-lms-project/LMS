package ru.lms_project.userservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.lms_project.userservice.model.User;
import java.util.UUID;
import java.util.Optional;
import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);
    
    Optional<User> findById(UUID id);

    List<User> findAll();

}
