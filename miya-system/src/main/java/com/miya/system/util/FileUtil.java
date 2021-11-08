package com.miya.system.util;


/**
 * 文件工具类
 */
public class FileUtil {

    /**
     * 判断一个目录是否系统敏感目录
     *
     * @param dirPath
     * @return
     */
    private static boolean isDangerDir(String dirPath) {
        //敏感目录
        final String[] DANGER_DIR = {"/", "/usr", "/bin", "/sbin"};
        for (int i = 0; i < DANGER_DIR.length; i++) {
            if (DANGER_DIR[i].equals(dirPath)) {
                return true;
            }
        }
        return false;
    }
}
