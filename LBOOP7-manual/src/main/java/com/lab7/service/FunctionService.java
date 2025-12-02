package com.lab7.service;

import com.lab7.dao.FunctionDAO;
import com.lab7.dto.FunctionRequest;
import com.lab7.dto.FunctionResponse;
import com.lab7.entity.Function;
import com.lab7.enums.FunctionType;
import com.lab7.mapper.Mapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public class FunctionService {
    private final FunctionDAO functionDAO;
    private static final Logger logger = LoggerFactory.getLogger(FunctionService.class);

    public FunctionService(FunctionDAO functionDAO) {
        this.functionDAO = functionDAO;
    }

    public FunctionResponse createFunction(FunctionRequest request) throws SQLException, IOException {
        logger.info("Начинаю создание функции с name={}", request.getName());
        try {
            Function function = Mapper.toEntity(request);
            Long id = functionDAO.create(function);
            function.setId(id);

        return Mapper.toResponse(function);
        }

        catch (SQLException | IOException error) {
            logger.error("Ошибка при создании функции с name={}", request.getName());
            throw error;
        }
    }

    public FunctionResponse getFunctionById(Long id) throws SQLException, IOException {
        logger.info("Получение функции по id={}", id);
        try{
            Function function = functionDAO.findId(id);
            return Mapper.toResponse(function);
        }

        catch (SQLException | IOException error) {
            logger.error("Ошибка при получении функции по id={}", id, error);
            throw error;
        }
    }

    public List<FunctionResponse> getFunctionsByName(String name) throws SQLException, IOException {
        List<Function> functions = functionDAO.findName(name);
        return functions.stream().map(Mapper::toResponse).collect(Collectors.toList());
    }

    public List<FunctionResponse> getFunctionsByOwnerId(Long ownerId) throws SQLException, IOException {
        logger.info("Получение функции по ownerId={}", ownerId);
        try{
            List<Function> functions = functionDAO.findOwnerId(ownerId);
            return functions.stream().map(Mapper::toResponse).collect(Collectors.toList());
        }

        catch (SQLException | IOException error) {
            logger.error("Ошибка при получении функции по ownerId={}", ownerId, error);
            throw error;
        }
    }

    public List<FunctionResponse> getFunctionsByType(String typeStr) throws SQLException, IOException {
        logger.info("Получение функции по typeStr={}", typeStr);
        try {
            if (typeStr == null)
                return List.of();

            FunctionType type = FunctionType.valueOf(typeStr);
            List<Function> functions = functionDAO.findType(type);

            return functions.stream().map(Mapper::toResponse).collect(Collectors.toList());
        }

        catch (SQLException | IOException error) {
            logger.error("Ошибка при получении функции по typeStr={}", typeStr, error);
            throw error;
        }
    }

    public List<FunctionResponse> findFunctionsByCriteria(String name, String typeStr, Long ownerId, String sortBy, boolean ascending) throws SQLException {
        logger.info("Получение функции по по нескольким полям с сортировкой.");
        try {
            FunctionType type = (typeStr != null) ? FunctionType.valueOf(typeStr) : null;
            SortOrder order = ascending ? SortOrder.ASCENDING : SortOrder.DESCENDING;

            List<Function> functions = functionDAO.findCriteriaSorted(name, type, ownerId, sortBy, order);
            return functions.stream().map(Mapper::toResponse).collect(Collectors.toList());
        }

        catch (SQLException error) {
            logger.error("Ошибка при получении функции по нескольким полям с сортировкой.", error);
            throw error;
        }
    }

    public FunctionResponse updateFunction(Long id, FunctionRequest request) throws SQLException, IOException {
        logger.info("Обновление функции id={}", id);
        try{
            Function function = functionDAO.findId(id);
            if (function == null)
                return null;

            if (request.getName() != null)
                function.setName(request.getName());

            if (request.getType() != null)
                function.setType(FunctionType.valueOf(request.getType()));

            if (request.getOwnerId() != null)
                function.setOwnerId(request.getOwnerId());

            functionDAO.update(function);
            return Mapper.toResponse(function);
        }

        catch (SQLException | IOException error) {
            logger.error("Ошибка при обновлении функции id={}", id, error);
            throw error;
        }
    }

    public boolean deleteFunction(Long id) throws SQLException, IOException {
        logger.info("Удаление функции id={}", id);
        try{
            Function function = functionDAO.findId(id);
             if (function == null)
                return false;

            functionDAO.delete(id);
            logger.info("Функция id={} успешно удалена", id);
            return true;
        }

        catch (SQLException | IOException error) {
            logger.error("Ошибка при удалении функции id={}", id, error);
            throw error;
        }
    }
}