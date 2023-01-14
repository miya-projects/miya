package com.miya.system.module.oss.model;

import com.miya.common.module.base.Convertable;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
public class SysFileDTO extends Convertable {

    private String id;

    private String name;

    private String url;

    public static SysFileDTO of(SysFile file) {
        return modelMapper.map(file, SysFileDTO.class);
    }

}

