package com.miya.system.config.web;

import com.miya.common.config.xlsx.ToExcelFormat;

/**
 * 为枚举类型扩展一个可读的名字
 * {@link com.miya.system.module.common.DropDownListApi#queryEnum}
 */
public interface ReadableEnum extends ToExcelFormat {

    String getName();

    default String toStringForExcel() {
        return getName();
    }
}
