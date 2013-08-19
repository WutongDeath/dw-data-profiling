package com.anjuke.dw.data_profiling.dao;

import java.util.List;

import org.springframework.dao.DataAccessException;

import com.anjuke.dw.data_profiling.model.Database;

public interface DatabaseDao {
    Database findById(int id) throws DataAccessException;
    List<Database> findByServerId(int serverId) throws DataAccessException;
    Integer insert(Database database) throws DataAccessException;
}
