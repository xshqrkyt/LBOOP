package com.lab7.repository;

import com.lab7.entity.CompositeFunction;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Sort;

import java.util.List;

@Repository
public interface CompositeFunctionRepository extends JpaRepository<CompositeFunction, Long> {
    // Поиск composite функций по владельцу
    List<CompositeFunction> findByOwnerId(Long ownerId);

    // Множественный поиск по именам функций
    List<CompositeFunction> findByNameIn(List<String> names);
    List<CompositeFunction> findByNameIn(List<String> names, Sort sort);

    // Поиск по совпадению имени
    List<CompositeFunction> findByName(String name);
}