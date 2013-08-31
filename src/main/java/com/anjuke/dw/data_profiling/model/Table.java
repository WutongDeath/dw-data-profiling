package com.anjuke.dw.data_profiling.model;

import java.util.Date;

public class Table {
    public static int STATUS_UNKNOWN = 0;
    public static int STATUS_NEW = 1;
    public static int STATUS_PROCESSED = 2;
    public static int STATUS_ERROR = 3;
    private Integer id;
    private Integer databaseId;
    private String name;
    private Integer status;
    private Integer progress = 0;
    private Integer columnCount;
    private Long rowCount;
    private Long dataLength;
    private Date updated;
    public Integer getId() {
        return id;
    }
    public void setId(Integer id) {
        this.id = id;
    }
    public Integer getDatabaseId() {
        return databaseId;
    }
    public void setDatabaseId(Integer databaseId) {
        this.databaseId = databaseId;
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
    public Integer getProgress() {
        return progress;
    }
    public void setProgress(Integer progress) {
        this.progress = progress;
    }
    public Integer getColumnCount() {
        return columnCount;
    }
    public void setColumnCount(Integer columnCount) {
        this.columnCount = columnCount;
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
