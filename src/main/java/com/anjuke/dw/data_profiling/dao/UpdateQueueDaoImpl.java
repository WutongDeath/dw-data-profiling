package com.anjuke.dw.data_profiling.dao;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.support.JdbcDaoSupport;

import com.anjuke.dw.data_profiling.model.UpdateQueue;

public class UpdateQueueDaoImpl extends JdbcDaoSupport implements
        UpdateQueueDao {

    @Override
    public Integer insert(UpdateQueue updateQueue) throws DataAccessException {
        int rows = getJdbcTemplate().update(
                "INSERT INTO dp_update_queue (table_id, status) VALUES (?, ?)",
                updateQueue.getTableId(),
                updateQueue.getStatus());
        if (rows == 0) {
            return null;
        }
        return getJdbcTemplate().queryForObject("SELECT LAST_INSERT_ID()", Integer.class);
    }

    @Override
    public Integer insert(Integer tableId) throws DataAccessException {
        UpdateQueue row = new UpdateQueue();
        row.setTableId(tableId);
        row.setStatus(UpdateQueue.STATUS_NEW);
        return insert(row);
    }

}
