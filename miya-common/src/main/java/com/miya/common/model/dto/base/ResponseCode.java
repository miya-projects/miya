package com.miya.common.model.dto.base;

import lombok.Getter;

/**
 * 支持msg{}构建 @link R.errorWithCodeAndMsg(ResponseCode code, String... args)
 * msg内的{}，将会在构建R时使用 args参数替换
 *
 * tips:
 * 返回此代码的意义是为前端提供固定不变的code用于判断并处理系统发生的各种情况
 * 为前端提供msg用于理解发生各种情况的原因，谨慎使用 服务端错误、登录失败、修改失败、删除失败 XX失败等让人摸不着头脑的msg
 * 无需处理对预期外的情况返回某个错误码，全局错误处理器已经处理
 * 好的例子： 用户名或密码错误、该数据为保护状态，不可修改，请联系管理员、 该分类下有产品不可删除，请先删除分类下产品
 * 如果不能提供解决错误的办法，请至少提供错误发生的原因
 *
 * 注意:
 * 1. 使用this.ordinal()方法构造的code依赖于枚举字段的顺序，所以在更改枚举顺序之前请确保不会影响到使用该code的地方
 * 2. msg的长度必须大于4
 * 3. 不应重复
 */
public interface ResponseCode {

    /**
     * 获得提示信息
     * @return
     */
    String getMsg();

    /**
     * 获得code
     * @return
     */
    int getCode();

    /**
     * 通用代码
     * 1 到 200
     */
    @Getter
    enum Common implements ResponseCode {
        NO_LOGIN("用户没有登录或登录态已过期"),
        TOKEN_SIGNATURE_FAIL("签名错误"),
        LOGIN_FAILED("登录失败: {}"),
        VERIFICATION_ERROR("验证码错误"),
        VERIFICATION_EXPIRE("验证码过期"),
        NULL_PARAMS("请检查是否参数是否为空"),
        ID_CANNOT_BE_EMPTY("参数id不可为空"),
        OBJECT_NOT_EXIST("{}不存在"),
        NO_PERMISSION("无权限访问"),
        NOT_ALLOW_UPLOAD("该文件不允许上传"),
        OLD_PASSWORD_IS_NOT_VALID("旧密码不正确"),
        PARAMETER_IS_REQUIRED("{}参数是必须的"),
        FILE_TOO_BIG("上传文件超出最大限制"),
        NOT_ADMIN("此内容只允许超级管理员操作"),
        DUPLICATE("{}重复"),
        CANNOT_DELETE("不可删除"),
        UPLOAD_TOO_MANY_FILES("上传文件数量超出限制"),
        FILE_IS_NOT_IMAGE("上传文件非图像文件"),
        VISIT_TOO_FAST("您访问太快了"),
        PATH_NOT_EXIST("路由{}不存在"),
        CAN_NOT_OPERATE_SUPER_ADMIN("该用户为超级管理员,不可进行该操作");

        private final int code;
        private final String msg;

        Common(String msg){
            this.msg = msg;
            this.code = 1 + this.ordinal();
        }
    }

}
