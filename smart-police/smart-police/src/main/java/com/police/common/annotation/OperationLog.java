package com.police.common.annotation;

import java.lang.annotation.*;

/**
 * 操作日志注解 — 标注在 Controller 方法上，由 AOP 切面自动记录
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OperationLog {
    /** 操作模块 */
    String module() default "";
    /** 操作动作 */
    String action() default "";
    /** 是否记录请求体 */
    boolean logBody() default true;
}
