package com.anjuke.dw.data_profiling.form;

import org.hibernate.validator.constraints.NotEmpty;

public class TableForm {
    @NotEmpty
    private String tableName;
    public String getTableName() {
        return tableName;
    }
    public void setTableName(String tableName) {
        this.tableName = tableName;
    }
}
