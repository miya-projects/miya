package com.miya.system.module.oss;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.miya.common.annotation.RequestLimit;
import com.miya.common.exception.ErrorMsgException;
import com.miya.common.exception.ResponseCodeException;
import com.miya.common.model.dto.base.R;
import com.miya.common.model.dto.base.ResponseCode;
import com.miya.system.config.web.ReadableEnum;
import com.miya.system.module.oss.model.SysFile;
import com.miya.system.module.oss.service.SysFileService;
import com.miya.system.util.ThumbnailsUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executor;

/**
 * 文件api
 */
@RestController
@RequestMapping("/file")
@Slf4j
@Tag(name = "文件")
@Validated
@RequiredArgsConstructor
public class SysFileApi {

    /**
     * 最大上传文件数量
     */
    private static final int MAX_UPLOAD_SIZE = 10;

    private final SysFileService sysFileService;


    @Getter
    @RequiredArgsConstructor
    enum FormatType implements ReadableEnum {
        Object("对象"),
        Array("数组");

        private final String name;

    }

    /**
     * 上传文件
     * @param files
     */
    @PostMapping(value = "upload", consumes = "multipart/form-data")
    @Operation(summary = "上传文件(支持多个)")
    public R<?> upload(@NotNull @RequestParam("file") List<MultipartFile> files,
                       @Parameter(description = "在上传单个文件时返回参数的格式化类型，默认返回数组，对象类型只有在上传单个文件时生效") FormatType formatType) {
        List<SysFile> list = new ArrayList<>();
        for (MultipartFile file : files) {
            if (sysFileService.isNotAllowUpload(file)) {
                throw new ResponseCodeException(ResponseCode.Common.NOT_ALLOW_UPLOAD, file.getOriginalFilename());
            }
            list.add(sysFileService.upload(file));
        }
        FormatType ft = Optional.ofNullable(formatType).orElse(FormatType.Array);
        if (ft.equals(FormatType.Object) && list.size() == 1){
            return R.successWithData(list.get(0));
        }
        return R.successWithData(list);
    }


    /**
     * 上传文件 耗时操作，限流避免攻击
     * @param url
     */
    @PostMapping(value = "upload/imageByUrl")
    @Operation(summary = "通过url上传图片")
    @RequestLimit(count = 5, seconds = 20)
    public R<SysFile> uploadByUrl(@NotBlank String url) {
        try {
            SysFile sysFile = sysFileService.uploadImageByUrl(url);
            return R.successWithData(sysFile);
        } catch (MalformedURLException e) {
            log.trace(ExceptionUtils.getStackTrace(e));
            return R.errorWithMsg("不支持的url");
        }
    }

    /**
     * 上传图片(进行压缩) 返回对象名
     * @param images
     */
    @PostMapping(value = "upload/image", consumes = "multipart/form-data")
    @Operation(summary = "上传图片(支持多张，进行压缩,然后丢弃原图)")
    public R<List<SysFile>> uploadImages(@NotNull @RequestParam List<MultipartFile> images) throws IOException {
        if (images.size() > MAX_UPLOAD_SIZE) {
            throw new ErrorMsgException(StrUtil.format("图片个数不能超过{}", MAX_UPLOAD_SIZE));
        }
        for (MultipartFile multipartFile : images) {
            if (sysFileService.isNotAllowUpload(multipartFile)) {
                throw new ResponseCodeException(ResponseCode.Common.NOT_ALLOW_UPLOAD);
            }
            String suffix = FileUtil.extName(multipartFile.getOriginalFilename());
            if (ThumbnailsUtil.isNotSupportCompression(suffix)) {
                return R.errorWithCodeAndMsg(ResponseCode.Common.FILE_IS_NOT_IMAGE, multipartFile.getOriginalFilename());
            }
        }
        List<SysFile> list = new ArrayList<>();
        for (MultipartFile file : images){
            list.add(uploadImage(file).getData());
        }
        return R.successWithData(list);
    }

    @Resource
    @Qualifier("taskExecutor")
    private Executor executor;

    /**
     * 上传图片(进行压缩) 返回对象名
     * @param image
     */
    private R<SysFile> uploadImage(@NotNull MultipartFile image) throws IOException {
        if (sysFileService.isNotAllowUpload(image)) {
            throw new ResponseCodeException(ResponseCode.Common.NOT_ALLOW_UPLOAD);
        }
        String fileName = image.getOriginalFilename();
        String suffix = FileUtil.extName(fileName);
        if (ThumbnailsUtil.isNotSupportCompression(suffix)) {
            return R.errorWithCodeAndMsg(ResponseCode.Common.FILE_IS_NOT_IMAGE, fileName);
        }
        // 原本的文件输入流 => 图像压缩 => 管道 => 上传流具体实现 CountdownLatch同步
        PipedInputStream pipedInputStream = new PipedInputStream();
        PipedOutputStream pipedOutputStream = new PipedOutputStream();
        pipedInputStream.connect(pipedOutputStream);
        executor.execute(() -> {
            try {
                ThumbnailsUtil.compressPictureForScale(image, pipedOutputStream, 500);
            } finally {
                try {
                    pipedOutputStream.close();
                    pipedInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        SysFile sysFile = sysFileService.upload(fileName, pipedInputStream, "image/png");
        return R.successWithData(sysFile);
    }

}
