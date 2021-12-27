package org.example.component;

import org.example.util.CommonUtil;
import org.springframework.stereotype.Component;


@Component
public class ShortLinkComponent {

    /**
     * 62个字符
     */
    private static final String CHARS = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";


    /**
     * 生成短链码
     */
    public String createShortLinkCode(String param) {
        long murmurhash = CommonUtil.murmurHash32(param);
        return encodeToBase62(murmurhash);
    }

    /**
     * 10进制转62进制
     */
    private String encodeToBase62(long num) {
        // StringBuffer线程安全，StringBuilder线程不安全，方法栈里也是线程安全的
        StringBuilder sb = new StringBuilder();
        do {
            int i = (int) (num % 62);
            sb.append(CHARS.charAt(i));
            num = num / 62;
        } while (num > 0);
        return sb.reverse().toString();
    }


}
