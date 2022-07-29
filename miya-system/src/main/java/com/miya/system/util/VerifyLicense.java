package com.miya.system.util;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.crypto.asymmetric.KeyType;
import cn.hutool.crypto.asymmetric.RSA;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.util.ResourceUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

/**
 * 许可验证/生成
 * 需配合xjar实现加密jar
 * <a href="https://github.com/core-lib/xjar">xjar</a>
 */
@Slf4j
public class VerifyLicense {

    /**
     * 公钥
     */
    private static final String PUBLIC_KEY = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCksG84Paua+H7900ax9umkSOCBSFHMelVFcqnJ17c6C+OpqJXD+9l60m0EhX1SJHdr/N8XBeE36HjPVYueQ+UQdPBkfXvtRzKLjf/405f2dq9l3B1zjsty//EpX4qyKxiH4qdW467ApqaQNeHCpZyD7Na0PMpDfscJQ+bWULoe5wIDAQAB";


    private static final String LICENSE_NAME = "License";
    /**
     * 许可文件位置
     */
    private static final String LICENSE_RESOURCE_LOCATION = ResourceUtils.CLASSPATH_URL_PREFIX + LICENSE_NAME;


    public static VerifyLicense getInstance(){
        return new VerifyLicense();
    }


    /**
     * 使用公钥解密许可信息
     * @param data   license
     * @throws Exception
     */
    private LicenseInfo decryptLicense(String data) throws Exception{
        RSA rsa = new RSA(null,PUBLIC_KEY);
        byte[] decrypt = rsa.decrypt(data, KeyType.PublicKey);
        ByteArrayInputStream bais = new ByteArrayInputStream(decrypt);
        ObjectInputStream ois = new ObjectInputStream(bais);
        Object o = ois.readObject();
        if (!(o instanceof LicenseInfo)){
            return null;
        }
        return (LicenseInfo)o;
    }

    /**
     * 验证许可证 通过返回true，否则返回false
     */
    public boolean verify() {
        try {
            InputStream stream = this.getClass().getClassLoader().getResourceAsStream(LICENSE_NAME);
            String result = IoUtil.read(stream, Charset.defaultCharset());
            LicenseInfo licenseInfo = getInstance().decryptLicense(result);
            assert licenseInfo != null;
            log.info("许可过期时间：{}", DateUtil.format(licenseInfo.getExpiredTime(), "yyyy-MM-dd HH:mm:ss"));
            if(licenseInfo.getExpiredTime().after(new Date())){
                return true;
            }
        } catch (Exception e) {
            log.error(ExceptionUtils.getStackTrace(e));
        }
        return false;
    }


    public static void main(String[] args) {
        boolean verify = getInstance().verify();
        System.out.println(verify);
    }
}

/**
 * 授权信息
 */
@Getter
@Setter
class LicenseInfo implements Serializable {

    /**
     * 到期时间
     */
    private Date expiredTime;
}

/**
 * 生成许可信息
 */
class GenerateLicense {

    private static GenerateLicense generateLicense;

    /**
     * 获取实例
     */
    public static GenerateLicense getInstance(){
        if (Objects.isNull(generateLicense)){
            synchronized(GenerateLicense.class){
                if (Objects.isNull(generateLicense)){
                    generateLicense = new GenerateLicense();
                }
            }
        }
        return generateLicense;
    }


