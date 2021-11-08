package com.miya.system.module.download;

import com.miya.common.annotation.Acl;
import com.miya.common.model.dto.base.R;
import com.miya.system.module.user.model.SysUser;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@Api(tags = {"下载中心(TODO)"})
@Validated
@RequiredArgsConstructor
@Acl(userType = SysUser.class)
@RequestMapping("download")
public class DownloadApi {

    private final DownloadService downloadService;

    @ApiOperation("获取文件url")
    @GetMapping("{id}/url")
    public R<?> getFileUrl(@PathVariable("id") SysDownloadRecord downloadRecord){
        return R.successWithData(downloadService.getFileUrl(downloadRecord));
    }

    @ApiOperation("获取某下载任务状态")
    @GetMapping("{id}/status")
    public R<?> getStatus(@PathVariable("id") SysDownloadRecord downloadRecord){
        return R.successWithData(downloadRecord.getStatus());
    }
}
