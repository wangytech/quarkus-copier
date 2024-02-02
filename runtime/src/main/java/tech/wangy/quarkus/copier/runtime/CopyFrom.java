package tech.wangy.quarkus.copier.runtime;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.CLASS)
@Target(ElementType.TYPE)
public @interface CopyFrom {

    /**
     * 标记当前类可以从目标类型复制
     *
     * @return
     */
    Class[] value();

    /**
     * 可逆的,即当前类可以复制给目标类
     *
     * @return
     */

    boolean reversible() default true;
}
