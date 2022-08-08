package org.example.constant;

public class LuaScript {

    // 检查key是否存在，然后递减，是否大于等于0，使用lua脚本
    // 如果key不存在，则未使用过，lua返回值是0； 新增流量包的时候，不用重新计算次数，直接删除key,消费的时候回计算更新
    /**
     * 检查key是否存在，然后递减
     */
    public static final String USER_DAY_TOTAL_TRAFFIC_DECR =
            "if redis.call('get',KEYS[1]) " +
            "then return redis.call('decr',KEYS[1]) " +
            "else return 0 " +
            "end";
}
