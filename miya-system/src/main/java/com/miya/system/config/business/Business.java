package com.miya.system.config.business;


import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Objects;

/**
 * 功能抽象
 * 什么是一个功能?
 * 功能是前端界面+后端接口的任意组合。具体表现形式为一个code，后端接口在后端代码中和code对应，前端资源也在前端代码中和code对应、
 */
@Getter
@Setter
public class Business {

    private String name;
    private String code;
    @JsonIgnore
    private Business parent;
    private List<Business> children;

    public String getFullCode(){
        if (this.parent == null) {
            return this.code;
        }
        return this.parent.getFullCode() + ":" + this.code;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Business business = (Business) o;
        return StrUtil.equals(this.getFullCode(), business.getFullCode());
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getFullCode());
    }
}
