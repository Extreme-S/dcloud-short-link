package org.example.strategy;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class ShardingDBConfig {

    /**
     * 存储数据库位置编号
     */
    private static final List<String> dbPrefixList = new ArrayList<>();

    private static Random random = new Random();

    // 配置启用那些库的前缀
    static {
        dbPrefixList.add("0");
        dbPrefixList.add("1");
        dbPrefixList.add("a");
    }

    /**
     * 获取随机的前缀
     */
    public static String getRandomDBPrefix() {
        int index = random.nextInt(dbPrefixList.size());
        return dbPrefixList.get(index);
    }


}
