package com.miya.common.module.base;

import lombok.Getter;
import lombok.Setter;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * DTO基类
 * @author 杨超辉
 */
@Getter
@Setter
public abstract class BaseDTO extends Convertable implements Serializable {

    protected String id;

    protected BaseDTO() {
    }

    /**
     * 创建时间戳 (单位:秒)
     */
    protected LocalDateTime createdTime;

    /**
     * 更新时间戳 (单位:秒)
     */
    protected LocalDateTime updatedTime;

}
