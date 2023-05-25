package com.miya.system.module.log;

import com.miya.common.annotation.Acl;
import com.miya.common.model.dto.base.Grid;
import com.miya.common.model.dto.base.R;
import com.miya.common.module.base.BaseApi;
import com.miya.common.module.base.BaseEntity;
import com.miya.system.module.user.model.SysUserPrincipal;
import com.querydsl.core.types.Predicate;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.querydsl.binding.QuerydslPredicate;
import org.springframework.data.web.PageableDefault;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 业务日志
 */
@RequestMapping("log")
@RestController
@Slf4j
@Tag(name = "日志")
@Acl(userType = SysUserPrincipal.class)
@Validated
@RequiredArgsConstructor
public class SysLogApi extends BaseApi {

    private final SysLogRepository sysLogRepository;

    /**
     * 日志列表
     */
    @Operation(summary = "日志列表")
    @GetMapping
    public R<?> list(
            @QuerydslPredicate(root = SysLog.class) Predicate predicate,
            @PageableDefault(sort = BaseEntity.Fields.createdTime, direction = Sort.Direction.DESC) Pageable pageRequest) {
        Page<SysLog> all = sysLogRepository.findAll(predicate, pageRequest);
        return R.successWithData(Grid.of(all));
    }
    /**
     * 日志详情
     */
    @Operation(summary = "日志详情")
    @GetMapping("{id}")
    public R<?> detail(@PathVariable("id") SysLog sysLog) {
        return R.successWithData(sysLog);
    }

}
