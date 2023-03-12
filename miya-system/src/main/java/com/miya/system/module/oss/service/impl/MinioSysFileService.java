package com.miya.system.module.oss.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.StrUtil;
import com.miya.system.module.oss.SysFileRepository;
import com.miya.system.module.oss.model.SysFile;
import com.miya.system.module.oss.service.SysFileService;
import io.minio.*;
import io.minio.errors.MinioException;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;

import static io.minio.ObjectWriteArgs.MAX_PART_SIZE;

/**
 * 杨超辉
 */
@Slf4j
@RequiredArgsConstructor
public class MinioSysFileService implements SysFileService, InitializingBean {

    private final SysFileRepository sysFileRepository;

    private final String bucketName;

    private final MinioClient minioClient;

    @Override
    public void afterPropertiesSet() {
        try {
            BucketExistsArgs bucketExistsArgs = BucketExistsArgs.builder()
                    .bucket(bucketName)
                    .build();
            boolean isExist = minioClient.bucketExists(bucketExistsArgs);
            if (!isExist) {
                log.info("bucket {} not exits, try creating...", bucketName);
                MakeBucketArgs makeBucketArgs = MakeBucketArgs.builder()
                        .bucket(bucketName)
                        .build();
                minioClient.makeBucket(makeBucketArgs);
                log.info("bucket {} not create success!", bucketName);
            }
        } catch (Exception e) {
            log.warn("minio客户端初始化失败");
            e.printStackTrace();
        }
    }

    /**
     * 上传单个文件
     * @param file  文件对象
     * @param objectName    oss对象名
     * @param override  是否覆盖已存在对象
     */
    @SneakyThrows({MinioException.class, IOException.class, NoSuchAlgorithmException.class, InvalidKeyException.class})
    public void upload(File file, String objectName, boolean override) {
        if (!override && exist(objectName)) {
            return;
        }
        PutObjectArgs putObjectArgs = PutObjectArgs.builder()
                .bucket(bucketName)
                .object(objectName)
                .stream(Files.newInputStream(file.toPath()), file.length(), MAX_PART_SIZE).build();
        minioClient.putObject(putObjectArgs);
    }


    /**
     * 获取对象信息
     * @param objectName    对象名
     */
    public StatObjectResponse objectStat(String objectName){
        try {
            StatObjectArgs statObjectArgs = StatObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build();
            return minioClient.statObject(statObjectArgs);
        } catch (Exception e) {
            log.trace(ExceptionUtils.getStackTrace(e));
        }
        return null;
    }

    /**
     * 返回指定对象名是否存在
     * @param objectName 对象名
     */
    private boolean exist(String objectName) {
        StatObjectResponse statObjectResponse = objectStat(objectName);
        return Objects.nonNull(statObjectResponse);
    }

    /**
     * 获取对象url(授权过)
     * @param objectName
     */
    @SneakyThrows({MinioException.class, IOException.class, NoSuchAlgorithmException.class, InvalidKeyException.class})
    public String getPresignedObjectUrl(String objectName) {
        return minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                .bucket(bucketName)
                .object(objectName)
                .build());
    }

    /**
     * 获取对象url
     * @param objectName
     */
    public String getObjectUrl(String objectName) {
        return getPresignedObjectUrl(objectName);
    }

    /**
     * 获取对象url
     * duration必须在int类型存储长度范围内，超过将会被截断
     * @param objectName 对象名
     * @param duration   有效时长
     */
    @SneakyThrows({MinioException.class, IOException.class, NoSuchAlgorithmException.class, InvalidKeyException.class})
    public String getObjectUrl(String objectName, Duration duration) {
        return minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                .bucket(bucketName)
                .object(objectName)
                .expiry((int)duration.toMillis())
                .build());
    }

    /**
     * 获取对象url
     * @param objectName
     * @param expireDate 过期时间 必须在int类型存储长度范围内，超过将会被截断
     */
    public String getObjectUrl(String objectName, Date expireDate) {
        Date now = new Date();
        int duration = 0;
        if (DateUtil.compare(expireDate, now) > 0) {
            duration = (int) DateUtil.betweenMs(now, expireDate);
        }
        return getObjectUrl(objectName, Duration.ofMillis(duration));
    }

    /**
     * 获取minioClient
     */
    public MinioClient getMinioClient() {
        return this.minioClient;
    }

    @Override
    @SneakyThrows({MinioException.class, IOException.class, NoSuchAlgorithmException.class, InvalidKeyException.class})
    public SysFile upload(MultipartFile file) {
        String fileName = file.getOriginalFilename();
        String realFileName = rename(fileName);
        PutObjectArgs putObjectArgs = PutObjectArgs.builder()
                .bucket(bucketName)
                .object(realFileName)
                .stream(file.getInputStream(), file.getSize(), MAX_PART_SIZE)
                .build();
        minioClient.putObject(putObjectArgs);
        SysFile sysFile = new SysFile();
        sysFile.setPath(realFileName)
                .setFilename(fileName)
                .setSize(file.getSize())
                .setSimpleSize(FileUtil.readableFileSize(file.getSize()));
        sysFileRepository.save(sysFile);
        return sysFile;
    }

    /**
     * 重命名文件名
     * @param fileName
     */
    private String rename(String fileName){
        return StrUtil.format("{}/{}.{}",
                DateUtil.today().replaceAll("-", "/"),
                UUID.fastUUID().toString(true), FileUtil.extName(fileName));
    }

    @Override
    @SneakyThrows({MinioException.class, IOException.class, NoSuchAlgorithmException.class, InvalidKeyException.class})
    public SysFile upload(String fileName, InputStream inputStream, String contentType) {
        String objectName = rename(fileName);
        PutObjectArgs putObjectArgs = PutObjectArgs.builder()
                .bucket(bucketName)
                .object(fileName)
                .contentType(contentType)
                .stream(inputStream, -1, MAX_PART_SIZE)
                .build();
        minioClient.putObject(putObjectArgs);
        StatObjectResponse statObjectResponse = objectStat(objectName);
        SysFile sysFile = new SysFile();
        sysFile.setPath(objectName)
                .setFilename(fileName)
                .setSize(statObjectResponse.size())
                .setSimpleSize(FileUtil.readableFileSize(statObjectResponse.size()));
        sysFileRepository.save(sysFile);
        return sysFile;
    }

    /**
     * 通过SysFile对象id删除文件，同时删除文件和文件数据
     * @param id    sysFile对象id
     */
    @Override
    @SneakyThrows({MinioException.class, IOException.class, NoSuchAlgorithmException.class, InvalidKeyException.class})
    public void deleteById(String id) {
        Optional<SysFile> sysFileOptional = sysFileRepository.findById(id);
        if (sysFileOptional.isPresent()) {
            SysFile sysFile = sysFileOptional.get();
            RemoveObjectArgs removeObjectArgs = RemoveObjectArgs.builder()
                    .bucket(bucketName)
                    .object(sysFile.getPath())
                    .build();
            minioClient.removeObject(removeObjectArgs);
            sysFileRepository.delete(sysFile);
        }
    }

    @Override
    public String getUrl(SysFile sysFile) {
        // todo 这里需改造为外部访问url
        return getObjectUrl(sysFile.getPath());
    }

    @Override
    @SneakyThrows({MinioException.class, IOException.class, InvalidKeyException.class, NoSuchAlgorithmException.class})
    public InputStream openStream(SysFile sysFile) {
        return minioClient.getObject(GetObjectArgs.builder()
                .bucket(bucketName)
                .object(sysFile.getPath())
                .build());
    }

}
