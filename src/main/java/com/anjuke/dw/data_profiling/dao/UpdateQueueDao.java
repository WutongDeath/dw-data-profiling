package com.anjuke.dw.data_profiling.dao;

import java.util.List;

import org.springframework.dao.DataAccessException;

import com.anjuke.dw.data_profiling.model.UpdateQueue;

public interface UpdateQueueDao {
    Integer insert(UpdateQueue updateQueue) throws DataAccessException;
    Integer insert(Integer tableId) throws DataAccessException;
    List<UpdateQueue> findByStatus(int status, int lastId) throws DataAccessException;
    boolean updateStatus(int id, int status) throws DataAccessException;
}
