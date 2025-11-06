package com.lab6.repository;

import com.lab6.entity.CompositeFunctionLink;
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

    // Поиск по списку id
    List<CompositeFunctionLink> findByIdIn(List<Long> ids);

    // Поиск всех CompositeFunctionLink с сортировкой по id
    List<CompositeFunctionLink> findAllByOrderById(Sort sort);
}