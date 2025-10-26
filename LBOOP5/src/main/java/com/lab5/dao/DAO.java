package com.lab5.dao;

import java.sql.SQLException;

public interface DAO<T> {
    Long create(T entity) throws SQLException;
    T findId(Long id) throws SQLException;
    void update(T entity) throws SQLException;
    void delete(Long id) throws SQLException;
}