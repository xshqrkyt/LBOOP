package com.lab7.dao;

import java.io.IOException;
import java.sql.SQLException;

public interface DAO<T> {
    Long create(T entity) throws SQLException, IOException;
    T findId(Long id) throws SQLException, IOException;
    void update(T entity) throws SQLException, IOException;
    void delete(Long id) throws SQLException, IOException;
}