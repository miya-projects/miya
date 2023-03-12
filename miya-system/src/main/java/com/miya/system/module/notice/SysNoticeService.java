package com.miya.system.module.notice;

import com.google.common.collect.Lists;
import com.miya.system.module.user.SysUserRepository;
import com.miya.system.module.user.model.SysUser;
import com.miya.common.module.base.BaseEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 系统通知服务
 */
@Service
@Slf4j
public class SysNoticeService {

    @Resource
    private SysNoticeRepository sysNoticeRepository;
    @Resource
    private SysNoticeUserRepository sysNoticeUserRepository;
    @Resource
    private SysUserRepository sysUserRepository;

    /**
     * 未读消息数量
     *
     * @param userId 用户
     */
    public Long unreadNoticeAmount(String userId) {
        QSysNotice qSysNotice = QSysNotice.sysNotice;
        QSysNoticeUser qSysNoticeUser = QSysNoticeUser.sysNoticeUser;
        return sysNoticeUserRepository.count(qSysNoticeUser.id.sysUser.id.eq(userId).and(
                qSysNoticeUser.read.isFalse()
        ).and(qSysNoticeUser.id.sysNotice.enable.isTrue()));
    }

    /**
     * 设置通知为已读
     *
     * @param noticeIds 读过的通知
     * @param user
     */
    public void readNotice(List<String> noticeIds, SysUser user) {
        for (String noticeId : noticeIds) {
            SysNoticeUser sysNoticeUser = new SysNoticeUser();
            SysNoticeUser.SysNoticeUserId sysNoticeUserId = new SysNoticeUser.SysNoticeUserId();
            sysNoticeUserId.setSysUser(user);
            sysNoticeUserId.setSysNotice(sysNoticeRepository.getReferenceById(noticeId));
            sysNoticeUser.setId(sysNoticeUserId);
            sysNoticeUser.setRead(true);
            sysNoticeUserRepository.save(sysNoticeUser);
        }
    }

    /**
     * 当前用户消息全部标记为已读
     *
     * @param userId 用户
     */
    public void readAllNoticeByUser(String userId) {
        sysNoticeUserRepository.updateReadByUser(userId);
    }


    /**
     * 获取前8条消息 时间倒序
     *
     * @param userId 当前登录用户id
     * @return 数据
     */
    public List<SysNotice> top8(String userId) {
        QSysNotice qSysNotice = QSysNotice.sysNotice;
        QSysNoticeUser any = qSysNotice.sysUser.any();
        PageRequest pr = PageRequest.of(0, 8, Sort.by(Sort.Direction.DESC, BaseEntity.Fields.createdTime));
        Iterable<SysNotice> all = sysNoticeRepository.findAll(any.id.sysUser.id.eq(userId), pr);
        return Lists.newArrayList(all);
    }


    /**
     * 批量发送系统通知
     *
     * @param userIds
     * @param title
     * @param content
     * @param extra
     */
    public void sendNotices(List<String> userIds, String title, String content, Map<String, Object> extra) {
        SysNotice.SysNoticeBuilder builder = SysNotice.builder();
        builder.title(title)
                .content(content)
                .extra(extra);
        SysNotice sysNotice = builder.build();
        List<SysNoticeUser> list = new ArrayList<>();
        userIds.forEach(id -> {
            SysNoticeUser sysNoticeUser = new SysNoticeUser();
            SysNoticeUser.SysNoticeUserId sysNoticeUserId = new SysNoticeUser.SysNoticeUserId();
            sysNoticeUserId.setSysUser(sysUserRepository.getReferenceById(id));
            sysNoticeUserId.setSysNotice(sysNotice);
            sysNoticeUser.setId(sysNoticeUserId);
            list.add(sysNoticeUser);
        });
        sysNotice.setSysUser(list);
        sysNoticeRepository.save(sysNotice);
    }

}
