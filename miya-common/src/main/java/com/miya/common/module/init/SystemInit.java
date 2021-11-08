package com.miya.common.module.init;

/**
 * 各个模块载系统初始化时要干一些事情，比如建立表结构，用户模块初始化超级用户，系统配置模块初始化默认配置等。那么实现该接口吧!
 * 系统初始化操作应当检测是否初始化过
 */
public interface SystemInit {

    /**
     * 进行系统初始化操作
     * @throws SystemInitErrorException
     */
    void init() throws SystemInitErrorException;
}
