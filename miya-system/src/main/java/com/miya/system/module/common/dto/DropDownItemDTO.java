package com.miya.system.module.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.FieldNameConstants;

/**
 * 下拉框每一项的数据结构
 */
@Data
@FieldNameConstants
@AllArgsConstructor(staticName = "of")
public class DropDownItemDTO {
    private String value;
    private String label;
}
