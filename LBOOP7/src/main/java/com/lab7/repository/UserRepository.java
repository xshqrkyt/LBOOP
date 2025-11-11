package com.lab7.repository;

import com.lab7.enums.UserRole;
import com.lab7.entity.User;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    // Множественный поиск
    List<User> findByEmailAndRole(String email, UserRole role);
    // Поиск с сортировкой
    List<User> findByUsername(String username, Sort sort);
}