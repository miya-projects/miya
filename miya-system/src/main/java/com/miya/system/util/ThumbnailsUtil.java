package com.miya.system.util;

import cn.hutool.core.io.IoUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * @link {<a href="https://github.com/coobird/thumbnailator/wiki/Examples">Examples</a>}
 * <p>
 * 图片压缩工具类
 */
@Slf4j
public class ThumbnailsUtil {

    /**
     * 判断文件名是否可压缩
     * @param suffix 需要判断的文件类型
     */
    public static boolean isNotSupportCompression(String suffix) {
        final String[] CAN_COMPRESSION_SUFFIX = {
                "jpg", "jpeg", "png", "bmp", "gif"
        };
        for (String can_compression_suffix : CAN_COMPRESSION_SUFFIX) {
            if (can_compression_suffix.equals(suffix)) {
                return false;
            }
        }
        return true;
    }


    /**
     * 获取图片宽度和高度
     * @param file 图片路径
     * @return 返回图片的宽度
     */
    public static int[] getImgWidthHeight(File file) {
        int[] result = {0, 0};
        try {
            // 获得文件输入流
            InputStream is = new FileInputStream(file);
            // 从流里将图片写入缓冲图片区
            BufferedImage src = ImageIO.read(is);
            result[0] = src.getWidth(null); // 得到源图片宽
            result[1] = src.getHeight(null);// 得到源图片高
            is.close();  //关闭输入流
        } catch (Exception ef) {
            ef.printStackTrace();
        }
        return result;
    }

    /**
     * 根据指定大小压缩图片
     * @param multipartFile 上传图像对象
     * @param outputStream 压缩后输出流
     * @param desFileSize  指定压缩后的图片大小，单位kb
     */
    public static void compressPictureForScale(MultipartFile multipartFile, OutputStream outputStream, long desFileSize) {
        try {
            compressPictureForScale(multipartFile.getInputStream(), multipartFile.getSize(), outputStream, desFileSize);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 根据指定大小压缩图片
     * @param inputStream  源图片输入流
     * @param outputStream 压缩后输出流
     * @param desFileSize  指定压缩后的图片大小，单位kb
     */
    public static void compressPictureForScale(InputStream inputStream, long sourceSize, OutputStream outputStream, long desFileSize) {
        double accuracy = getAccuracy(sourceSize / 1024);
        // 压缩次数，最多压缩5次
        int reduceCount = 0;
        try {
            byte[] bytes = IoUtil.readBytes(inputStream);

            while (bytes.length > desFileSize * 1024 && reduceCount < 5) {
                ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(bytes.length);
                Thumbnails.of(byteArrayInputStream).scale(accuracy).outputQuality(accuracy).toOutputStream(byteArrayOutputStream);
                bytes = byteArrayOutputStream.toByteArray();
                reduceCount++;
            }
            log.info("【图片压缩】 | 图片原大小={}kb | 压缩后大小={}kb, 压缩次数={}", sourceSize / 1024,
                    bytes.length / 1024, reduceCount);
            IoUtil.write(outputStream, true, bytes);
        } catch (Exception e) {
            log.error("【图片压缩】msg=图片压缩失败!", e);
        }
    }


    /**
     * 根据指定大小压缩图片
     * @param imageBytes  源图片字节数组
     * @param desFileSize 指定图片大小，单位kb
     * @return 压缩质量后的图片字节数组
     */
    public static byte[] compressPictureForScale(byte[] imageBytes, long desFileSize) {
        if (imageBytes == null || imageBytes.length <= 0 || imageBytes.length < desFileSize * 1024) {
            return imageBytes;
        }
        long srcSize = imageBytes.length;
        double accuracy = getAccuracy(srcSize / 1024);
        // 压缩次数，最多压缩5次
        int reduceCount = 0;
        try {
            while (imageBytes.length > desFileSize * 1024 && reduceCount < 5) {
                ByteArrayInputStream inputStream = new ByteArrayInputStream(imageBytes);
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream(imageBytes.length);
                Thumbnails.of(inputStream).scale(accuracy).outputQuality(accuracy).toOutputStream(outputStream);
                imageBytes = outputStream.toByteArray();
                reduceCount++;
            }
            log.info("【图片压缩】 | 图片原大小={}kb | 压缩后大小={}kb, 压缩次数={}", srcSize / 1024,
                    imageBytes.length / 1024, reduceCount);
        } catch (Exception e) {
            log.error("【图片压缩】msg=图片压缩失败!", e);
        }
        return imageBytes;
    }

    /**
     * 自动调节精度(经验数值)
     * @param size 源图片大小
     * @return 图片压缩质量比
     */
    private static double getAccuracy(long size) {
        double accuracy;
        if (size < 900) {
            accuracy = 0.85;
        } else if (size < 2047) {
            accuracy = 0.6;
        } else if (size < 3275) {
            accuracy = 0.44;
        } else {
            accuracy = 0.4;
        }
        return accuracy;
    }

/*
        旋转
        Thumbnails.of("images/test.jpg").size(1280, 1024).rotate(90).toFile("C:/image+90.jpg");
        水印
        // Thumbnails.of("images/test.jpg").size(1280, 1024).watermark(Positions.BOTTOM_RIGHT, ImageIO.read(new File("images/watermark.png")), 0.5f)
        //         .outputQuality(0.8f).toFile("C:/image_watermark_bottom_right.jpg");
        转化图像格式
        Thumbnails.of("images/test.jpg").size(1280, 1024).outputFormat("png").toFile("C:/image_1280x1024.png");
        裁剪
        图片中心400*400的区域
        Thumbnails.of("images/test.jpg").sourceRegion(Positions.CENTER, 400, 400).size(200, 200).keepAspectRatio(false)
        .toFile("C:/image_region_center.jpg");
        图片右下400*400的区域
        Thumbnails.of("images/test.jpg").sourceRegion(Positions.BOTTOM_RIGHT, 400, 400).size(200, 200).keepAspectRatio(false)
        .toFile("C:/image_region_bootom_right.jpg");
        指定坐标
        Thumbnails.of("images/test.jpg").sourceRegion(600, 500, 400, 400).size(200, 200).keepAspectRatio(false).toFile("C:/image_region_coord.jpg");
     */


    @SneakyThrows
    public static void main(String[] args) {
        String input = "C:\\Users\\MI\\Desktop\\1.jpg";
        String watermark = "C:\\Users\\MI\\Desktop\\default.jpg";
        byte[] bytes = compressPictureForScale(IoUtil.readBytes(Files.newInputStream(Paths.get(new File(input).toURI())), true), 500);
        IoUtil.write(new FileOutputStream("C:\\Users\\MI\\Desktop\\2.jpg"), true, bytes);
    }


}
