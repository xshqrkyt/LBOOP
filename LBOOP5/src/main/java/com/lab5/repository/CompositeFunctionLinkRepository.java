package com.lab5.repository;

import com.lab5.entity.CompositeFunctionLink;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CompositeFunctionLinkRepository extends JpaRepository<CompositeFunctionLink, Long> {
    // Все ссылки по id composite функции без сортировки
    List<CompositeFunctionLink> findByCompositeFunctionId(Long compositeFunctionId);

    // Все ссылки по id функции без сортировки
    List<CompositeFunctionLink> findByFunctionId(Long functionId);

    // Все ссылки по id composite функции с сортировкой
    List<CompositeFunctionLink> findByCompositeFunctionId(Long compositeFunctionId, Sort sort);

    // Все ссылки по id функции с сортировкой
    List<CompositeFunctionLink> findByFunctionId(Long functionId, Sort sort);
}