package com.anjuke.dw.data_profiling.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.jdbc.support.rowset.SqlRowSet;

import com.anjuke.dw.data_profiling.model.Table;
import com.anjuke.dw.data_profiling.util.Functions;

public class TableDaoImpl extends JdbcDaoSupport implements TableDao {

    private static String INSERT_FIELDS = "database_id, name, status, column_count, row_count, data_length";
    private static String SELECT_FIELDS = "id, updated, " + INSERT_FIELDS;

    @Override
    public Table findById(int id) throws DataAccessException {
        try {
            return getJdbcTemplate().queryForObject(
                    "SELECT " + SELECT_FIELDS + " FROM dp_table WHERE id = ?",
                    rowMapper, id);
        } catch (IncorrectResultSizeDataAccessException e) {
            return null;
        }
    }

    @Override
    public List<Table> findByDatabaseId(int databaseId) throws DataAccessException {
        return getJdbcTemplate().query(
                "SELECT " + SELECT_FIELDS + " FROM dp_table WHERE database_id = ? ORDER BY id",
                rowMapper, databaseId);

    }

    private RowMapper<Table> rowMapper = new RowMapper<Table>() {

        @Override
        public Table mapRow(ResultSet rs, int rowNum) throws SQLException {
            Table table = new Table();
            table.setId(rs.getInt("id"));
            table.setDatabaseId(rs.getInt("database_id"));
            table.setName(rs.getString("name"));
            table.setStatus(rs.getInt("status"));
            table.setColumnCount(rs.getInt("column_count"));
            table.setRowCount(rs.getLong("row_count"));
            table.setDataLength(rs.getLong("data_length"));
            table.setUpdated(rs.getTimestamp("updated"));
            return table;
        }

    };

    @Override
    public boolean nameExists(String name) throws DataAccessException {
        SqlRowSet rs = getJdbcTemplate().queryForRowSet("SELECT 1 FROM dp_table WHERE name = ?", name);
        return rs.next();
    }

    @Override
    public Integer insert(Table table) throws DataAccessException {

        int rows = getJdbcTemplate().update(
                "INSERT INTO dp_table (" + INSERT_FIELDS + ") VALUES (?, ?, ?, ?, ?, ?)",
                table.getDatabaseId(),
                table.getName(),
                table.getStatus(),
                table.getColumnCount(),
                table.getRowCount(),
                table.getDataLength());

        if (rows == 0) {
            return null;
        }

        return getJdbcTemplate().queryForObject("SELECT LAST_INSERT_ID()", Integer.class);
    }

    @Override
    public boolean delete(int tableId) throws DataAccessException {
        return getJdbcTemplate().update(
                "DELETE FROM dp_table WHERE id = ?",
                tableId) > 0;
    }

    @Override
    public Table findByDatabaseIdAndTableName(int databaseId, String tableName)
            throws DataAccessException {
        try {
            return getJdbcTemplate().queryForObject(
                    "SELECT " + SELECT_FIELDS + " FROM dp_table WHERE database_id = ? AND name = ?",
                    rowMapper, databaseId, tableName);
        } catch (IncorrectResultSizeDataAccessException e) {
            return null;
        }
    }

    @Override
    public List<Table> findByDatabaseIdAndTableNameList(int databaseId,
            List<String> tableNameList) throws DataAccessException {

        List<Object> args = new ArrayList<Object>();
        args.add(databaseId);

        List<String> inList = new ArrayList<String>();
        for (int i = 0; i < tableNameList.size(); ++i) {
            inList.add("?");
            args.add(tableNameList.get(i));
        }

        return getJdbcTemplate().query(
                "SELECT " + SELECT_FIELDS + " FROM dp_table"
                + " WHERE database_id = ? AND name IN (" + Functions.joinString(inList, ",") + ")",
                args.toArray(), rowMapper);
    }

    @Override
    public boolean update(Table table) throws DataAccessException {
        return getJdbcTemplate().update(
                "UPDATE dp_table SET status = ?, column_count = ?, row_count = ?, data_length = ?"
                + " WHERE id = ?",
                table.getStatus(),
                table.getColumnCount(),
                table.getRowCount(),
                table.getDataLength(),
                table.getId()) > 0;
    }

}
