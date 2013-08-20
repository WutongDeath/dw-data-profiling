package com.anjuke.dw.data_profiling.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.support.JdbcDaoSupport;

import com.anjuke.dw.data_profiling.model.Server;

public class ServerDaoImpl extends JdbcDaoSupport implements ServerDao {

    @Override
    public List<Server> findAll() throws DataAccessException {
        return getJdbcTemplate().query(
                "SELECT id, name, host, port, username, password, database_count, updated"
                + " FROM dp_server ORDER BY name",
                rowMapper);
    }

    @Override
    public Server findById(int serverId) throws DataAccessException {
        try {
            return getJdbcTemplate().queryForObject(
                    "SELECT id, name, host, port, username, password, database_count, updated"
                    + " FROM dp_server WHERE id = ?",
                    rowMapper, serverId);
        } catch (IncorrectResultSizeDataAccessException e) {
            return null;
        }
    }

    @Override
    public Integer insert(Server server) throws DataAccessException {
        int affectedRows = getJdbcTemplate().update(
                "INSERT INTO dp_server (name, host, port, username, password, database_count)"
                + " VALUES (?, ?, ?, ?, ?, ?)",
                server.getName(),
                server.getHost(),
                server.getPort(),
                server.getUsername(),
                server.getPassword(),
                server.getDatabaseCount());
        if (affectedRows == 0) {
            return null;
        }
        return getJdbcTemplate().queryForObject("SELECT LAST_INSERT_ID()", Integer.class);
    }

    @Override
    public boolean nameExsits(String serverName) throws DataAccessException {
        return getJdbcTemplate().queryForObject(
                "SELECT COUNT(*) FROM dp_server WHERE name = ?",
                Integer.class, serverName) > 0;
    }

    @Override
    public boolean delete(int serverId) throws DataAccessException {
        return getJdbcTemplate().update(
                "DELETE FROM dp_server WHERE id = ?",
                serverId) > 0;
    }

    private RowMapper<Server> rowMapper = new RowMapper<Server>() {

        @Override
        public Server mapRow(ResultSet rs, int rowNum) throws SQLException {
            Server server = new Server();
            server.setId(rs.getInt("id"));
            server.setName(rs.getString("name"));
            server.setHost(rs.getString("host"));
            server.setPort(rs.getInt("port"));
            server.setUsername(rs.getString("username"));
            server.setPassword(rs.getString("password"));
            server.setDatabaseCount(rs.getInt("database_count"));
            server.setUpdated(rs.getDate("updated"));
            return server;
        }

    };

}
