package com.anjuke.dw.data_profiling.model;

import java.util.Date;

public class UpdateQueue {
    public static int STATUS_NEW = 1;
    public static int STATUS_PROCESSED = 2;
    public static int STATUS_ERROR = 3;
    private Integer id;
    private Integer tableId;
    private Integer status;
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
    public Integer getStatus() {
        return status;
    }
    public void setStatus(Integer status) {
        this.status = status;
    }
    public Date getUpdated() {
        return updated;
    }
    public void setUpdated(Date updated) {
        this.updated = updated;
    }
}
