package com.miya.system.module.notice;

import com.miya.common.model.dto.base.Grid;
import com.miya.common.model.dto.base.R;
import com.miya.system.module.user.model.SysUser;
import com.miya.common.module.base.BaseEntity;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Predicate;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
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

import javax.annotation.Resource;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// todo
@RequestMapping("/notice")
@RestController
@Api(tags = {"通知"})
@Validated
public class SysNoticeApi {

    @Resource
    private SysNoticeService sysNoticeService;
    @Resource
    private SysNoticeRepository sysNoticeRepository;

    @PostMapping(value = "sendNotices")
    @ApiOperation(value = "发送通知")
    public R<?> sendNotices(@AuthenticationPrincipal SysUser sysUser,
                            @NotNull String[] userIds, @NotBlank String title,
                            @NotBlank String content, Map<String, Object> extra) {
        if (!sysUser.isAdmin()) {
            return R.errorWithMsg("您不是管理员，不可进行该操作");
        }
        sysNoticeService.sendNotices(Arrays.asList(userIds), title, content, extra);
        return R.success();
    }

    @PostMapping(value = "readNotices")
    @ApiOperation(value = "读通知")
    public R<?> readNotice(@AuthenticationPrincipal SysUser sysUser, @NotNull String[] noticeId) {
        sysNoticeService.readNotice(Arrays.asList(noticeId), sysUser);
        return R.success();
    }

    @PostMapping(value = "readAllNotice")
    @ApiOperation(value = "当前用户消息全部标记为已读")
    public R<?> readAllNotice(@AuthenticationPrincipal SysUser sysUser) {
        sysNoticeService.readAllNoticeByUser(sysUser.getId());
        return R.success();
    }

    @PostMapping(value = "list")
    @ApiOperation(value = "查询自己收到通知(分页)")
    public R<Grid<SysNoticeDTO>> list(@AuthenticationPrincipal SysUser sysUser, @QuerydslPredicate(root = SysNotice.class) Predicate predicate,
                                   @PageableDefault(sort = {"enable", BaseEntity.Fields.createdTime},
                                           direction = Sort.Direction.DESC) Pageable pageRequest) {
        Page<SysNotice> all = sysNoticeRepository.findAll(ExpressionUtils.and(QSysNotice.sysNotice.sysUser.any()
                .id.sysUser.eq(sysUser), predicate), pageRequest);
        return R.successWithData(Grid.of(all.map(SysNoticeDTO::of)));
    }


    @PostMapping(value = "top8")
    @ApiOperation(value = "查询最近8条通知")
    public R<List<SysNoticeDTO>> top8(@AuthenticationPrincipal SysUser sysUser) {
        List<SysNotice> sysNotices = sysNoticeService.top8(sysUser.getId());
        List<SysNoticeDTO> list = sysNotices.stream().map(notice -> {
            List<SysNoticeUser> noticeUsers = notice.getSysUser();
            SysNoticeUser noticeUser = noticeUsers.stream().filter(nu -> nu.getId().getSysUser().equals(sysUser)).findFirst().get();
            SysNoticeDTO sysNoticeDTO = SysNoticeDTO.of(notice);
            sysNoticeDTO.assign(noticeUser);
            return sysNoticeDTO;
        }).collect(Collectors.toList());
        return R.successWithData(list);
    }


}
