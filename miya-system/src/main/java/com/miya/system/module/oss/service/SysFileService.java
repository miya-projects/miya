package com.miya.system.module.oss.service;

import cn.hutool.core.io.FileUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.miya.common.exception.ErrorMsgException;
import com.miya.system.module.oss.domain.ClassPathSysFile;
import com.miya.system.module.oss.model.SysFile;
import org.springframework.http.HttpHeaders;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotBlank;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 文件服务
 */
public interface SysFileService {

    /**
     * 允许上传的文件后缀名
     */
    String[] ALLOW_SUFFIX = {"jpg", "jpeg", "bmp", "gif", "png", "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx"};
    Pattern IMAGE_PATTERN = Pattern.compile("image/(.+)");

    /**
     * 默认图片
     */
    ClassPathSysFile DEFAULT_AVATAR = new ClassPathSysFile("default_avatar.jpg", "默认头像");
    ClassPathSysFile NOT_FOUND = new ClassPathSysFile("404.jpg", "找不到图像");

    /**
     * 上传一个文件
     * @param file
     * @return
     */
    SysFile upload(MultipartFile file);

    /**
     * 上传一个文件 通过流
     * @param fileName
     * @param inputStream
     * @param contentType
     * @return
     */
    SysFile upload(String fileName, InputStream inputStream, String contentType);

    /**
     * 是否允许上传
     * @param file
     */
    default boolean isNotAllowUpload(MultipartFile file){
        String fileName = file.getOriginalFilename();
        String suffix = FileUtil.extName(fileName);
        return Arrays.stream(ALLOW_SUFFIX).noneMatch(s -> s.equalsIgnoreCase(suffix));
    }

    /**
     * 上传一个文件 通过流
     * @param fileName
     * @param inputStream
     * @return
     */
    default SysFile upload(String fileName, InputStream inputStream) {
        return upload(fileName, inputStream, null);
    }

    /**
     * 通过url上传图像
     * @param imageUrl
     * @return
     */
    default SysFile uploadByUrl(@NotBlank String imageUrl) throws MalformedURLException {
        //测试url是否合法
        new URL(imageUrl);
        HttpRequest get = HttpUtil.createGet(imageUrl);
        HttpResponse response = get.execute();
        String contentType = response.header(HttpHeaders.CONTENT_TYPE);
        Matcher matcher = IMAGE_PATTERN.matcher(contentType);
        if (!matcher.matches()){
            throw new ErrorMsgException("该链接指向内容非图像");
        }
        String suffix = matcher.group(1);
        if (suffix.startsWith("svg")){
            suffix = "svg";
        }
        return upload(UUID.randomUUID().toString() + "." + suffix,
                response.bodyStream(), "image/png");
    }

    /**
     * 删除数据库实体和文件存储
     * @param id
     */
    void deleteById(String id);

    /**
     * 获取外部可访问的url
     * @param sysFile
     * @return
     */
    String getUrl(SysFile sysFile);

    /**
     * 获取流
     * @param sysFile
     * @return
     */
    InputStream openStream(SysFile sysFile);


    /**
     * todo 清理实际不存在的文件对象
     * 应该是一个耗时很长的任务需异步处理
     */
    // void deleteDanglingSysFiles();


}
