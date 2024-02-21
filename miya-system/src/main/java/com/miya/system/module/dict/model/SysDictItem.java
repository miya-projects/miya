package com.miya.system.module.dict.model;

import com.miya.common.module.base.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.hibernate.annotations.Comment;
import org.hibernate.envers.Audited;

/**
 * @author 杨超辉
 * 字典表
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@Entity
@Accessors(chain = true)
@Table(name = "sys_dict_item", indexes = {@Index(name = "dict_id_value", unique = true, columnList = "dict_id, value")})
@Audited
@Comment("字典数据")
public class SysDictItem extends BaseEntity {

    /**
     * 字典
     */
    @ManyToOne
    @JoinColumn(name = "dict_id")
    @Comment("所属字典id")
    private SysDict sysDict;

    /**
     * 字典键值
     */
    @Column(name = "`value`", length = 50, nullable = false)
    @Comment("字典键值")
    private String value;

    /**
     * 描述
     */
    @Column(length = 50, nullable = false)
    @Comment("描述")
    private String label;

}
