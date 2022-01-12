package org.example.strategy;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class ShardingTableConfig {

    /**
     * 存储数据表位置编号
     */
    private static final List<String> tableSuffixList = new ArrayList<>();


    //配置启用那些表的后缀
    static {
        tableSuffixList.add("0");
        tableSuffixList.add("a");
    }

    /**
     * 根据code生成表位后缀
     */
    public static String getRandomTableSuffix(String code) {
        int hashCode = code.hashCode();
        int index = Math.abs(hashCode) % tableSuffixList.size();
        return tableSuffixList.get(index);
    }
}
