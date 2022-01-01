package com.miya.system.util;

import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.geometry.Positions;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Author：马兆祥 2018-10-25
 * 图片压缩工具类
 */
@Slf4j
public class PictureCompression {

    /**
     * 定义缩略图最大宽高
     */
    public static final int THUMBNAIL_WIDTH = 256;
    public static final int THUMBNAIL_HEIGHT = 256;

    /**
     * 定义详情图的最大宽高
     */
    public static final int DETAIL_WIDTH = 1080;
    public static final int DETAIL_HEIGHT = 2340;

    /**
     * 判断文件名是否可压缩
     *
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
     * 压缩为缩略图
     * @param imgSrc    待压缩图路径
     * @param imgDist   压缩后目标路径
     * @throws IOException
     */
    public static void compressImageOfThumbnail(String imgSrc, String imgDist) throws IOException {
        compressImage(imgSrc,imgDist, THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT);
    }

    /**
     * 压缩为详情图
     * @param imgSrc    待压缩图路径
     * @param imgDist   压缩后目标路径
     * @throws IOException
     */
    public static void compressImageOfDetail(String imgSrc, String imgDist) throws IOException {
        compressImage(imgSrc,imgDist, DETAIL_WIDTH, DETAIL_HEIGHT);
    }

    /**
     * 压缩为详情图
     * @param inputStream    待压缩图读流
     * @param outputStream   压缩后写流
     * @throws IOException
     */
    public static void compressImageOfDetail(InputStream inputStream, OutputStream outputStream) throws IOException {
        Thumbnails.of(inputStream).size(DETAIL_WIDTH, DETAIL_HEIGHT).toOutputStream(outputStream);
    }

    /**
     * 指定最大宽高进行等比例缩放(保持图像比例)
     * @param imgSrc    待压缩图路径
     * @param imgDist   压缩后目标路径
     * @param maxWidth  压缩后最大宽度
     * @param maxHeight 压缩后最大高度
     * @throws IOException
     */
    public static void compressImage(String imgSrc, String imgDist, int maxWidth, int maxHeight) throws IOException {
        Thumbnails.of(imgSrc).size(maxWidth, maxHeight).toFile(imgDist);
    }

    /**
     * 指定宽高进行压缩(不保持图像比例)
     * @param imgSrc  待压缩图路径
     * @param imgDist 压缩后目标路径
     * @param width   压缩后最大宽度
     * @param height  压缩后最大高度
     * @throws IOException
     */
    public static void compressImageByWH(String imgSrc, String imgDist, int width, int height) throws IOException {
        /**
         * keepAspectRatio(false) 默认是按照比例缩放的
         */
        Thumbnails.of(imgSrc).size(width, height).keepAspectRatio(false).toFile(imgDist);
    }

    /**
     * 按照指定比例进行缩放
     *
     * @param imgSrc  待压缩图路径
     * @param imgDist 压缩后目标路径
     * @param rate    压缩比例
     * @throws IOException
     */
    public static void compressImage(String imgSrc, String imgDist, double rate) throws IOException {
        Thumbnails.of(imgSrc).scale(rate).toFile(imgDist);
    }

    /**
     * 旋转
     *
     * @throws IOException
     */
    private void test4() throws IOException {
        /**
         * rotate(角度),正数：顺时针 负数：逆时针
         */
        Thumbnails.of("images/test.jpg").size(1280, 1024).rotate(90).toFile("C:/image+90.jpg");
        Thumbnails.of("images/test.jpg").size(1280, 1024).rotate(-90).toFile("C:/iamge-90.jpg");
    }

    /**
     * 水印
     *
     * @throws IOException
     */
    private void test5() throws IOException {
        /**
         * watermark(位置，水印图，透明度)
         */
        // Thumbnails.of("images/test.jpg").size(1280, 1024).watermark(Positions.BOTTOM_RIGHT, ImageIO.read(new File("images/watermark.png")), 0.5f)
        //         .outputQuality(0.8f).toFile("C:/image_watermark_bottom_right.jpg");
        // Thumbnails.of("images/test.jpg").size(1280, 1024).watermark(Positions.CENTER, ImageIO.read(new File("images/watermark.png")), 0.5f)
        //         .outputQuality(0.8f).toFile("C:/image_watermark_center.jpg");
    }


//******************************以下功能未经过测试**************************************************


    /**
     * 裁剪
     *
     * @throws IOException
     */
    private void test6() throws IOException {
        /**
         * 图片中心400*400的区域
         */
        Thumbnails.of("images/test.jpg").sourceRegion(Positions.CENTER, 400, 400).size(200, 200).keepAspectRatio(false)
                .toFile("C:/image_region_center.jpg");
        /**
         * 图片右下400*400的区域
         */
        Thumbnails.of("images/test.jpg").sourceRegion(Positions.BOTTOM_RIGHT, 400, 400).size(200, 200).keepAspectRatio(false)
                .toFile("C:/image_region_bootom_right.jpg");
        /**
         * 指定坐标
         */
        Thumbnails.of("images/test.jpg").sourceRegion(600, 500, 400, 400).size(200, 200).keepAspectRatio(false).toFile("C:/image_region_coord.jpg");
    }

    /**
     * 转化图像格式
     *
     * @throws IOException
     */
    private void test7() throws IOException {
        /**
         * outputFormat(图像格式)
         */
        Thumbnails.of("images/test.jpg").size(1280, 1024).outputFormat("png").toFile("C:/image_1280x1024.png");
        Thumbnails.of("images/test.jpg").size(1280, 1024).outputFormat("gif").toFile("C:/image_1280x1024.gif");
    }


    /**
     * 获取图片宽度和高度
     *
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

}
