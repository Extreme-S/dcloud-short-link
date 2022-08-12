package org.example.util;

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


}
