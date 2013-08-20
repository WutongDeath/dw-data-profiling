package com.anjuke.dw.data_profiling.dao;

import java.util.List;

import org.springframework.dao.DataAccessException;

import com.anjuke.dw.data_profiling.model.Table;

public interface TableDao {
    Table findById(int id) throws DataAccessException;
    List<Table> findByDatabaseId(int databaseId) throws DataAccessException;
    boolean nameExists(String name) throws DataAccessException;
    Integer insert(Table table) throws DataAccessException;
    boolean delete(int tableId) throws DataAccessException;
    Table findByDatabaseIdAndTableName(int databaseId, String tableName) throws DataAccessException;
}
