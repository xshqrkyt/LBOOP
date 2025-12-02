package com.lab7.service;

import com.lab7.dao.PointsDAO;
import com.lab7.dto.PointsRequest;
import com.lab7.dto.PointsResponse;
import com.lab7.entity.Points;
import com.lab7.mapper.Mapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public class PointsService {
    private final PointsDAO pointsDAO;
    private static final Logger logger = LoggerFactory.getLogger(PointsService.class);

    public PointsService(PointsDAO pointsDAO) {
        this.pointsDAO = pointsDAO;
    }

    public PointsResponse createPoints(PointsRequest request) throws SQLException, IOException {
        logger.info("Начинаю создание точек функции.");
        try{
            Points points = Mapper.toEntity(request);
            Long id = pointsDAO.create(points);
            points.setId(id);

            return Mapper.toResponse(points);
        }

        catch (SQLException | IOException error) {
            logger.error("Ошибка при создании точек функции.");
            throw error;
        }
    }

    public PointsResponse getPointsById(Long id) throws SQLException, IOException {
        logger.info("Получение точек функции по id={}", id);
        try{
            Points points = pointsDAO.findId(id);
            return Mapper.toResponse(points);
        }

        catch (SQLException | IOException error) {
            logger.error("Ошибка при получении точек функции по id={}", id, error);
            throw error;
        }
    }

    public List<PointsResponse> getPointsByFunctionId(Long functionId, String sortBy, boolean ascending) throws SQLException {
        logger.info("Получение точек функции по functionId={}", functionId);
        try {
            SortOrder order = ascending ? SortOrder.ASCENDING : SortOrder.DESCENDING;
            List<Points> pointsList = pointsDAO.findFunctionIdSorted(functionId, sortBy, order);

            return pointsList.stream().map(Mapper::toResponse).collect(Collectors.toList());
        }

        catch (SQLException error) {
            logger.error("Ошибка при получении точек функции по ownerId={}", functionId, error);
            throw error;
        }
    }

    public List<PointsResponse> getPointsByOwnerId(Long ownerId, String sortBy, boolean ascending) throws SQLException {
        logger.info("Получение точек функции по ownerId={}", ownerId);
        try {
            SortOrder order = ascending ? SortOrder.ASCENDING : SortOrder.DESCENDING;
            List<Points> pointsList = pointsDAO.findOwnerIdSorted(ownerId, sortBy, order);

            return pointsList.stream().map(Mapper::toResponse).collect(Collectors.toList());
        }

        catch (SQLException error) {
            logger.error("Ошибка при получении точек функции по ownerId={}", ownerId, error);
            throw error;
        }
    }

    public List<PointsResponse> getPointsByUserId(Long userId) throws SQLException, IOException {
        logger.info("Получение точек функции по userId={}", userId);
        try {
            List<Points> pointsList = pointsDAO.findUserId(userId);
            return pointsList.stream().map(Mapper::toResponse).collect(Collectors.toList());
        }

        catch (SQLException error) {
            logger.error("Ошибка при получении точек функции по ownerId={}", userId, error);
            throw error;
        }
    }

    public PointsResponse updatePoints(Long id, PointsRequest request) throws SQLException, IOException {
        logger.info("Обновление точек функции id={}", id);
        try{
            Points points = pointsDAO.findId(id);
            if (points == null)
                return null;

            if (request.getXValues() != null)
                points.setXValues(request.getXValues());

            if (request.getYValues() != null)
                points.setYValues(request.getYValues());

            if (request.getFunctionId() != null)
                points.setFunctionId(request.getFunctionId());

            pointsDAO.update(points);

            return Mapper.toResponse(points);
        }

        catch (SQLException | IOException error) {
            logger.error("Ошибка при обновлении точек функции id={}", id, error);
            throw error;
        }
    }

    public boolean deletePoints(Long id) throws SQLException, IOException {
        logger.info("Удаление точек функции id={}", id);
        try {
            Points points = pointsDAO.findId(id);
            if (points == null)
                return false;

            pointsDAO.delete(id);
            logger.info("Точки id={} успешно удалены", id);
            return true;
        }

        catch (SQLException | IOException error) {
            logger.error("Ошибка при удалении точек функции id={}", id, error);
            throw error;
        }
    }
}