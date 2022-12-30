package com.miya.common.module.base;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.collection.spi.PersistentCollection;
import org.modelmapper.Conditions;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import java.io.Serializable;
import java.util.Date;

/**
 * DTO基类
 * @author 杨超辉
 */
@Getter
@Setter
public abstract class BaseDTO implements Serializable {

    protected String id;

    protected BaseDTO() {
    }

    /**
     * 创建时间戳 (单位:秒)
     */
    protected Date createdTime;

    /**
     * 更新时间戳 (单位:秒)
     */
    protected Date updatedTime;

    /**
     * 创建人
     */
    // protected String createdUser;

    protected static ModelMapper modelMapper = new ModelMapper();


    static {
        modelMapper.getConfiguration().setFullTypeMatchingRequired(true);
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);

        modelMapper.getConfiguration().setAmbiguityIgnored(true);
        modelMapper.getConfiguration().setSkipNullEnabled(true);
        modelMapper.getConfiguration().setPropertyCondition(Conditions.isNotNull());
        modelMapper.getConfiguration().setPreferNestedProperties(false);

        // modelMapper.getConfiguration()
        //         .setPropertyCondition(context ->
        //                 !(context.getSource() instanceof PersistentCollection));

        // PropertyMap<SysUser, SysUserListDTO> propertyMap = new PropertyMap<SysUser, SysUserListDTO>() {
        //     @Override
        //     protected void configure() {
        //        // 这里的代码会被生成代理类，有一些限制，如不可使用final类等, 使用TypeMap就没有这样的限制
        //        //  map().setAvatar(source.getAvatar().getUrl());
        //     }
        // };
        // modelMapper.addMappings(propertyMap);
    }

}
