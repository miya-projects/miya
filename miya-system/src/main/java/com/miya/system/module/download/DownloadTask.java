package com.miya.system.module.download;

import java.io.InputStream;
import java.util.function.Supplier;

/**
 * 下载任务抽象
 */
public interface DownloadTask extends Supplier<InputStream> {

    /**
     * 标注下载的是什么
     */
    String getName();

    /**
     * 下载的文件名
     */
    String getFileName();

}
