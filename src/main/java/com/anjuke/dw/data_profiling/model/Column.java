package com.anjuke.dw.data_profiling.model;

import java.util.Date;

public class Column {
    private Integer id;
    private Integer tableId;
    private String name;
    private String type;
    private Integer typeFlag;
    private String stats;
    private Date updated;
    public Integer getId() {
        return id;
    }
    public void setId(Integer id) {
        this.id = id;
    }
    public Integer getTableId() {
        return tableId;
    }
    public void setTableId(Integer tableId) {
        this.tableId = tableId;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }
    public Integer getTypeFlag() {
        return typeFlag;
    }
    public void setTypeFlag(Integer typeFlag) {
        this.typeFlag = typeFlag;
    }
    public String getStats() {
        return stats;
    }
    public void setStats(String stats) {
        this.stats = stats;
    }
    public Date getUpdated() {
        return updated;
    }
    public void setUpdated(Date updated) {
        this.updated = updated;
    }
}
