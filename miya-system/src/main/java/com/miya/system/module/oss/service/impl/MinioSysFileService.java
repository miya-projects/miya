package com.miya.system.module.oss.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.StrUtil;
import com.miya.system.module.oss.OssConfigProperties;
import com.miya.system.module.oss.SysFileRepository;
import com.miya.system.module.oss.model.SysFile;
import com.miya.system.module.oss.service.SysFileService;
import io.minio.MinioClient;
import io.minio.ObjectStat;
import io.minio.PutObjectOptions;
import io.minio.errors.MinioException;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;

import static io.minio.PutObjectOptions.MAX_PART_SIZE;

/**
 * 杨超辉
 */
@Slf4j
@RequiredArgsConstructor
public class MinioSysFileService implements SysFileService, InitializingBean {

    private final SysFileRepository sysFileRepository;
    private final OssConfigProperties.Minio minio;


    private String bucketName;

    private MinioClient minioClient;

    @Override
    public void afterPropertiesSet() {
        try {
            Optional.ofNullable(minio.getBucketName()).ifPresent(bucketName -> this.bucketName = bucketName);
            minioClient = new MinioClient(minio.getEndpoint(), minio.getAccessKey(), minio.getSecretKey());
            boolean isExist = minioClient.bucketExists(bucketName);
            if (!isExist) {
                log.info("bucket {} not exits, try creating...", bucketName);
                minioClient.makeBucket(bucketName);
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
        PutObjectOptions putObjectOptions =
                new PutObjectOptions(file.length(), -1);
        if (!override && exist(objectName)) {
            return;
        }
        minioClient.putObject(bucketName, objectName, new FileInputStream(file), putObjectOptions);
    }


    /**
     * 获取对象信息
     * @param objectName    对象名
     */
    public ObjectStat objectStat(String objectName){
        try {
            return minioClient.statObject(bucketName, objectName);
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
        ObjectStat objectStat = null;
        try {
            objectStat = minioClient.statObject(bucketName, objectName);
        } catch (Exception e) {
            log.trace(ExceptionUtils.getStackTrace(e));
        }
        return Objects.nonNull(objectStat);
    }

    /**
     * 获取对象url(授权过)
     * @param objectName
     */
    @SneakyThrows({MinioException.class, IOException.class, NoSuchAlgorithmException.class, InvalidKeyException.class})
    public String presignedGetObject(String objectName) {
        return minioClient.presignedGetObject(bucketName, objectName);
    }

    /**
     * 获取对象url
     * @param objectName
     */
    @SneakyThrows({MinioException.class, IOException.class, NoSuchAlgorithmException.class, InvalidKeyException.class})
    public String getObjectUrl(String objectName) {
        return minioClient.getObjectUrl(bucketName, objectName);
    }

    /**
     * 获取对象url
     * duration必须在int类型存储长度范围内，超过将会被截断
     * @param objectName 对象名
     * @param duration   有效时长
     */
    @SneakyThrows({MinioException.class, IOException.class, NoSuchAlgorithmException.class, InvalidKeyException.class})
    public String getObjectUrl(String objectName, Duration duration) {
        return minioClient.presignedGetObject(bucketName, objectName, (int) duration.toMillis());
    }

    /**
     * 获取对象url
     * @param objectName
     * @param expireDate 过期时间 必须在int类型存储长度范围内，超过将会被截断
     */
    @SneakyThrows({MinioException.class, IOException.class, NoSuchAlgorithmException.class, InvalidKeyException.class})
    public String getObjectUrl(String objectName, Date expireDate) {
        Date now = new Date();
        int duration = 0;
        if (DateUtil.compare(expireDate, now) > 0) {
            duration = (int) DateUtil.betweenMs(now, expireDate);
        }
        return minioClient.presignedGetObject(bucketName, objectName, duration);
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
        PutObjectOptions putObjectOptions =
                new PutObjectOptions(file.getSize(), -1);
        // putObjectOptions.setContentType(readContentType());
        minioClient.putObject(bucketName, realFileName, file.getInputStream(), putObjectOptions);
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
        PutObjectOptions putObjectOptions = new PutObjectOptions(-1, MAX_PART_SIZE);
        putObjectOptions.setContentType(contentType);
        minioClient.putObject(bucketName, objectName, inputStream, putObjectOptions);
        ObjectStat objectStat = objectStat(objectName);
        SysFile sysFile = new SysFile();
        sysFile.setPath(objectName)
                .setFilename(fileName)
                .setSize(objectStat.length())
                .setSimpleSize(FileUtil.readableFileSize(objectStat.length()));
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
            minioClient.removeObject(bucketName, sysFile.getPath());
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
        return minioClient.getObject(bucketName, sysFile.getPath());
    }

}
