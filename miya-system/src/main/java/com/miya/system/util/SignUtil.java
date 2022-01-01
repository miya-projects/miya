package com.miya.system.util;

import lombok.extern.slf4j.Slf4j;


/**
 * @author 樊超
 * 签名认证
 */
@Slf4j
public class SignUtil {

    private static final int NONCE_DIGIT = 10;
    private static final String SHEX_CHARS = "0123456789abcdef";

    /**
     * 非对称数据加密 加密数据必须为字符串(第一位最好是数字)
     * <p>
     * 数据加密步骤 1,根据数据第一位 1.1 奇数不变 1.2 偶数反转 2,对排序后数据进行SHA加密 3,加密后的数据全部转为大写
     *
     * @param encryptData 加密的字符串数据
     */
    public static String encodeSign(String encryptData) {
        try {
            if (encryptData != null && !encryptData.trim().equals("")) {
                // 进行随机反转
                encryptData = randomReverse(encryptData);
                int[] x = alignSHA1(encryptData);
                int[] w = new int[80];
                int numberKeyOne = 1732584193, numberKeyTwo = -271733879, numberKeyThree = -1732584194,
                        numberKeyFour = 271733878, numberKeyFive = -1009589776;
                for (int i = 0; i < x.length; i += 16) {
                    int numberKeyOneOld = numberKeyOne, numberKeyTwoOld = numberKeyTwo,
                            numberKeyThreeOld = numberKeyThree, numberKeyFourOld = numberKeyFour,
                            numberKeyFiveOld = numberKeyFive;
                    for (int j = 0; j < 80; j++) {
                        if (j < 16){
                            w[j] = x[i + j];
                        } else{
                            w[j] = rol(w[j - 3] ^ w[j - 8] ^ w[j - 14] ^ w[j - 16], 1);
                        }
                        int t = add(add(rol(numberKeyOne, 5), ft(j, numberKeyTwo, numberKeyThree, numberKeyFour)),
                                add(add(numberKeyFive, w[j]), kt(j)));
                        numberKeyFive = numberKeyFour;
                        numberKeyFour = numberKeyThree;
                        numberKeyThree = rol(numberKeyTwo, 30);
                        numberKeyTwo = numberKeyOne;
                        numberKeyOne = t;
                    }
                    numberKeyOne = add(numberKeyOne, numberKeyOneOld);
                    numberKeyTwo = add(numberKeyTwo, numberKeyTwoOld);
                    numberKeyThree = add(numberKeyThree, numberKeyThreeOld);
                    numberKeyFour = add(numberKeyFour, numberKeyFourOld);
                    numberKeyFive = add(numberKeyFive, numberKeyFiveOld);
                }
                String SHA1Value = sha1Hex(numberKeyOne) + sha1Hex(numberKeyTwo) + sha1Hex(numberKeyThree)
                        + sha1Hex(numberKeyFour) + sha1Hex(numberKeyFive);
                return SHA1Value.toUpperCase();
            } else {
                log.error("生成签名失败,签名数据必须字符串.");
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return "error";
    }

    /**
     * 生成随机数
     *
     * @return 字符串随机数
     */
    private static String getNonce() {
        // 0-9的随机数
        StringBuilder numberStr = new StringBuilder();
        for (int i = 0; i < NONCE_DIGIT; i++) {
            numberStr.append(Math.random() * 9);// Math.random();每次生成(0-1)之间的数;
        }
        return numberStr.toString();
    }

    /**
     * @param str 需要随机反转的字符串
     * @return 随机反转后的字符串
     */
    private static String randomReverse(String str) {
        try {
            int data = Integer.parseInt(str.substring(0, 1));
            // 第一位数字为偶数时反转字符串
            if (data % 2 == 0) {
                return new StringBuilder(str).reverse().toString();
            }
        } catch (NumberFormatException e) {
            log.error("签名认证数据异常");
        }
        return str;
    }

    private static String sha1Hex(int num) {
        StringBuilder str = new StringBuilder();
        for (int j = 7; j >= 0; j--) {
            str.append(SHEX_CHARS.charAt((num >> (j * 4)) & 0x0F));
        }
        return str.toString();
    }

    private static int[] alignSHA1(String sIn) {
        int keyCount = ((sIn.length() + 8) >> 6) + 1;
        int[] keyCounts = new int[keyCount * 16];
        int i;
        for (i = 0; i < sIn.length(); i++) {
            keyCounts[i >> 2] |= sIn.charAt(i) << (24 - (i & 3) * 8);
        }
        keyCounts[i >> 2] |= 0x80 << (24 - (i & 3) * 8);
        keyCounts[keyCount * 16 - 1] = sIn.length() * 8;
        return keyCounts;
    }

    private static int rol(int num, int cnt) {
        return (num << cnt) | (num >>> (32 - cnt));
    }

    private static int ft(int t, int b, int c, int d) {
        if (t < 20){
            return (b & c) | ((~b) & d);
        }
        if (t < 40){
            return b ^ c ^ d;
        }
        if (t < 60){
            return (b & c) | (b & d) | (c & d);
        }
        return b ^ c ^ d;
    }

    private static int add(int x, int y) {
        return ((x & 0x7FFFFFFF) + (y & 0x7FFFFFFF)) ^ (x & 0x80000000) ^ (y & 0x80000000);
    }

    private static int kt(int t) {
        return (t < 20) ? 1518500249 : (t < 40) ? 1859775393 : (t < 60) ? -1894007588 : -899497514;
    }
}