    /**
     * 构造许可信息
     * @return LicenseInfo
     */
    private LicenseInfo buildLicense(){
        LicenseInfo licenseInfo = new LicenseInfo();
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, 120);
        licenseInfo.setExpiredTime(calendar.getTime());
        return licenseInfo;
    }

    /**
     * 使用私钥加密许可信息
     * @param privateKeyStr   私钥
     * @throws Exception
     */
    private String encryptLicense(String privateKeyStr) throws Exception{
        RSA rsa = new RSA(privateKeyStr,null);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        LicenseInfo licenseInfo = buildLicense();
        //序列化许可信息对象
        oos.writeObject(licenseInfo);
        byte[] data = baos.toByteArray();
        //加密
        byte[] decrypt = rsa.encrypt(data, KeyType.PrivateKey);
        return Base64.encode(decrypt);
    }

    /**
     * 生成并保存license
     * @param license   license
     * @throws Exception
     */
    private void generateAndSaveLicense(String license) throws Exception{
        String file = ResourceUtil.getResource("License").getFile();
        System.out.println(file);


    }

    /**
     * 生成密钥对
     */
    private static void generateRsa(){
        RSA rsa = new RSA();
        System.out.println("私钥：" + rsa.getPrivateKeyBase64());
        System.out.println("公钥：" + rsa.getPublicKeyBase64());
    }

    public static void main(String[] args) throws Exception{
        //加密
//       私钥，放到注释中，打包后即消失 MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBAKSwbzg9q5r4fv3TRrH26aRI4IFIUcx6VUVyqcnXtzoL46molcP72XrSbQSFfVIkd2v83xcF4TfoeM9Vi55D5RB08GR9e+1HMouN//jTl/Z2r2XcHXOOy3L/8SlfirIrGIfip1bjrsCmppA14cKlnIPs1rQ8ykN+xwlD5tZQuh7nAgMBAAECgYEAotUHwpXHPHyCIzloZsF5FUQxJeJ5bjFuajILClTNBwmGWdMj8RjsWPIBdD0AQd1obk8hzMO1gO/Ls0QjvaHnsppzOpHmN692HKajDtFRSYiScJQXWCjMUIy/FI5eWI1/+XQaWBpQgsHBwrzTdTSSd2+UTAd+LHyGC6vvQtnRPrECQQDpSr48tT3FM29COctBTfvVO77IvMjKzcGLvm1m7aUAHtS4qWJpvLOEKfcaEvNDQRTafbM08iHppodiiqDkalRlAkEAtLg4xgoSLsdpne1M5jnZR3I1xDJ7Xzi4RMhqK1hmVi++ASp2lFmOcwI1yuloBlyOTFJmYjyC+zZEai+4YsYzWwJBAMXHyRpcqV6rKXLi3m+h9pOjkC5M5ooRADpCGiv94zoD6WIYsEdmZGby0Pv4/uWQomZN1QVZFekpdnPThW3Au4UCQHS3sns9T+cEhdMoywy6eflp6w3PKN0kDebmpaNfEaFCbm3kVfeUK6td1w45VyUcrA7g/R0fRd0nfGI0ddrddZcCQA0E/Ov9Cvriio8ReJ5CKUs7h/MsSe6DcBUKuhnEGvSZT4hc08MbrURphD32EURUWiMcbSSqrDyx6Lr1mqdsqRo=
//        String privateKeyStr = "MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBAKSwbzg9q5r4fv3TRrH26aRI4IFIUcx6VUVyqcnXtzoL46molcP72XrSbQSFfVIkd2v83xcF4TfoeM9Vi55D5RB08GR9e+1HMouN//jTl/Z2r2XcHXOOy3L/8SlfirIrGIfip1bjrsCmppA14cKlnIPs1rQ8ykN+xwlD5tZQuh7nAgMBAAECgYEAotUHwpXHPHyCIzloZsF5FUQxJeJ5bjFuajILClTNBwmGWdMj8RjsWPIBdD0AQd1obk8hzMO1gO/Ls0QjvaHnsppzOpHmN692HKajDtFRSYiScJQXWCjMUIy/FI5eWI1/+XQaWBpQgsHBwrzTdTSSd2+UTAd+LHyGC6vvQtnRPrECQQDpSr48tT3FM29COctBTfvVO77IvMjKzcGLvm1m7aUAHtS4qWJpvLOEKfcaEvNDQRTafbM08iHppodiiqDkalRlAkEAtLg4xgoSLsdpne1M5jnZR3I1xDJ7Xzi4RMhqK1hmVi++ASp2lFmOcwI1yuloBlyOTFJmYjyC+zZEai+4YsYzWwJBAMXHyRpcqV6rKXLi3m+h9pOjkC5M5ooRADpCGiv94zoD6WIYsEdmZGby0Pv4/uWQomZN1QVZFekpdnPThW3Au4UCQHS3sns9T+cEhdMoywy6eflp6w3PKN0kDebmpaNfEaFCbm3kVfeUK6td1w45VyUcrA7g/R0fRd0nfGI0ddrddZcCQA0E/Ov9Cvriio8ReJ5CKUs7h/MsSe6DcBUKuhnEGvSZT4hc08MbrURphD32EURUWiMcbSSqrDyx6Lr1mqdsqRo=";
//        String s = getInstance().encryptLicense(privateKeyStr);
//        getInstance().generateAndSaveLicense(s);
//        System.out.println(s);

        generateRsa();
    }
}
