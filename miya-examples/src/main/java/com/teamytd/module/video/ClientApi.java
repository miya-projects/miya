package com.teamytd.module.video;

import com.miya.common.annotation.Acl;
import com.miya.system.module.download.DownloadTask;
import com.miya.system.module.download.SimpleDownloadTask;
import com.miya.system.module.user.model.SysUser;
import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.InputStream;

//todo 测试后删除
@RequestMapping(value = "client")
@RestController
@Slf4j
@Api(tags = {"客户服务"})
@Acl(userType = SysUser.class)
@Validated
@RequiredArgsConstructor
public class ClientApi {

    public void df(){
        DownloadTask task = new SimpleDownloadTask("a.xls") {
            @Override
            public InputStream get() {
                return null;
            }

            @Override
            public String getName() {
                return null;
            }

        };
    }

}
