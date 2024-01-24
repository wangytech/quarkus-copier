package tech.wangy.quarkus.copier.runtime;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记当前类是复制转换器, 该转换器可以继承 CopierConvertor
 * 或者存在 public static T convert(F from)的方法.
 */
@Retention(RetentionPolicy.CLASS)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface CopierConvert {
}
