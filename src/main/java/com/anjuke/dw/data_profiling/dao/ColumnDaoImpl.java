package com.anjuke.dw.data_profiling.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.support.JdbcDaoSupport;

import com.anjuke.dw.data_profiling.model.Column;

public class ColumnDaoImpl extends JdbcDaoSupport implements ColumnDao {

    @Override
    public Column findById(int id) throws DataAccessException {
        try {
            return getJdbcTemplate().queryForObject(
                    "SELECT id, table_id, name, type, type_flag, stats, updated FROM dp_column WHERE id = ?",
                    rowMapper, id);
        } catch (IncorrectResultSizeDataAccessException e) {
            return null;
        }
    }

    @Override
    public List<Column> findByTableId(int tableId) throws DataAccessException {
        return getJdbcTemplate().query(
                "SELECT id, table_id, name, type, type_flag, stats, updated FROM dp_column WHERE table_id = ? ORDER BY id",
                rowMapper, tableId);
    }

    private RowMapper<Column> rowMapper = new RowMapper<Column>() {

        @Override
        public Column mapRow(ResultSet rs, int rowNum) throws SQLException {
            Column column = new Column();
            column.setId(rs.getInt("id"));
            column.setTableId(rs.getInt("table_id"));
            column.setName(rs.getString("name"));
            column.setType(rs.getString("type"));
            column.setTypeFlag(rs.getInt("type_flag"));
            column.setStats(rs.getString("stats"));
            column.setUpdated(rs.getDate("updated"));
            return column;
        }

    };

    @Override
    public Integer insert(Column column) throws DataAccessException {

        int rows = getJdbcTemplate().update(
                "INSERT INTO dp_column (table_id, name, type, type_flag, stats) VALUES (?, ?, ?, ?, ?)",
                column.getTableId(),
                column.getName(),
                column.getType(),
                column.getTypeFlag(),
                column.getStats());

        if (rows == 0) {
            return null;
        }

        return getJdbcTemplate().queryForObject("SELECT LAST_INSERT_ID()", Integer.class);
    }

}
