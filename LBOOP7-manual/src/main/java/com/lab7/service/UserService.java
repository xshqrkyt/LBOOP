package com.lab7.service;

import com.lab7.dao.UserDAO;
import com.lab7.dto.UserRequest;
import com.lab7.dto.UserResponse;
import com.lab7.entity.User;
import com.lab7.enums.UserRole;
import com.lab7.mapper.Mapper;
import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public class UserService {
    private final UserDAO userDAO;
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    public UserService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    public UserResponse createUser(UserRequest request) throws SQLException, IOException {
        logger.info("Начинаю создание пользователя с username={}", request.getUsername());
        try {
            User user = Mapper.toEntity(request);

            // Хэшируем пароль
            String hashedPassword = BCrypt.hashpw(request.getPasswordHash(), BCrypt.gensalt());

            // Устанавливаем в объект User хеш пароля
            user.setPasswordHash(hashedPassword);

            Long id = userDAO.create(user);
            user.setId(id);

            logger.info("Пользователь {} успешно создан с id={}", request.getUsername(), id);
            return Mapper.toResponse(user);
        }

        catch (SQLException | IOException error) {
            logger.error("Ошибка при создании пользователя с username={}", request.getUsername(), error);
            throw error;
        }
    }

    public UserResponse getUserById(Long id) throws SQLException, IOException {
        logger.info("Получение пользователя по id={}", id);
        try {
            User user = userDAO.findId(id);
            return Mapper.toResponse(user);
        }

        catch (SQLException | IOException error) {
            logger.error("Ошибка при получении пользователя по id={}", id, error);
            throw error;
        }
    }

    public UserResponse getUserByUsername(String username) throws SQLException, IOException {
        logger.info("Получение пользователя по username={}", username);
        try {
            User user = userDAO.findUsername(username);
            return Mapper.toResponse(user);
        }

        catch (SQLException | IOException error) {
            logger.error("Ошибка при получении пользователя по username={}", username, error);
            throw error;
        }
    }

    public UserResponse getUserByEmail(String email) throws SQLException, IOException {
        logger.info("Получение пользователя по email={}", email);
        try {
            User user = userDAO.findByEmail(email);
            return Mapper.toResponse(user);
        }

        catch (SQLException | IOException error) {
            logger.error("Ошибка при получении пользователя по email={}", email, error);
            throw error;
        }
    }

    public List<UserResponse> findUsersByCriteria(String email, String roleStr) throws SQLException {
        logger.info("Поиск пользователей по критериям email={}, role={}", email, roleStr);
        try {
            UserRole role = roleStr != null ? UserRole.valueOf(roleStr) : null;
            List<User> users = userDAO.findCriteria(email, role);
            return users.stream().map(Mapper::toResponse).collect(Collectors.toList());
        }

        catch (SQLException error) {
            logger.error("Ошибка при поиске пользователей по критериям email={}, role={}", email, roleStr, error);
            throw error;
        }
    }

    public UserResponse updateUser(Long id, UserRequest request) throws SQLException, IOException {
        logger.info("Обновление пользователя id={}", id);
        try {
            User user = userDAO.findId(id);
            if (user == null) {
                logger.warn("Пользователь id={} не найден", id);
                return null;
            }

            user.setEmail(request.getEmail());
            if (request.getPasswordHash() != null && !request.getPasswordHash().isBlank()) {
                user.setPasswordHash(BCrypt.hashpw(request.getPasswordHash(), BCrypt.gensalt()));
            }
            user.setRole(request.getRole());
            userDAO.update(user);
            logger.info("Пользователь id={} успешно обновлен", id);

            return Mapper.toResponse(user);
        }

        catch (SQLException | IOException error) {
            logger.error("Ошибка при обновлении пользователя id={}", id, error);
            throw error;
        }
    }

    public boolean deleteUser(Long id) throws SQLException, IOException {
        logger.info("Удаление пользователя id={}", id);
        try {
            User user = userDAO.findId(id);
            if (user == null) {
                logger.warn("Пользователь id={} не найден, удаление невозможно", id);
                return false;
            }

            userDAO.delete(id);
            logger.info("Пользователь id={} успешно удален", id);
            return true;
        }

        catch (SQLException | IOException error) {
            logger.error("Ошибка при удалении пользователя id={}", id, error);
            throw error;
        }
    }
}