package com.anjuke.dw.data_profiling.form;

import javax.validation.constraints.Pattern;

public class TableForm {
    @Pattern(regexp="[0-9a-zA-Z$_]+")
    private String tableName;
    public String getTableName() {
        return tableName;
    }
    public void setTableName(String tableName) {
        this.tableName = tableName;
    }
}
