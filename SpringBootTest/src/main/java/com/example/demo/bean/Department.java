package com.example.demo.bean;

import java.io.Serializable;

public class Department implements Serializable {

    private Integer id;
    private String departmentName;

    public Department(String departmentName) {
        super();
        this.departmentName = departmentName;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }
}
