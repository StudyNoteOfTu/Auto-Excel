package template.annotations.db;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
/**
 * 模板类对应的表名
 */
public @interface TableName {
    /**
     * tableName
     */
    String value();

    //String engine();
}
