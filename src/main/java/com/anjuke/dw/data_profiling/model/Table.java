package com.anjuke.dw.data_profiling.model;

import java.util.Date;

public class Table {
    private Integer id;
    private Integer connectionId;
    private String name;
    private Integer status;
    private Long rowCount;
    private Long dataLength;
    private Date updated;
    public Integer getId() {
        return id;
    }
    public void setId(Integer id) {
        this.id = id;
    }
    public Integer getConnectionId() {
        return connectionId;
    }
    public void setConnectionId(Integer connectionId) {
        this.connectionId = connectionId;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public Integer getStatus() {
        return status;
    }
    public void setStatus(Integer status) {
        this.status = status;
    }
    public Long getRowCount() {
        return rowCount;
    }
    public void setRowCount(Long rowCount) {
        this.rowCount = rowCount;
    }
    public Long getDataLength() {
        return dataLength;
    }
    public void setDataLength(Long dataLength) {
        this.dataLength = dataLength;
    }
    public Date getUpdated() {
        return updated;
    }
    public void setUpdated(Date updated) {
        this.updated = updated;
    }
}
