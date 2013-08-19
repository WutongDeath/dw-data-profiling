package com.anjuke.dw.data_profiling.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.support.JdbcDaoSupport;

import com.anjuke.dw.data_profiling.model.Database;

public class DatabaseDaoImpl extends JdbcDaoSupport implements DatabaseDao {

    @Override
    public Database findById(int id) throws DataAccessException {
        try {
            return getJdbcTemplate().queryForObject(
                    "SELECT id, server_id, name, table_count, updated FROM dp_database WHERE id = ?",
                    rowMapper, id);
        } catch (IncorrectResultSizeDataAccessException e) {
            return null;
        }
    }

    @Override
    public List<Database> findByServerId(int serverId) throws DataAccessException {
        return getJdbcTemplate().query(
                "SELECT id, server_id, name, table_count, updated FROM dp_database WHERE server_id = ? ORDER BY name",
                rowMapper, serverId);
    }

    @Override
    public Integer insert(Database database) throws DataAccessException {
        int affectedRows = getJdbcTemplate().update(
                "INSERT INTO dp_database (server_id, name, table_count) VALUES (?, ?, ?)",
                database.getServerId(),
                database.getName(),
                database.getTableCount());
        if (affectedRows == 0) {
            return null;
        }
        return getJdbcTemplate().queryForObject("SELECT LAST_INSERT_ID()", Integer.class);
    }

    private RowMapper<Database> rowMapper = new RowMapper<Database>() {

        @Override
        public Database mapRow(ResultSet rs, int rowNum) throws SQLException {
            Database row = new Database();
            row.setId(rs.getInt("id"));
            row.setServerId(rs.getInt("server_id"));
            row.setName(rs.getString("name"));
            row.setTableCount(rs.getInt("table_count"));
            row.setUpdated(rs.getDate("updated"));
            return row;
        }

    };

}
