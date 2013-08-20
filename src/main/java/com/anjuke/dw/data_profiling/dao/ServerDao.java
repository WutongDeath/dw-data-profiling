package com.anjuke.dw.data_profiling.dao;

import java.util.List;

import org.springframework.dao.DataAccessException;

import com.anjuke.dw.data_profiling.model.Server;

public interface ServerDao {
    List<Server> findAll() throws DataAccessException;
    Server findById(int serverId) throws DataAccessException;
    Integer insert(Server server) throws DataAccessException;
    boolean nameExsits(String serverName) throws DataAccessException;
    boolean delete(int serverId) throws DataAccessException;
}
