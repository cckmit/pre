package com.xd.pre.generator.domain;

import lombok.Data;

/**
 * @Classname CodeGenConfig
 * @Author Created by Lihaodong (alias:小东啊) lihaodongmail@163.com
 * @Date 2019-08-04 16:34
 * @Version 1.0
 */
@Data
public class CodeGenConfig {

    /**
     * 包名
     */
    private String packageName;
    /**
     * 作者
     */
    private String author;
    /**
     * 模块名称
     */
    private String moduleName;
    /**
     * 表前缀
     */
    private String tablePrefix;

    /**
     * 表名称
     */
    private String tableName;

    /**
     * 表备注
     */
    private String comments;
}
