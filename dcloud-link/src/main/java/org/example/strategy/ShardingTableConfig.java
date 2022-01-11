package org.example.strategy;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class ShardingTableConfig {

    /**
     * 存储数据表位置编号
     */
    private static final List<String> tableSuffixList = new ArrayList<>();

    private static Random random = new Random();

    //配置启用那些表的后缀
    static {
        tableSuffixList.add("0");
        tableSuffixList.add("a");
    }


    /**
     * 获取随机的后缀
     */
    public static String getRandomTableSuffix() {
        int index = random.nextInt(tableSuffixList.size());
        return tableSuffixList.get(index);
    }


}
