package com.lab7.service;

import com.lab7.dao.CompositeFunctionLinkDAO;
import com.lab7.dto.CompositeFunctionLinkRequest;
import com.lab7.dto.CompositeFunctionLinkResponse;
import com.lab7.entity.CompositeFunctionLink;
import com.lab7.mapper.Mapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public class CompositeFunctionLinkService {
    private final CompositeFunctionLinkDAO dao;
    private static final Logger logger = LoggerFactory.getLogger(CompositeFunctionLinkService.class);

    public CompositeFunctionLinkService(CompositeFunctionLinkDAO dao) {
        this.dao = dao;
    }

    public CompositeFunctionLinkResponse createLink(CompositeFunctionLinkRequest request) throws SQLException, IOException {
        logger.info("Начинаю создание ссылки.");
        try{
            CompositeFunctionLink link = Mapper.toEntity(request);
            Long id = dao.create(link);
            link.setId(id);
            return Mapper.toResponse(link);
        }

        catch (SQLException | IOException error) {
            logger.error("Ошибка при создании ссылки.");
            throw error;
        }
    }

    public CompositeFunctionLinkResponse getLinkById(Long id) throws SQLException, IOException {
        logger.info("Получение ссылки по id={}", id);
        try {
            CompositeFunctionLink link = dao.findId(id);
            return Mapper.toResponse(link);
        }

        catch (SQLException | IOException error) {
            logger.error("Ошибка при получении ссылки по id={}", id, error);
            throw error;
        }
    }

    public List<CompositeFunctionLinkResponse> getLinksByCompositeFunctionId(Long compositeFunctionId) throws SQLException, IOException {
        logger.info("Получение ссылки по compositeFunctionId={}", compositeFunctionId);
        try {
            List<CompositeFunctionLink> links = dao.findByCompositeFunctionId(compositeFunctionId);
            return links.stream().map(Mapper::toResponse).collect(Collectors.toList());
        }

        catch (SQLException | IOException error) {
            logger.error("Ошибка при получении ссылки по compositeFunctionId={}", compositeFunctionId, error);
            throw error;
        }
    }

    public List<CompositeFunctionLinkResponse> getLinksByFunctionId(Long functionId) throws SQLException, IOException {
        logger.info("Получение ссылки по functionId={}", functionId);
        try {
            List<CompositeFunctionLink> links = dao.findByFunctionId(functionId);
            return links.stream().map(Mapper::toResponse).collect(Collectors.toList());
        }

        catch (SQLException | IOException error) {
            logger.error("Ошибка при получении ссылки по functionId={}", functionId, error);
            throw error;
        }
    }

    public List<CompositeFunctionLinkResponse> getLinksByIds(List<Long> ids) throws SQLException {
        logger.info("Получение ссылки по списку id.");
        try {
            List<CompositeFunctionLink> links = dao.findIds(ids);
            return links.stream().map(Mapper::toResponse).collect(Collectors.toList());
        }

        catch (SQLException error) {
            logger.error("Ошибка при получении ссылки по списку id.");
            throw error;
        }
    }

    public List<CompositeFunctionLinkResponse> getAllSorted(String sortBy, boolean ascending) throws SQLException {
        logger.info("Получение ссылки с сортировкой по полю.");
        try {
            SortOrder order = ascending ? SortOrder.ASCENDING : SortOrder.DESCENDING;
            List<CompositeFunctionLink> links = dao.findAllSorted(sortBy, order);
            return links.stream().map(Mapper::toResponse).collect(Collectors.toList());
        }

        catch (SQLException error) {
            logger.error("Ошибка при получении ссылки с сортировкой по полю.");
            throw error;
        }
    }

    public CompositeFunctionLinkResponse updateLink(Long id, CompositeFunctionLinkRequest request) throws SQLException, IOException {
        logger.info("Обновление ссылки id={}", id);
        try{
            CompositeFunctionLink link = dao.findId(id);
            if (link == null)
                return null;

            if (request.getOrderIndex() != null)
                link.setOrderIndex(request.getOrderIndex());

            if (request.getCompositeId() != null)
                link.setCompositeId(request.getCompositeId());

            if (request.getFunctionId() != null)
                link.setFunctionId(request.getFunctionId());

            dao.update(link);
            return Mapper.toResponse(link);
        }

        catch (SQLException | IOException error) {
            logger.error("Ошибка при обновлении ссылки id={}", id, error);
            throw error;
        }
    }

    public boolean deleteLink(Long id) throws SQLException, IOException {
        logger.info("Удаление ссылки id={}", id);
        try {
            CompositeFunctionLink link = dao.findId(id);
            if (link == null)
                return false;

            dao.delete(id);
            logger.info("Ссылка id={} успешно удален", id);
            return true;
        }

        catch (SQLException | IOException error) {
            logger.error("Ошибка при удалении ссылки id={}", id, error);
            throw error;
        }
    }
}