package com.lab5.repository;

import com.lab5.entity.Point;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PointRepository extends JpaRepository<Point, Long> {
    // Найти все точки по id функции
    List<Point> findByFunctionId(Long functionId);

    // Получить все точки, где функция принадлежит владельцу с конкретным id
    @Query("SELECT p FROM Point p WHERE p.function.owner.id = :ownerId")
    List<Point> findByFunctionOwnerId(@Param("ownerId") Long ownerId);

    // Найти все точки по id функции с сортировкой
    List<Point> findByFunctionId(Long functionId, Sort sort);

    // Найти точки по id владельца функции с сортировкой
    @Query("SELECT p FROM Point p WHERE p.function.owner.id = :ownerId")
    List<Point> findByFunctionOwnerId(@Param("ownerId") Long ownerId, Sort sort);
}