package com.lab7.service;

import com.lab7.dao.CompositeFunctionDAO;
import com.lab7.dto.CompositeFunctionRequest;
import com.lab7.dto.CompositeFunctionResponse;
import com.lab7.entity.CompositeFunction;
import com.lab7.mapper.Mapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public class CompositeFunctionService {
    private final CompositeFunctionDAO compositeFunctionDAO;
    private static final Logger logger = LoggerFactory.getLogger(CompositeFunctionService.class);

    public CompositeFunctionService(CompositeFunctionDAO compositeFunctionDAO) {
        this.compositeFunctionDAO = compositeFunctionDAO;
    }

    public CompositeFunctionResponse createCompositeFunction(CompositeFunctionRequest request) throws SQLException, IOException {
        logger.info("Начинаю создание сложной функции с name={}", request.getName());
        try {CompositeFunction compositeFunction = Mapper.toEntity(request);
            Long id = compositeFunctionDAO.create(compositeFunction);
            compositeFunction.setId(id);
            return Mapper.toResponse(compositeFunction);
        }

        catch (SQLException | IOException error) {
            logger.error("Ошибка при создании сложной функции с name={}", request.getName());
            throw error;
        }
    }

    public CompositeFunctionResponse getCompositeFunctionById(Long id) throws SQLException, IOException {
        logger.info("Получение сложной функции по id={}", id);
        try {
            CompositeFunction compositeFunction = compositeFunctionDAO.findId(id);
            return Mapper.toResponse(compositeFunction);
        }

        catch (SQLException | IOException error) {
            logger.error("Ошибка при получении сложной функции по id={}", id, error);
            throw error;
        }
    }

    public List<CompositeFunctionResponse> getCompositeFunctionsByName(String name) throws SQLException, IOException {
        logger.info("Получение сложной функции по name={}", name);
        try {
            List<CompositeFunction> compositeFunctions = compositeFunctionDAO.findName(name);
            return compositeFunctions.stream().map(Mapper::toResponse).collect(Collectors.toList());
        }

        catch (SQLException | IOException error) {
            logger.error("Ошибка при получении сложной функции по name={}", name, error);
            throw error;
        }
    }

    public List<CompositeFunctionResponse> getCompositeFunctionsByNamesSorted(List<String> names, String sortBy, boolean ascending) throws SQLException {
        logger.info("Получение сложной функции по именам с сортировкой.");
        try {
            SortOrder order = ascending ? SortOrder.ASCENDING : SortOrder.DESCENDING;
            List<CompositeFunction> result = compositeFunctionDAO.findAllSorted(names, sortBy, order);

            return result.stream().map(Mapper::toResponse).collect(Collectors.toList());
        }

        catch (SQLException error) {
            logger.error("Ошибка при получении сложной функции по именам с сортировкой.");
            throw error;
        }
    }

    public List<CompositeFunctionResponse> getCompositeFunctionsByOwnerId(Long ownerId) throws SQLException, IOException {
        logger.info("Получение сложной функции по ownerId={}", ownerId);
        try {
            List<CompositeFunction> compositeFunctions = compositeFunctionDAO.findOwnerId(ownerId);
            return compositeFunctions.stream().map(Mapper::toResponse).collect(Collectors.toList());
        }

        catch (SQLException | IOException error) {
            logger.error("Ошибка при получении сложной функции по ownerId={}", ownerId, error);
            throw error;
        }
    }

    public CompositeFunctionResponse updateCompositeFunction(Long id, CompositeFunctionRequest request) throws SQLException, IOException {
        logger.info("Обновление сложной функции id={}", id);
        try{
            CompositeFunction compositeFunction = compositeFunctionDAO.findId(id);
            if (compositeFunction == null)
                return null;

            if (request.getName() != null)
                compositeFunction.setName(request.getName());

            if (request.getOwnerId() != null)
                compositeFunction.setOwnerId(request.getOwnerId());

            compositeFunctionDAO.update(compositeFunction);

            return Mapper.toResponse(compositeFunction);
        }

        catch (SQLException | IOException error) {
            logger.error("Ошибка при обновлении сложной функции id={}", id, error);
            throw error;
        }
    }

    public boolean deleteCompositeFunction(Long id) throws SQLException, IOException {
        logger.info("Удаление сложной функции id={}", id);
        try{
            CompositeFunction compositeFunction = compositeFunctionDAO.findId(id);
            if (compositeFunction == null)
                return false;

            compositeFunctionDAO.delete(id);
            logger.info("Сложная функция id={} успешно удален", id);
            return true;
        }

        catch (SQLException | IOException error) {
            logger.error("Ошибка при удалении сложной функции id={}", id, error);
            throw error;
        }
    }
}
