package template;

import com.alibaba.excel.annotation.ExcelProperty;
import template.annotations.common.ColumnName;
import template.meta.BaseMeta;
import utils.common.collections.LruMap;

import java.lang.reflect.Field;
import java.util.Map;

/**
 * 模板类构造器
 * 1. 反射解析类信息到类元数据
 * 2. 缓存类元数据
 * 3. 可以被继承，重写parse方法，解析更多的TemplateMeta
 */
public class Template {
    /**
     * 锁
     */
    protected final Object lock = new Object();

    /**
     * 类元信息集合 - 全局
     */
    private static final Map<Class<?>, BaseMeta> metaCache = new LruMap<>(50);

    /**
     * 类元解析器
     */
    public BaseMeta parse(Class<?> clz) {
        if (metaCache.containsKey(clz)) {
            return metaCache.get(clz);
        } else {
            synchronized (lock) {
                if (!metaCache.containsKey(clz)) {
                    //真正进行解析
                    BaseMeta t = parse0(clz);
                    metaCache.put(clz, t);
                    return t;
                } else {
                    return metaCache.get(clz);
                }
            }
        }
    }

    /**
     * 默认的解析方式
     */
    protected BaseMeta parse0(Class<?> clz) {
        //只能解析默认内容，即 @ColumnName, @ExcelProperty
        BaseMeta t = new BaseMeta();
        t.setSource(clz);
        //反射所有字段
        Field[] fields = clz.getDeclaredFields();
        //遍历所有字段
        for (Field field : fields) {
            //置为可访问
            field.setAccessible(true);
            //获取 @ColumnName 内容
            ColumnName columnName = field.getAnnotation(ColumnName.class);
            if (columnName == null) continue;
            //获取 @ExcelProperty 内容
            ExcelProperty excelProperty = field.getAnnotation(ExcelProperty.class);
            if (excelProperty == null) continue;
            //设置反射缓存
            t.getFieldColumnNameMap().put(field,columnName);
            t.getFieldExcelPropertyMap().put(field,excelProperty);
        }
        return t;
    }
}
