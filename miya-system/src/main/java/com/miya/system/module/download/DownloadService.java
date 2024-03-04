package com.miya.system.module.download;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.thread.ExecutorBuilder;
import cn.hutool.http.ContentType;
import cn.hutool.json.JSONUtil;
import com.miya.common.exception.ErrorMsgException;
import com.miya.common.model.dto.base.R;
import com.miya.common.module.config.SysConfigService;
import com.miya.common.module.config.SystemConfigKeys;
import com.miya.common.util.AuthenticationUtil;
import com.miya.common.util.TransactionUtil;
import com.miya.system.module.oss.model.SysFile;
import com.miya.system.module.oss.service.SysFileService;
import com.miya.system.module.user.model.SysUser;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 下载服务
 * 1. 对导出日志做统一记录，
 * 2. 实现异步下载导出，解决实时导出慢体验差的问题
 * 3. 导出任务队列，防止用户同时多次导出大数据量的任务，导致服务器内存爆炸
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DownloadService {

    private final SysFileService fileService;
    private final DownloadRecordRepository downloadRecordRepository;
    private final SysConfigService configService;

    /**
     * 导出线程池
     * 限制同时进行的任务数，如导出数量超级巨大，内存放不下单个任务时，另外处理。
     */
    ThreadPoolExecutor poolExecutor = ExecutorBuilder.create().setMaxPoolSize(5).setCorePoolSize(2).build();

    /**
     * 生成任务记录
     */
    public void generateTask(String taskName, String fileName){
        SysDownloadRecord record = new SysDownloadRecord();
        record.setName(taskName);
        record.setFileName(fileName);
        Object principal = AuthenticationUtil.getPrincipal();
        if (principal instanceof SysUser){
            record.setUser((SysUser) principal);
        }
        downloadRecordRepository.save(record);
    }

    /**
     * 运行一个导出下载任务
     * @param task
     */
    @Transactional
    public void export(DownloadTask task) {
        String exportWay = configService.getValOrDefaultVal(SystemConfigKeys.EXPORT_WAY);
        if (exportWay.equalsIgnoreCase("async")){
            executeAsync(task);
        }else if (exportWay.equalsIgnoreCase("sync")) {
            execute(task);
        }else {
            throw new ErrorMsgException("未正确配置导出方式，请先配置EXPORT_WAY后再进行导出");
        }
    }

    /**
     * 同步运行一个导出下载任务 顺便记录日志
     * @param task
     */
    @SneakyThrows(IOException.class)
    public void execute(DownloadTask task) {
        ServletRequestAttributes attributes = (ServletRequestAttributes)RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            throw new IllegalStateException("不可在非web环境中运行同步下载任务");
        }
        HttpServletResponse response = attributes.getResponse();
        if (response == null){
            throw new IllegalStateException("当前线程请求已失去");
        }
        final SysDownloadRecord record = TransactionUtil.INSTANCE.transactional(() -> newDownloadRecord(task));
        log.info("[{}]开始导出", task.getName());
        SysFile file = fileService.upload(task.getFileName(), task.get());
        TransactionUtil.INSTANCE.transactional(() -> {
            record.setFile(file);
            record.setStatus(SysDownloadRecord.Status.COMPLETED);
            record.setCompletedTime(new Date());
            downloadRecordRepository.save(record);
        });
        log.info("[{}]导出完毕", task.getName());
        response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(task.getFileName(), StandardCharsets.UTF_8));
        response.setContentType("application/octet-stream");
        IoUtil.copy(fileService.openStream(file), response.getOutputStream());
    }

    /**
     * 异步运行一个导出下载任务
     * @param task
     */
    @SneakyThrows(IOException.class)
    public void executeAsync(DownloadTask task) {
        // 保存po必须单独一个事务，以便在下面异步执行更新前提交事务
        final SysDownloadRecord record = TransactionUtil.INSTANCE.transactional(() -> newDownloadRecord(task));
        ServletRequestAttributes attributes = (ServletRequestAttributes)RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            throw new IllegalStateException("当前线程请求已失去");
        }
        HttpServletResponse response = attributes.getResponse();
        if (response == null){
            throw new IllegalStateException("当前线程请求已失去");
        }
        response.setHeader(HttpHeaders.CONTENT_TYPE, ContentType.JSON.toString());
        response.getWriter().write(JSONUtil.toJsonStr(R.success()));
        log.info("[{}]开始导出", task.getName());
        CompletableFuture<InputStream> future = CompletableFuture.supplyAsync(() -> {
            record.setStatus(SysDownloadRecord.Status.PROCESSING);
            downloadRecordRepository.save(record);
            return task.get();
        }, poolExecutor);
        future.thenAccept(stream -> {
            SysFile file = fileService.upload(task.getFileName(), stream);
            record.setFile(file);
            record.setStatus(SysDownloadRecord.Status.COMPLETED);
            record.setCompletedTime(new Date());
            downloadRecordRepository.save(record);
            log.info("[{}]导出完毕", task.getName());
        }).exceptionally(e -> {
            log.error("[{}]导出失败",task.getName(), e);
            record.setStatus(SysDownloadRecord.Status.FAILED);
            downloadRecordRepository.save(record);
            return null;
        });
    }

    /**
     * 创建新的下载记录
     * @param task
     */
    public SysDownloadRecord newDownloadRecord(DownloadTask task) {
        SysDownloadRecord record = new SysDownloadRecord();
        record.setName(task.getName());
        record.setFileName(task.getFileName());
        record.setUser(task.getUser());
        record = downloadRecordRepository.saveAndFlush(record);
        return record;
    }


    /**
     * 获取文件url 可能为null
     * @param downloadRecord
     */
    public String getFileUrl(SysDownloadRecord downloadRecord) {
        List<SysDownloadRecord.Status> statuses = Arrays.asList(SysDownloadRecord.Status.COMPLETED, SysDownloadRecord.Status.DOWNLOAD);
        if (!statuses.contains(downloadRecord.getStatus())) {
            throw new ErrorMsgException("当前导出任务未完成或导出任务失败");
        }
        downloadRecord.setStatus(SysDownloadRecord.Status.DOWNLOAD);
        downloadRecordRepository.save(downloadRecord);
        return Optional.ofNullable(downloadRecord.getFile()).map(SysFile::getUrl).orElse(null);
    }

    /**
     * 获取下载记录
     */
    public Page<SysDownloadRecordDTO> list(Predicate predicate, Pageable pageRequest) {
        QSysDownloadRecord qSysDownloadRecord = QSysDownloadRecord.sysDownloadRecord;
        Object principal = AuthenticationUtil.getPrincipal();
        BooleanBuilder bb = new BooleanBuilder();
        bb.and(predicate);
        if (principal instanceof SysUser) {
            SysUser user = (SysUser) principal;
            if (!user.isAdmin()) {
                bb.and(qSysDownloadRecord.user.eq(user));
            }
        }
        Page<SysDownloadRecord> page = downloadRecordRepository.findAll(bb, pageRequest);
        return page.map(SysDownloadRecordDTO::of);
    }

}
