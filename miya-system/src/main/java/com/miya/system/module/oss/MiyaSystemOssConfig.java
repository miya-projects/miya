package com.miya.system.module.oss;

import cn.hutool.core.io.FileUtil;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;

public interface MiyaSystemOssConfig {

    /**
     * 允许上传的文件后缀名
     */
    String[] DEFAULT_ALLOW_SUFFIX = {"jpg", "jpeg", "bmp", "gif", "png", "pdf", "doc", "docx", "xls", "xlsx", "xlsm", "ppt", "pptx"};

    /**
     * 是否允许上传该文件
     * @param file  文件名
     */
    default boolean isNotAllowUpload(MultipartFile file){
        String fileName = file.getOriginalFilename();
        String suffix = FileUtil.extName(fileName);
        return Arrays.stream(allowUploadSuffix()).noneMatch(s -> s.equalsIgnoreCase(suffix));
    }

    /**
     * 允许上传文件的后缀
     */
    default String[] allowUploadSuffix(){
        return DEFAULT_ALLOW_SUFFIX;
    }

}
