package com.miya.system.module.oss.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import com.miya.system.module.oss.OssConfigProperties;
import com.miya.system.module.oss.SysFileRepository;
import com.miya.system.module.oss.model.SysFile;
import com.miya.system.module.oss.service.SysFileService;
import jakarta.servlet.MultipartConfigElement;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * 直接存储到服务器上
 */
@Slf4j
public class BareSysFileService implements SysFileService, WebMvcConfigurer {

    private final SysFileRepository sysFileRepository;
    private final OssConfigProperties.Bare bare;

    /**
     * 访问到系统的域名， eg: http://localhost:8080/
     */
    private final Supplier<String> backendDomain;

    /**
     * 文件存储的服务器目录，绝对路径
     */
    private final String uploadAbsolutePath;

    public BareSysFileService(SysFileRepository sysFileRepository, OssConfigProperties.Bare bare, Supplier<String> backendDomain) {
        this.sysFileRepository = sysFileRepository;
        this.bare = bare;
        this.backendDomain = backendDomain;

        this.uploadAbsolutePath = this.bare.getUploadAbsolutePath();
        if (!new File(this.uploadAbsolutePath).exists()){
            log.info("上传目录{}不存在，自动创建...", this.uploadAbsolutePath);
            FileUtil.mkdir(this.uploadAbsolutePath);
        }
    }


    @Bean
    MultipartConfigElement multipartConfigElement() {
        MultipartConfigFactory factory = new MultipartConfigFactory();
        //配置上传临时目录，默认的上传临时目录在系统临时目录下，时间长会被清理掉，导致上传失败
        File file = new File(this.uploadAbsolutePath, ".tmp/");
        if (!file.exists()) {
            boolean mkdirs = file.mkdirs();
            if (!mkdirs){
                log.warn("创建默认临时文件失败,请手工检查,{}", file.getAbsolutePath());
            }
        }
        factory.setLocation(file.getAbsolutePath());
        return factory.createMultipartConfig();
    }

    /**
     * 重命名文件名
     *
     * @param fileName
     * @return
     */
    private String rename(String fileName) {
        return StrUtil.format("{}/{}.{}",
                DateUtil.today().replaceAll("-", "/"),
                UUID.fastUUID().toString(true), FileUtil.extName(fileName));
    }

    @SneakyThrows
    @Override
    public SysFile upload(MultipartFile file) {
        String fileName = file.getOriginalFilename();
        String name = rename(fileName);
        File destFile = new File(uploadAbsolutePath, name);
        FileUtil.mkdir(destFile.getParentFile());
        //保存到数据库
        SysFile sysFile = new SysFile();
        sysFile.setFilename(fileName);
        sysFile.setPath(name);
        sysFile.setSize(file.getSize());
        sysFile.setSimpleSize(FileUtil.readableFileSize(file.getSize()));
        file.transferTo(destFile);
        sysFileRepository.save(sysFile);
        return sysFile;
    }

    @Override
    public SysFile upload(String fileName, InputStream inputStream) {
        return upload(fileName, inputStream, null);
    }

    @Override
    @SneakyThrows
    public SysFile upload(String fileName, InputStream inputStream, String contentType) {
        String name = rename(fileName);
        File destFile = new File(uploadAbsolutePath, name);
        FileUtil.mkdir(destFile.getParentFile());
        //保存到数据库
        SysFile sysFile = new SysFile();
        sysFile.setFilename(fileName);
        sysFile.setPath(name);
        long size = 0;
        try (
                InputStream i = inputStream;
                FileOutputStream fileOutputStream = new FileOutputStream(destFile)
        ) {
            size = IoUtil.copy(i, fileOutputStream);
        }
        sysFile.setSize(size);
        sysFile.setSimpleSize(FileUtil.readableFileSize(size));
        sysFile = sysFileRepository.save(sysFile);
        return sysFile;
    }

    @Override
    public void deleteById(String id) {
        Optional<SysFile> sysFileOptional = sysFileRepository.findById(id);
        if (sysFileOptional.isPresent()) {
            SysFile sysFile = sysFileOptional.get();
            boolean success = new File(uploadAbsolutePath + sysFile.getPath()).delete();
            if (!success){
                throw new RuntimeException("文件删除失败");
            }
            sysFileRepository.deleteById(id);
        }

    }

    @Override
    public String getUrl(SysFile sysFile) {
        try {
            String url = URLUtil.completeUrl(URLUtil.completeUrl(backendDomain.get(), "upload/"), FileUtil.subPath(uploadAbsolutePath, sysFile.getPath()));
            return new URL(url).toString();
        } catch (MalformedURLException e) {
            log.error(ExceptionUtils.getStackTrace(e));
            return backendDomain.get();
        }
    }

    @Override
    @SneakyThrows(IOException.class)
    public InputStream openStream(SysFile sysFile) {
        return Files.newInputStream(Paths.get(uploadAbsolutePath, sysFile.getPath()));
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String resourceLocation = uploadAbsolutePath;
        if (!resourceLocation.endsWith("/")) {
            resourceLocation = resourceLocation + "/";
        }
        registry.addResourceHandler(this.bare.getPathPatterns())
                .addResourceLocations("file:" + resourceLocation);
    }
}
