package com.anjuke.dw.data_profiling.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.support.JdbcDaoSupport;

import com.anjuke.dw.data_profiling.model.Connection;

public class ConnectionDaoImpl extends JdbcDaoSupport implements ConnectionDao {

    @Override
    public Connection findById(int id) throws DataAccessException {
        try {
            return getJdbcTemplate().queryForObject(
                    "SELECT id, name, host, port, username, password, `database` FROM dp_connection WHERE id = ?",
                    rowMapper, id);
        } catch (IncorrectResultSizeDataAccessException e) {
            return null;
        }
    }

    @Override
    public List<Connection> findAll() throws DataAccessException {
        return getJdbcTemplate().query(
                "SELECT id, name, host, port, username, password, `database` FROM dp_connection ORDER BY id",
                rowMapper);
    }

    private RowMapper<Connection> rowMapper = new RowMapper<Connection>() {

        @Override
        public Connection mapRow(ResultSet rs, int rowNum) throws SQLException {
            Connection connection = new Connection();
            connection.setId(rs.getInt("id"));
            connection.setName(rs.getString("name"));
            connection.setHost(rs.getString("host"));
            connection.setPort(rs.getInt("port"));
            connection.setUsername(rs.getString("username"));
            connection.setPassword(rs.getString("password"));
            connection.setDatabase(rs.getString("database"));
            return connection;
        }

    };

}
