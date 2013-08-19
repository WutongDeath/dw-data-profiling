package com.anjuke.dw.data_profiling.model;

import java.util.Date;

public class Database {
    private Integer id;
    private Integer serverId;
    private String name;
    private Integer tableCount;
    private Date updated;
    public Integer getId() {
        return id;
    }
    public void setId(Integer id) {
        this.id = id;
    }
    public Integer getServerId() {
        return serverId;
    }
    public void setServerId(Integer serverId) {
        this.serverId = serverId;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public Integer getTableCount() {
        return tableCount;
    }
    public void setTableCount(Integer tableCount) {
        this.tableCount = tableCount;
    }
    public Date getUpdated() {
        return updated;
    }
    public void setUpdated(Date updated) {
        this.updated = updated;
    }
}
