package com.anjuke.dw.data_profiling.dao;

import java.util.List;

import org.springframework.dao.DataAccessException;

import com.anjuke.dw.data_profiling.model.Connection;

public interface ConnectionDao {
    Connection findById(int id) throws DataAccessException;
    List<Connection> findAll() throws DataAccessException;
}
