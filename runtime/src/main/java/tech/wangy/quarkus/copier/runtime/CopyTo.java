package tech.wangy.quarkus.copier.runtime;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.CLASS)
@Target(ElementType.TYPE)
public @interface CopyTo {

    /**
     * 标记当前类可以复制给目标类型
     * @return
     */
    Class[] value();

    /**
     * 可逆的,即目标类型可以复制给当前类型
     * @return
     */

    boolean reversible() default true;
}
