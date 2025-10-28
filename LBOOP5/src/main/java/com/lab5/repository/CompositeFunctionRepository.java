package com.lab5.repository;

import com.lab5.entity.CompositeFunction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;

@Repository
public interface CompositeFunctionRepository extends JpaRepository<CompositeFunction, Long> {
    // Поиск composite функций по владельцу
    List<CompositeFunction> findByOwnerId(Long ownerId);

    // Поиск по частичному совпадению имени с игнорированием регистра
    List<CompositeFunction> findByNameContainingIgnoreCase(String name);

    // Поиск composite функций владельца с фильтром по имени
    List<CompositeFunction> findByOwnerIdAndNameContainingIgnoreCase(Long ownerId, String name);

    // Получение composite функции с загрузкой списка compositeLinks (fetch join)
    @Query("SELECT cf FROM CompositeFunction cf LEFT JOIN FETCH cf.compositeLinks WHERE cf.id = :id")
    Optional<CompositeFunction> findByIdWithLinks(@Param("id") Long id);

    // Получение всех composite функций владельца с загрузкой compositeLinks с сортировкой
    @Query("SELECT cf FROM CompositeFunction cf LEFT JOIN FETCH cf.compositeLinks WHERE cf.owner.id = :ownerId")
    List<CompositeFunction> findByOwnerIdWithLinks(@Param("ownerId") Long ownerId, Sort sort);
}
