package com.miya.system.module.dict.model;

import com.miya.common.module.base.BaseEntity;
import com.miya.common.module.bod.BackupOnDelete;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @author 杨超辉
 *  字典表
 */
@Getter
@Setter
@ToString
@Entity
@BackupOnDelete
@Accessors(chain = true)
@Table(indexes = {@Index(name = "code_unique", columnList = "code", unique = true)})
public class SysDict extends BaseEntity {

    /**
     * 字典名称
     */
    @Column(length = 30, nullable = false)
    private String name;

    /**
     * 业务代码
     */
    @Column(length = 30, nullable = false)
    private String code;

    /**
     * 是否为系统字典
     */
    @Column(nullable = false)
    private Boolean isSystem;

    @ToString.Exclude
    @OneToMany(mappedBy = "sysDict", orphanRemoval = true)
    private List<SysDictItem> items;

}
