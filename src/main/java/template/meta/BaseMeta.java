package template.meta;

import com.alibaba.excel.annotation.ExcelProperty;
import template.annotations.common.ColumnName;

import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 模板类的类信息，用于缓存反射后的字段
 * 模板类可拓展，这里只提供最基础的版本
 *      - @ColumnName
 *      - @ExcelProperty
 */
public class BaseMeta {
    protected Class<?> source;
    protected final Map<Field, ColumnName> mFieldColumnNameMap;
    protected final Map<Field, ExcelProperty> mFieldExcelPropertyMap;

    public BaseMeta() {
        this.mFieldColumnNameMap = new LinkedHashMap<>();
        this.mFieldExcelPropertyMap = new LinkedHashMap<>();
    }

    public Map<Field, ColumnName> getFieldColumnNameMap() {
        return mFieldColumnNameMap;
    }

    public Map<Field, ExcelProperty> getFieldExcelPropertyMap() {
        return mFieldExcelPropertyMap;
    }

    public Class<?> getSource() {
        return source;
    }

    public void setSource(Class<?> source) {
        this.source = source;
    }
}
