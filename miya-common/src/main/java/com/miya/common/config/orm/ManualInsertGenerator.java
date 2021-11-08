package com.miya.common.config.orm;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.UUIDHexGenerator;

import java.io.Serializable;
import java.util.Objects;

/**
 * 实体类有id时，带id直接插入
 */
public class ManualInsertGenerator extends UUIDHexGenerator {

    @Override
    public Serializable generate(SharedSessionContractImplementor session, Object obj) {
        Serializable id = session.getEntityPersister(null, obj).getClassMetadata().getIdentifier(obj, session);
        if (Objects.nonNull(id) && StringUtils.isNotBlank(id.toString())){
            return id;
        }
        return super.generate(session, obj);
    }
}
