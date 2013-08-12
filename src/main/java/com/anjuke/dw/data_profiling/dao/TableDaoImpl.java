package com.anjuke.dw.data_profiling.dao;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.support.JdbcDaoSupport;

import com.anjuke.dw.data_profiling.model.Table;

public class TableDaoImpl extends JdbcDaoSupport implements TableDao {

    @Override
    public Table findById(int id) throws DataAccessException {
        return getJdbcTemplate().queryForObject(
                "SELECT id, connection_id, name, status, row_count, data_length, updated FROM dp_table WHERE id = ?",
                new Object[] { id }, rowMapper);
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
