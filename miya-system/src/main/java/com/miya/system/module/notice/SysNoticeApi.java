package com.miya.system.module.notice;

import com.miya.common.model.dto.base.Grid;
import com.miya.common.model.dto.base.R;
import com.miya.common.module.base.BaseEntity;
import com.miya.system.module.user.model.SysUserPrincipal;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Predicate;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.querydsl.binding.QuerydslPredicate;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// todo
@RequestMapping("/notice")
@RestController
@Tag(name = "通知")
@Validated
public class SysNoticeApi {

    @Resource
    private SysNoticeService sysNoticeService;
    @Resource
    private SysNoticeRepository sysNoticeRepository;

    @PostMapping(value = "sendNotices")
    @Operation(summary = "发送通知")
    public R<?> sendNotices(@AuthenticationPrincipal SysUserPrincipal sysUser,
                            @NotNull String[] userIds, @NotBlank String title,
                            @NotBlank String content, Map<String, Object> extra) {
        if (!sysUser.isAdmin()) {
            return R.errorWithMsg("您不是管理员，不可进行该操作");
        }
        sysNoticeService.sendNotices(Arrays.asList(userIds), title, content, extra);
        return R.success();
    }

    @PostMapping(value = "readNotices")
    @Operation(summary = "读通知")
    public R<?> readNotice(@AuthenticationPrincipal SysUserPrincipal sysUserPrincipal, @NotNull String[] noticeId) {
        sysNoticeService.readNotice(Arrays.asList(noticeId), sysUserPrincipal.toPO());
        return R.success();
    }

    @PostMapping(value = "readAllNotice")
    @Operation(summary = "当前用户消息全部标记为已读")
    public R<?> readAllNotice(@AuthenticationPrincipal SysUserPrincipal sysUser) {
        sysNoticeService.readAllNoticeByUser(sysUser.getId());
        return R.success();
    }

    @PostMapping(value = "list")
    @Operation(summary = "查询自己收到通知(分页)")
    public R<Grid<SysNoticeDTO>> list(@AuthenticationPrincipal SysUserPrincipal sysUser, @QuerydslPredicate(root = SysNotice.class) Predicate predicate,
                                   @PageableDefault(sort = {"enable", BaseEntity.Fields.createdTime},
                                           direction = Sort.Direction.DESC) Pageable pageRequest) {
        Page<SysNotice> all = sysNoticeRepository.findAll(ExpressionUtils.and(QSysNotice.sysNotice.sysUser.any()
                .id.sysUser.eq(sysUser.toPO()), predicate), pageRequest);
        return R.successWithData(Grid.of(all.map(SysNoticeDTO::of)));
    }


    @PostMapping(value = "top8")
    @Operation(summary = "查询最近8条通知")
    public R<List<SysNoticeDTO>> top8(@AuthenticationPrincipal SysUserPrincipal sysUserPrincipal) {
        List<SysNotice> sysNotices = sysNoticeService.top8(sysUserPrincipal.getId());
        List<SysNoticeDTO> list = sysNotices.stream().map(SysNoticeDTO::of).collect(Collectors.toList());
        return R.successWithData(list);
    }


}
