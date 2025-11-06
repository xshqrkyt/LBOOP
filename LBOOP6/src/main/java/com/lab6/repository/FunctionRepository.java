package com.lab6.repository;

import com.lab6.entity.Function;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FunctionRepository extends JpaRepository<Function, Long> {
    List<Function> findByOwnerId(Long ownerId);
    List<Function> findByName(String name);
    List<Function> findByNameAndTypeAndOwnerId(String name, String type, Long ownerId, Sort sort);
    List<Function> findByType(String type);

    @Query("SELECT f FROM Function f LEFT JOIN FETCH f.points WHERE f.id = :id")
    Optional<Function> findByIdWithPoints(@Param("id") Long id);

    @Query("SELECT f FROM Function f LEFT JOIN FETCH f.points WHERE f.owner.id = :ownerId")
    List<Function> findByOwnerIdWithPoints(@Param("ownerId") Long ownerId);

    @Query("SELECT f FROM Function f WHERE f.owner.id = :ownerId ORDER BY f.createdAt DESC")
    List<Function> findByOwnerIdOrderByCreatedAtDesc(@Param("ownerId") Long ownerId);

    @Query("SELECT f FROM Function f WHERE f.owner.id = :ownerId ORDER BY f.name ASC")
    List<Function> findByOwnerIdOrderByNameAsc(@Param("ownerId") Long ownerId);
}