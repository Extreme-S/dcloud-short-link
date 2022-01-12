package org.example.strategy;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class ShardingDBConfig {

    /**
     * 存储数据库位置编号
     */
    private static final List<String> dbPrefixList = new ArrayList<>();

    // 配置启用那些库的前缀
    static {
        dbPrefixList.add("0");
        dbPrefixList.add("1");
        dbPrefixList.add("a");
    }

    /**
     * 根据code生成库位前缀
     */
    public static String getRandomDBPrefix(String code) {
        int hashCode = code.hashCode();
        int index = Math.abs(hashCode) % dbPrefixList.size();
        return dbPrefixList.get(index);
    }


}
