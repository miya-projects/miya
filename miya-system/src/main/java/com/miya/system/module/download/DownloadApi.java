package com.miya.system.module.download;

import com.miya.common.annotation.Acl;
import com.miya.common.model.dto.base.Grid;
import com.miya.common.model.dto.base.R;
import com.miya.common.module.base.BaseEntity;
import com.miya.system.module.user.model.SysUserPrincipal;
import com.querydsl.core.types.Predicate;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.querydsl.binding.QuerydslPredicate;
import org.springframework.data.web.PageableDefault;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


@Slf4j
@RestController
@Tag(name = "下载中心")
@Validated
@RequiredArgsConstructor
@Acl(userType = SysUserPrincipal.class)
@RequestMapping("download")
public class DownloadApi {

    private final DownloadService downloadService;

    @Operation(summary = "获取文件url")
    @GetMapping("{id}/url")
    public R<?> getFileUrl(@PathVariable("id") SysDownloadRecord downloadRecord){
        return R.successWithData(downloadService.getFileUrl(downloadRecord));
    }

    @Operation(summary = "获取某下载任务状态")
    @GetMapping("{id}/status")
    public R<?> getStatus(@PathVariable("id") SysDownloadRecord downloadRecord){
        return R.successWithData(downloadRecord.getStatus());
    }

    @Operation(summary = "下载任务列表")
    @GetMapping
    public R<?> list(@QuerydslPredicate(root = SysDownloadRecord.class) Predicate predicate,
                     @PageableDefault(sort = BaseEntity.Fields.createdTime, direction = Sort.Direction.DESC) Pageable pageRequest){
        return R.successWithData(Grid.of(downloadService.list(predicate, pageRequest)));
    }

    @Operation(summary = "创建一个下载(测试)")
    @PostMapping
    public R<?> createDownload(){
        // downloadService.execute(downloadTask);
        return R.success();
    }

}
