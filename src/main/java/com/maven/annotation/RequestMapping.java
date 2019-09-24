package com.maven.annotation;


import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Documented
@Retention(RetentionPolicy.RUNTIME)

public @interface RequestMapping {
    String value() default "";

    String method() default "";

    String description() default "";
}
