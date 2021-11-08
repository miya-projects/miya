package com.miya.system.module.download;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class SimpleDownloadTask implements DownloadTask{

    private final String fileName;

    @Override
    public String getFileName() {
        return this.fileName;
    }

}
