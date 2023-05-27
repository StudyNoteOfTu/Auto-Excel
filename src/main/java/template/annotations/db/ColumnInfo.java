package template.annotations.db;

import template.annotations.common.ColumnName;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 字段在数据库中的信息
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ColumnInfo {
    //是否为主键
    boolean isPrimaryKey() default false;
    //是否自增
    boolean isAutoIncrement() default false;
    //类型，默认是text最大长度
    String type() default Type.TEXT;
    //长度，默认255
    int len() default 255;
    //...拓展其他需求
}
