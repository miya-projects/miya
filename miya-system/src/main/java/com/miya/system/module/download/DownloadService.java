package com.miya.system.module.download;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.thread.ExecutorBuilder;
import com.miya.system.module.oss.model.SysFile;
import com.miya.system.module.oss.service.SysFileService;
import com.miya.system.module.user.model.SysUser;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 下载服务
 * 1. 对导出日志做统一记录，
 * 2. 实现异步下载导出，解决实时导出慢体验差的问题
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DownloadService {

    private final SysFileService fileService;
    private final DownloadRecordRepository downloadRecordRepository;
    ThreadPoolExecutor poolExecutor = ExecutorBuilder.create().setMaxPoolSize(5).setCorePoolSize(3).build();

    /**
     * 生成任务记录
     */
    public void generateTask(String taskName, String fileName){
        SysDownloadRecord record = new SysDownloadRecord();
        record.setName(taskName);
        record.setFileName(fileName);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null){
            Object principal = authentication.getPrincipal();
            if (principal instanceof SysUser){
                record.setUser((SysUser) principal);
            }
        }
        downloadRecordRepository.save(record);
    }

    /**
     * 同步运行一个导出下载任务 顺便记录日志
     * @param task
     */
    public void execute(DownloadTask task) throws IOException {
        ServletRequestAttributes attributes = (ServletRequestAttributes)RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            throw new IllegalStateException("不可在非web环境中运行同步下载任务");
        }
        HttpServletResponse response = attributes.getResponse();
        if (response == null){
            throw new IllegalStateException("当前线程请求已失去");
        }
        ServletOutputStream outputStream = response.getOutputStream();
        InputStream inputStream = task.get();

        // todo 下载
        // response.setContentType("");


        IoUtil.copy(inputStream, outputStream);
        outputStream.close();
        inputStream.close();
    }

    /**
     * 异步运行一个导出下载任务
     * @param task
     */
    public void executeAsync(DownloadTask task){
        SysDownloadRecord record = new SysDownloadRecord();
        record.setName(task.getName());
        record.setFileName(task.getFileName());

        CompletableFuture<InputStream> future = CompletableFuture.supplyAsync(task, poolExecutor);
        future.thenAccept(stream -> {
            SysFile file = fileService.upload(task.getFileName(), stream);
            record.setFile(file);
            record.setStatus(SysDownloadRecord.Status.COMPLETED);
            downloadRecordRepository.save(record);
        });
    }


    /**
     * todo 没啥乱用
     * 获取文件url 可能为null
     * @param downloadRecord
     */
    public String getFileUrl(SysDownloadRecord downloadRecord) {
        downloadRecord.setStatus(SysDownloadRecord.Status.DOWNLOAD);
        downloadRecordRepository.save(downloadRecord);
        return Optional.ofNullable(downloadRecord.getFile()).map(SysFile::getUrl).orElse(null);
    }

    /**
     * 获取下载记录
     */
    public Page<SysDownloadRecordDTO> list(Predicate predicate, Pageable pageRequest) {
        QSysDownloadRecord qSysDownloadRecord = QSysDownloadRecord.sysDownloadRecord;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principal = Optional.ofNullable(authentication).map(Authentication::getPrincipal).orElse(null);
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
