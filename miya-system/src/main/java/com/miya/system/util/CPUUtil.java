package com.miya.system.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * 获取cpu相关信息
 */
@Slf4j
public class CPUUtil {
    /**
     * 获取当前操作系统名称. return 操作系统名称 例如:windows xp,linux 等.
     */
    public static String getOSName() {
        return System.getProperty("os.name").toLowerCase();
    }


    /**
     * 获取CPU序列号
     */
    public static String getCPUIDByWindows() {
        String result = "";
        try {
            File file = File.createTempFile("tmp", ".vbs");
            file.deleteOnExit();
            FileWriter fw = new java.io.FileWriter(file);
            String vbs = "Set objWMIService = GetObject(\"winmgmts:\\\\.\\root\\cimv2\")\n"
                    + "Set colItems = objWMIService.ExecQuery _ \n"
                    + "   (\"Select * from Win32_Processor\") \n"
                    + "For Each objItem in colItems \n"
                    + "    Wscript.Echo objItem.ProcessorId \n"
                    + "    exit for  ' do the first cpu only! \n" + "Next \n";


            // + "    exit for  \r\n" + "Next";
            fw.write(vbs);
            fw.close();
            Process p = Runtime.getRuntime().exec(
                    "cscript //NoLogo " + file.getPath());
            BufferedReader input = new BufferedReader(new InputStreamReader(
                    p.getInputStream()));
            String line;
            while ((line = input.readLine()) != null) {
                result += line;
            }
            input.close();
            file.delete();
        } catch (Exception e) {
            log.error(ExceptionUtils.getStackTrace(e));
        }
        return result.trim();
    }


    public static String getCPUIDByLinux() {
        String result = "";
        String CPU_ID_CMD = "dmidecode";
        BufferedReader bufferedReader = null;
        Process p = null;
        try {
            p = Runtime.getRuntime().exec(new String[]{ "sh", "-c", CPU_ID_CMD });// 管道
            bufferedReader = new BufferedReader(new InputStreamReader(
                    p.getInputStream()));
            String line = null;
            int index = -1;
            while ((line = bufferedReader.readLine()) != null) {
                // 寻找标示字符串[hwaddr]
                index = line.toLowerCase().indexOf("uuid");
                if (index >= 0) {// 找到了
                    // 取出mac地址并去除2边空格
                    result = line.substring(index + "uuid".length() + 1).trim();
                    break;
                }
            }

        } catch (IOException e) {
            log.error(ExceptionUtils.getStackTrace(e));
        }
        return result.trim();
    }


    public static String getCPUId() throws InterruptedException {
        String os = getOSName();
        String cpuId = "";
        if (os.startsWith("windows")) {
            cpuId = CPUUtil.getCPUIDByWindows();
        } else if (os.startsWith("linux")) {
            cpuId = CPUUtil.getCPUIDByLinux();
        }
        if(!StringUtils.isNotBlank(cpuId)){
            cpuId="null";
        }
        return cpuId;
    }


    public static void main(String[] args) throws Exception {
        String os = getOSName();
        System.out.println(os);
        String cpuid = getCPUId();
        System.out.println(cpuid);
    }


}
