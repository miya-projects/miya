package com.miya.system.module.common.po;


import com.miya.common.module.base.BaseEntity;
import lombok.*;
import lombok.experimental.Accessors;

import javax.persistence.Entity;
import java.util.Date;

/**
 * 可持久化k-v存储
 */
@Getter
@Setter
@Entity
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class SysCache extends BaseEntity {
    private String sKey;
    private String sVal;
    private Date expireDate;

}
