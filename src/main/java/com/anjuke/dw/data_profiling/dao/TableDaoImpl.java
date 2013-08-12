package com.anjuke.dw.data_profiling.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.support.JdbcDaoSupport;

import com.anjuke.dw.data_profiling.model.Table;

public class TableDaoImpl extends JdbcDaoSupport implements TableDao {

    @Override
    public Table findById(int id) throws DataAccessException {
        try {
            return getJdbcTemplate().queryForObject(
                    "SELECT id, connection_id, name, status, row_count, data_length, updated FROM dp_table WHERE id = ?",
                    rowMapper, id);
        } catch (IncorrectResultSizeDataAccessException e) {
            return null;
        }
    }

    @Override
    public List<Table> findByConnectionId(int connectionId) throws DataAccessException {
        return getJdbcTemplate().query(
                "SELECT id, connection_id, name, status, row_count, data_length, updated FROM dp_table WHERE connection_id = ? ORDER BY id",
                rowMapper, connectionId);

    }

    private RowMapper<Table> rowMapper = new RowMapper<Table>() {

        @Override
        public Table mapRow(ResultSet rs, int rowNum) throws SQLException {
            Table table = new Table();
            table.setId(rs.getInt("id"));
            table.setConnectionId(rs.getInt("connection_id"));
            table.setName(rs.getString("name"));
            table.setStatus(rs.getInt("status"));
            table.setRowCount(rs.getLong("row_count"));
            table.setDataLength(rs.getLong("data_length"));
            table.setUpdated(rs.getDate("updated"));
            return table;
        }

    };

}
