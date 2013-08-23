package com.anjuke.dw.data_profiling.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.RowMapper;
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

    @Override
    public List<UpdateQueue> findByStatus(int status, int lastId) throws DataAccessException {
        return getJdbcTemplate().query(
                "SELECT id, table_id, status, updated FROM dp_update_queue"
                + " WHERE status = ? AND id > ? ORDER BY id LIMIT 20",
                rowMapper, status, lastId);
    }

    @Override
    public boolean updateStatus(int id, int status) throws DataAccessException {
        return getJdbcTemplate().update(
                "UPDATE dp_update_queue SET status = ? WHERE id = ?",
                status, id) > 0;
    }

    private RowMapper<UpdateQueue> rowMapper = new RowMapper<UpdateQueue>() {

        @Override
        public UpdateQueue mapRow(ResultSet rs, int rowNum) throws SQLException {
            UpdateQueue row = new UpdateQueue();
            row.setId(rs.getInt("id"));
            row.setTableId(rs.getInt("table_id"));
            row.setStatus(rs.getInt("status"));
            row.setUpdated(rs.getTimestamp("updated"));
            return row;
        }

    };

}
