package com.miya.common.config;

/**
 * 为实现类型安全的包配置，需在包下新建一个标记类，在配置时指定那个类并继承该接口，
 * 继承该接口的目的是不重复说明这一段文字^v^，也方便查找各个扫描位置
 * 用于定义扫描包标记
 */
public interface ScanFlag {
}
