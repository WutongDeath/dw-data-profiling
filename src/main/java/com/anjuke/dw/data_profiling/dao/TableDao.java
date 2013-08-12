package com.anjuke.dw.data_profiling.dao;

import org.springframework.dao.DataAccessException;

import com.anjuke.dw.data_profiling.model.Table;

public interface TableDao {
    Table findById(int id) throws DataAccessException;
}
