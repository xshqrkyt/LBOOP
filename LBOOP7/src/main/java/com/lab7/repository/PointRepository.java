package com.lab7.repository;

import com.lab7.entity.Point;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PointRepository extends JpaRepository<Point, Long> {
    // Найти все точки по id функции
    Point findByFunctionId(Long functionId);

    // Получить все точки, где функция принадлежит владельцу с конкретным id
    @Query("SELECT p FROM Point p WHERE p.function.owner.id = :ownerId")
    Point findByFunctionOwnerId(@Param("ownerId") Long ownerId);
}