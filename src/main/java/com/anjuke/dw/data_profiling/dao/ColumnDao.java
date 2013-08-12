package com.anjuke.dw.data_profiling.dao;

import java.util.List;

import org.springframework.dao.DataAccessException;

import com.anjuke.dw.data_profiling.model.Column;

public interface ColumnDao {
    List<Column> findByTableId(int tableId) throws DataAccessException;
}
