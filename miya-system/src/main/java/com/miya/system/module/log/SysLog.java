package com.miya.system.module.log;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.miya.common.module.base.BaseEntity;
import lombok.*;
import lombok.experimental.Accessors;
import org.hibernate.Hibernate;
import org.hibernate.annotations.Type;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.FullTextField;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.Indexed;
import javax.persistence.Entity;
import java.util.Map;

/**
 * 业务日志表
 * 某条数据的操作日志，要求日志有一个业务数据主键id
 **/
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@JsonInclude(value = JsonInclude.Include.NON_NULL)
@Entity
@Accessors(chain = true)
@Indexed
public class SysLog extends BaseEntity {

    /**
     * 日志操作人
     */
    @FullTextField
    private String operatorName;

    /**
     * 所属模块
     */
    @FullTextField
    private String business;

    /**
     * 操作类型
     * 对同一业务模块同一领域模型的操作可能有很多，
     * 例如对订单的操作可以有: 取消、申请退款、付款、删除等，这样对日志分类也能引导程序员从这方面去思考
     */
    @FullTextField
    private String operationType;
    /**
     * 详细日志内容
     */
    @FullTextField
    private String content;

    /**
     * 额外参数
     */
    @Type(type = "json")
    // @FullTextField
    private Map<String, Object> extra;

    /**
     * 业务数据id
     */
    private String businessId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        SysLog sysLog = (SysLog) o;

        return id != null && id.equals(sysLog.id);
    }

    @Override
    public int hashCode() {
        return 893433846;
    }
}
