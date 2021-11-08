package com.miya.system.module.dict.model;

import com.miya.common.module.base.BaseEntity;
import com.miya.common.service.mapper.BaseMapper;
import lombok.*;
import lombok.experimental.Accessors;
import org.mapstruct.Mapper;
import javax.persistence.*;

/**
 * @author 杨超辉
 * @date 2018/12/18
 * @description 字典表
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@Entity
@Accessors(chain = true)
@Table(name = "sys_dict_item")
public class SysDictItem extends BaseEntity {

    /**
     * 字典
     */
    @ManyToOne
    @JoinColumn(name = "dict_id")
    private SysDict sysDict;
    /**
     * 字典键值
     */
    @Column(name = "`value`")
    private String value;

    /**
     * 描述
     */
    private String label;

}
