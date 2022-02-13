package com.miya.system.module.dict.model;

import com.miya.common.module.base.BaseEntity;
import lombok.*;
import lombok.experimental.Accessors;

import javax.persistence.*;
import java.util.List;

/**
 * @author 杨超辉
 *  字典表
 */
@Getter
@Setter
@ToString
@Entity
@Accessors(chain = true)
@Table(indexes = {@Index(name = "code_unique", columnList = "code", unique = true)})
public class SysDict extends BaseEntity {

    /**
     * 字典名称
     */
    @Column(length = 30)
    private String name;

    /**
     * 业务代码
     */
    @Column(length = 30, unique = true)
    private String code;
    /**
     * 是否为系统字典
     */
    private Boolean isSystem;

    @ToString.Exclude
    // @Setter(AccessLevel.NONE)
    // @Getter(AccessLevel.NONE)
    @OneToMany(mappedBy = "sysDict", orphanRemoval = true)
    private List<SysDictItem> items;

}
