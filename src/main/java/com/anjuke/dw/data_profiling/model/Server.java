package com.anjuke.dw.data_profiling.model;

import java.util.Date;

public class Server {
    private Integer id;
    private String name;
    private String host;
    private Integer port;
    private String username;
    private String password;
    private Integer databaseCount;
    private Date updated;
    public Integer getId() {
        return id;
    }
    public void setId(Integer id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getHost() {
        return host;
    }
    public void setHost(String host) {
        this.host = host;
    }
    public Integer getPort() {
        return port;
    }
    public void setPort(Integer port) {
        this.port = port;
    }
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    public Integer getDatabaseCount() {
        return databaseCount;
    }
    public void setDatabaseCount(Integer databaseCount) {
        this.databaseCount = databaseCount;
    }
    public Date getUpdated() {
        return updated;
    }
    public void setUpdated(Date updated) {
        this.updated = updated;
    }
}
