package com.miya.common.config.orm;

import com.miya.common.config.orm.annotations.NoDashesUuidGenerator;
import com.miya.common.module.base.BaseEntity;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.generator.BeforeExecutionGenerator;
import org.hibernate.generator.EventType;
import org.hibernate.generator.EventTypeSets;
import org.hibernate.generator.GeneratorCreationContext;
import org.hibernate.id.factory.spi.CustomIdGeneratorCreationContext;
import org.hibernate.id.uuid.CustomVersionOneStrategy;
import org.hibernate.type.descriptor.java.UUIDJavaType;
import java.lang.reflect.Member;
import java.util.EnumSet;

import static org.hibernate.generator.EventTypeSets.INSERT_ONLY;

/**
 * 实体类有id时，带id直接插入
 * HHH000409: Using com.miya.common.config.orm.ManualInsertGenerator which does not generate IETF RFC 4122 compliant UUID values; consider using org.hibernate.id.UUIDGenerator instead
 */
//UUIDGenerator
@Slf4j
public class NoDashesUUIDGenerator implements BeforeExecutionGenerator {


    //StandardRandomStrategy.INSTANCE
    private final CustomVersionOneStrategy generator = new CustomVersionOneStrategy();

    public NoDashesUUIDGenerator(
            NoDashesUuidGenerator config,
            Member idMember,
            CustomIdGeneratorCreationContext creationContext) {
        this(config, idMember);
    }

    public NoDashesUUIDGenerator(
            NoDashesUuidGenerator config,
            Member member,
            GeneratorCreationContext creationContext) {
        this(config, member);
    }

    private NoDashesUUIDGenerator(
            NoDashesUuidGenerator config,
            Member idMember) {
        //final Class<?> propertyType = ReflectHelper.getPropertyType( idMember );
    }

    /**
     * @return {@link EventTypeSets#INSERT_ONLY}
     */
    @Override
    public EnumSet<EventType> getEventTypes() {
        return INSERT_ONLY;
    }

    @Override
    public Object generate(SharedSessionContractImplementor session, Object owner, Object currentValue, EventType eventType) {
        // 如果手动指定了ID，则不再生成，使用手动指定的ID
        if (owner instanceof BaseEntity baseEntity && baseEntity.getId() != null) {
            return baseEntity.getId();
        }
        return UUIDJavaType.NoDashesStringTransformer.INSTANCE.transform( generator.generateUuid( session ) );
    }

}
