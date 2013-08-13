package com.anjuke.dw.data_profiling.dao;

import org.springframework.dao.DataAccessException;

import com.anjuke.dw.data_profiling.model.UpdateQueue;

public interface UpdateQueueDao {
    Integer insert(UpdateQueue updateQueue) throws DataAccessException;
}
