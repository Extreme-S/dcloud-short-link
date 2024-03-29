package org.example.util;

import eu.bitwalker.useragentutils.Browser;
import eu.bitwalker.useragentutils.OperatingSystem;
import eu.bitwalker.useragentutils.UserAgent;
import org.apache.commons.lang3.StringUtils;
import org.example.model.DeviceInfoDO;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Map;


public class DeviceUtil {

    /**
     * 生成web设备唯一ID
     */
    public static String geneWebUniqueDeviceId(Map<String, String> map) {
        return MD5(map.toString());
    }


    /**
     * MD5加密
     */
    public static String MD5(String data) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] array = md.digest(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte item : array) {
                sb.append(Integer.toHexString((item & 0xFF) | 0x100).substring(1, 3));
            }
            return sb.toString().toUpperCase();
        } catch (Exception exception) {
        }
        return null;
    }


    /**
     * 获取浏览器对象
     */
    public static Browser getBrowser(String agent) {
        UserAgent userAgent = UserAgent.parseUserAgentString(agent);
        return userAgent.getBrowser();
    }


    /**
     * 获取操作系统
     */
    public static OperatingSystem getOperationSystem(String agent) {
        UserAgent userAgent = UserAgent.parseUserAgentString(agent);
        return userAgent.getOperatingSystem();
    }


    /**
     * 获取浏览器名称
     */
    public static String getBrowserName(String agent) {
        return getBrowser(agent).getGroup().getName();
    }


    /**
     * 获取设备类型
     */
    public static String getDeviceType(String agent) {
        return getOperationSystem(agent).getDeviceType().toString();
    }


    /**
     * 获取os: windows 、IOS、Android
     */
    public static String getOS(String agent) {
        return getOperationSystem(agent).getGroup().getName();
    }


    /**
     * 获取设备厂家
     */
    public static String getDeviceManufacturer(String agent) {
        return getOperationSystem(agent).getManufacturer().toString();
    }


    /**
     * 操作系统版本
     */
    public static String getOSVersion(String userAgent) {
        String osVersion = "";
        if (StringUtils.isBlank(userAgent)) {
            return osVersion;
        }
        String[] strArr = userAgent.substring(userAgent.indexOf("(") + 1, userAgent.indexOf(")")).split(";");
        if (null == strArr || strArr.length == 0) {
            return osVersion;
        }
        osVersion = strArr[1];
        return osVersion;
    }


    /**
     * 解析对象
     */
    public static DeviceInfoDO getDeviceInfo(String agent) {
        UserAgent userAgent = UserAgent.parseUserAgentString(agent);
        Browser browser = userAgent.getBrowser();
        OperatingSystem operatingSystem = userAgent.getOperatingSystem();

        String browserName = browser.getGroup().getName();
        String os = operatingSystem.getGroup().getName();
        String manufacture = operatingSystem.getManufacturer().toString();
        String deviceType = operatingSystem.getDeviceType().toString();

        return DeviceInfoDO.builder().browserName(browserName)
                .deviceManufacturer(manufacture)
                .deviceType(deviceType)
                .os(os)
                .osVersion(getOSVersion(agent))
                .build();
    }

}
