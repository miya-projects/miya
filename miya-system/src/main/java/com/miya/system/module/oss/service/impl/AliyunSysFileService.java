package com.miya.system.module.oss.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.StrUtil;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.model.CannedAccessControlList;
import com.aliyun.oss.model.CreateBucketRequest;
import com.aliyun.oss.model.OSSObject;
import com.aliyun.oss.model.ObjectMetadata;
import com.miya.system.module.oss.OssConfigProperties;
import com.miya.system.module.oss.SysFileRepository;
import com.miya.system.module.oss.model.SysFile;
import com.miya.system.module.oss.service.SysFileService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

/**
 * aliyun oss实现
 */
@Slf4j
@RequiredArgsConstructor
public class AliyunSysFileService implements SysFileService, InitializingBean {

    private final SysFileRepository sysFileRepository;
    private final OssConfigProperties.Aliyun aliyun;

    private String bucketName;
    private OSS ossClient;

    @Override
    public void afterPropertiesSet() {
        bucketName = aliyun.getBucketName();
        ossClient = new OSSClientBuilder().build(aliyun.getEndpoint(), aliyun.getAccessKey(), aliyun.getSecretKey());
        if (!ossClient.doesBucketExist(bucketName)) {
            log.info("bucket {}不存在, 创建Bucket：", bucketName);
            CreateBucketRequest createBucketRequest = new CreateBucketRequest(bucketName);
            createBucketRequest.setCannedACL(CannedAccessControlList.PublicRead);
            ossClient.createBucket(createBucketRequest);
        }
    }

    /**
     * 重命名文件名
     * @param fileName
     * @return
     */
    private String rename(String fileName){
        return StrUtil.format("{}/{}.{}",
                DateUtil.today().replaceAll("-", "/"),
                UUID.fastUUID().toString(true), FileUtil.extName(fileName));
    }

    @Override
    public SysFile upload(String fileName, InputStream inputStream, String contentType) {
        String objectName = rename(fileName);
        ObjectMetadata objectMetadata1 = new ObjectMetadata();
        objectMetadata1.setContentType(contentType);
        ossClient.putObject(bucketName, objectName, inputStream, objectMetadata1);
        OSSObject ossObject = ossClient.getObject(bucketName, objectName);
        ObjectMetadata objectMetadata = ossObject.getObjectMetadata();
        SysFile sysFile = new SysFile();
        sysFile.setPath(objectName)
                .setFilename(fileName)
                .setSize(objectMetadata.getContentLength())
                .setSimpleSize(FileUtil.readableFileSize(objectMetadata.getContentLength()));
        sysFileRepository.save(sysFile);
        return sysFile;
    }

    @Override
    @SneakyThrows(IOException.class)
    public SysFile upload(MultipartFile file) {
        return upload(file.getOriginalFilename(), file.getInputStream(), "");
    }

    @Override
    public void deleteById(String id) {
        Optional<SysFile> sysFileOptional = sysFileRepository.findById(id);
        if (sysFileOptional.isPresent()) {
            SysFile sysFile = sysFileOptional.get();
            ossClient.deleteObject(bucketName, sysFile.getPath());
            sysFileRepository.delete(sysFile);
        }
    }

    @Override
    public String getUrl(SysFile sysFile) {
        try {
            OSSObject object = ossClient.getObject(bucketName, sysFile.getPath());
            return object.getResponse().getUri();
        }catch (OSSException e){
            // todo 考虑要不要提到接口
            return NOT_FOUND.getUrl();
        }
    }

    @Override
    public InputStream openStream(SysFile sysFile) {
        OSSObject object = ossClient.getObject(bucketName, sysFile.getPath());
        return object.getObjectContent();
    }

}
