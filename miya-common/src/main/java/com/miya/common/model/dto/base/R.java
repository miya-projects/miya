package com.miya.common.model.dto.base;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.io.Serializable;
import java.util.Objects;

/**
 * @author 杨超辉
 * http响应实体
 * 当用户未达到调用接口的目的时，success应该为false，业务逻辑的多种走向应该使用data里的信息承载
 * 带泛型的返回类，不能指定@Schema(name="")，否则springdoc只会生成一个泛型的schema
 */
@Getter
@Setter
@Schema(description = "统一的api返回格式")
@NoArgsConstructor
public class R<T> implements Serializable {
    /**
     * 响应码
     */
    @Schema(name = "code", description = "响应码", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    private Integer code;
    /**
     * 当code为0时success为true，方便判断
     */
    @Schema(name = "success", description = "当code为0时success为true，方便判断", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    private Boolean success;
    /**
     * 说明信息, 该信息应当可展示给最终用户查看
     */
    @Schema(name = "msg", description = "说明信息", requiredMode = Schema.RequiredMode.REQUIRED, example = "用户名或密码错误")
    private String msg;
    /**
     * 返回数据
     */
    @Schema(name = "data", description = "返回数据", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private T data;

    private R(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
        this.success = code == 0;
    }

    private R(T data, Integer code, String msg) {
        this.data = data;
        this.code = code;
        this.msg = msg;
        this.success = code == 0;
    }

    private static <T> R<T> errorWithCodeAndMsg(Integer code, String msg) {
        return new R<>(code, msg);
    }

    public static <T> R<T> successWithData(T data) {
        return new R<>(data, 0, "");
    }

    /**
     * 返回错误信息
     * @param code  错误码
     * @param args  错误信息构建参数 用该参数依次替换msg的{}
     */
    public static <T> R<T> errorWithCodeAndMsg(ResponseCode code, String... args) {
        String msg = code.getMsg();
        if (Objects.nonNull(args)){
            for (String arg : args) {
                arg = arg == null?"":arg;
                msg = msg.replaceFirst("\\{}", arg);
            }
        }
        return errorWithCodeAndMsg(code.getCode(), msg);
    }

    /**
     * @param msg
     */
    public static <T> R<T> errorWithMsg(String msg) {
        return new R<>(Integer.MIN_VALUE, msg);
    }

    public static <T> R<T> success() {
        return new R<>(0, "");
    }

}
