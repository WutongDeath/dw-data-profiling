package com.anjuke.dw.data_profiling.dao;

import java.util.List;

import org.springframework.dao.DataAccessException;

import com.anjuke.dw.data_profiling.model.Column;

public interface ColumnDao {
    Column findById(int id) throws DataAccessException;
    List<Column> findByTableId(int tableId) throws DataAccessException;
    Integer insert(Column column) throws DataAccessException;
}
