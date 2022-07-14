package org.example.annotation;

import java.lang.annotation.*;

/**
 * 自定义防重提交
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)//保存源码到RUNTIME级别才能通过反射去获取到内容
public @interface RepeatSubmit {

    /**
     * 防重提交，支持两种，一个是方法参数，一个是令牌
     */
    enum Type {PARAM, TOKEN}

    /**
     * 默认防重提交，是方法参数
     */
    Type limitType() default Type.PARAM;

    /**
     * 加锁过期时间，默认是5秒
     */
    long lockTime() default 5;

}
