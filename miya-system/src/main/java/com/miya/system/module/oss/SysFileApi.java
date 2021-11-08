package com.miya.system.module.oss;

import cn.hutool.core.io.FileUtil;
import com.miya.common.annotation.RequestLimit;
import com.miya.common.exception.ResponseCodeException;
import com.miya.common.model.dto.base.R;
import com.miya.common.model.dto.base.ResponseCode;
import com.miya.system.module.oss.model.SysFile;
import com.miya.system.module.oss.service.SysFileService;
import com.miya.system.util.PictureCompression;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

/**
 * TODO 图像contenttype
 */
@RestController
@RequestMapping("/file")
@Slf4j
@Api(tags = {"文件"})
@Validated
public class SysFileApi {
    @Resource
    private SysFileService sysFileService;

    /**
     * 上传文件 耗时操作，限流避免攻击
     * @param imageUrl
     * @return
     */
    @PostMapping(params = "imageUrl")
    @ApiOperation(value = "通过url上传图片")
    @RequestLimit(count = 5, seconds = 20)
    public R<?> uploadByUrl(@NotBlank String imageUrl) {
        try {
            SysFile sysFile = sysFileService.uploadByUrl(imageUrl);
            return R.successWithData(sysFile);
        } catch (MalformedURLException e) {
            log.trace(ExceptionUtils.getStackTrace(e));
            return R.errorWithMsg("不支持的url");
        }
    }

    /**
     * 上传文件
     * @param file
     * @return
     */
    @PostMapping
    @ApiOperation(value = "上传单个文件")
    public R<?> upload(@NotNull MultipartFile file) {
        if (sysFileService.isNotAllowUpload(file)) {
            throw new ResponseCodeException(ResponseCode.Common.NOT_ALLOW_UPLOAD);
        }
        SysFile sysFile = sysFileService.upload(file);
        return R.successWithData(sysFile);
    }

    /**
     * 上传文件
     * @param files
     * @return
     */
    @PostMapping("many")
    @ApiOperation(value = "上传多个文件")
    public R<?> upload(@NotNull @RequestParam("file") List<MultipartFile> files) {
        List<SysFile> list = new ArrayList<>();
        for (MultipartFile file : files) {
            if (sysFileService.isNotAllowUpload(file)) {
                throw new ResponseCodeException(ResponseCode.Common.NOT_ALLOW_UPLOAD);
            }
            list.add(sysFileService.upload(file));
        }
        return R.successWithData(list);
    }

    @Resource
    @Qualifier("taskExecutor")
    private Executor executor;

    /**
     * 上传图片(进行压缩) 返回对象名
     * @param file
     * @return
     */
    @PostMapping(params = "image")
    @ApiOperation(value = "上传图片(进行压缩,丢弃原图)")
    public R<SysFile> uploadImage(@NotNull MultipartFile file) throws IOException {
        if (sysFileService.isNotAllowUpload(file)) {
            throw new ResponseCodeException(ResponseCode.Common.NOT_ALLOW_UPLOAD);
        }
        String fileName = file.getOriginalFilename();
        String suffix = FileUtil.extName(fileName);
        if (PictureCompression.isNotSupportCompression(suffix)) {
            return R.errorWithCodeAndMsg(ResponseCode.Common.FILE_IS_NOT_IMAGE);
        }
//      原本的文件输入流 => 图像压缩 => 管道 => minio CountdownLatch同步
        PipedInputStream pipedInputStream = new PipedInputStream();
        PipedOutputStream pipedOutputStream = new PipedOutputStream();
        pipedInputStream.connect(pipedOutputStream);
        executor.execute(() -> {
            try {
                PictureCompression.compressImageOfDetail(file.getInputStream(), pipedOutputStream);
            } catch (IOException e) {
                e.printStackTrace();
            }finally {
                try {
                    pipedOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        SysFile sysFile = sysFileService.upload(fileName, pipedInputStream, "image/png");
        return R.successWithData(sysFile);
    }

    /**
     * 上传图片 多张(进行压缩) 返回对象名
     * @param files
     * @return
     */
    @PostMapping(params = {"image", "files"})
    @ApiOperation(value = "上传图片多张(进行压缩,丢弃原图)")
    public R<List<SysFile>> uploadImages(@NotNull @RequestParam List<MultipartFile> files) throws IOException {
        for (MultipartFile multipartFile : files) {
            if (sysFileService.isNotAllowUpload(multipartFile)) {
                throw new ResponseCodeException(ResponseCode.Common.NOT_ALLOW_UPLOAD);
            }
        }
        List<SysFile> list = new ArrayList<>();
        for (MultipartFile file : files){
            list.add(uploadImage(file).getData());
        }
        return R.successWithData(list);
    }

}
