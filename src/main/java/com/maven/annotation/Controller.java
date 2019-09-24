package com.maven.annotation;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME) //设置该注解保留策略
@Target({ElementType.TYPE})
public @interface Controller {
    public String value() default "";

    public String description() default "";
}
