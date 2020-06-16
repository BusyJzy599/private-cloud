package com.cloud.demo.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HdfsPath {
    private String name;
    private String newName;
    private String pathName;
    private String types;
    private Long size;
    private Long createTime;

    public HdfsPath(String name, String pathName) {
        this.name=name;
        this.pathName=pathName;
    }
}
