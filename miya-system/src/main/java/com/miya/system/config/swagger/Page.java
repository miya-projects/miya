package com.miya.system.config.swagger;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import org.springdoc.core.annotations.ParameterObject;

import java.util.List;


@Getter
@Setter
@Schema(name = "分页参数")
@ParameterObject
public class Page {
    @Schema(description = "第page页,从0开始计数", example = "0")
    private Integer page;

    @Schema(description = "每页数据数量", example = "20")
    private Integer size;

    @Schema(description = "按属性排序,格式:属性,[asc|desc]")
    private List<String> sort;
}
