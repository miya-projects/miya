package com.miya.common.config.orm;

import com.miya.common.config.orm.annotations.NoDashesUuidGenerator;
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
        return UUIDJavaType.NoDashesStringTransformer.INSTANCE.transform( generator.generateUuid( session ) );
    }

    //@Override
    //public Object generate(SharedSessionContractImplementor session, Object obj) {
    //    Object id = session.getEntityPersister(null, obj).getClassMetadata().getIdentifier(obj, session);
    //    if (Objects.nonNull(id) && StringUtils.isNotBlank(id.toString())){
    //        return id;
    //    }
    //    return super.generate(session, obj);
    //}
}
