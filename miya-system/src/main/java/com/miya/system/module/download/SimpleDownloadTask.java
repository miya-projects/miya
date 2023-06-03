package com.miya.system.module.download;

import com.miya.system.module.user.model.SysUser;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.InputStream;
import java.util.function.Supplier;

@Getter
@RequiredArgsConstructor
public class SimpleDownloadTask implements DownloadTask {

    private final String name;
    private final String fileName;
    private final SysUser user;

    private final Supplier<InputStream> is;

    @Override
    public InputStream get() {
        return is.get();
    }
}
