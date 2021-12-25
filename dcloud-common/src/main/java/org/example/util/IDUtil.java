package org.example.util;

import org.apache.shardingsphere.core.strategy.keygen.SnowflakeShardingKeyGenerator;


public class IDUtil {

    private static SnowflakeShardingKeyGenerator shardingKeyGenerator = new SnowflakeShardingKeyGenerator();

    /**
     * 雪花算法生成器
     */
    public static Comparable<?> geneSnowFlakeID() {
        return shardingKeyGenerator.generateKey();
    }

}
