package com.miya.common.module.base;

import org.modelmapper.Conditions;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;

public class Convertable {
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

        // TypeMap<SysUser, SysUserListDTO> typeMap = modelMapper.typeMap(SysUser.class, SysUserListDTO.class);
        // typeMap.addMapping(user -> {
        //             return Optional.ofNullable(user.getAvatar()).map(SysFile::getUrl).orElse(null);
        //         },
        // SysUserListDTO::setAvatar);
    }
}
