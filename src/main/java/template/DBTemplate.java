package template;

import com.alibaba.excel.annotation.ExcelProperty;
import template.annotations.common.ColumnName;
import template.annotations.db.ColumnInfo;
import template.annotations.db.MappingKey;
import template.annotations.db.TableName;
import template.meta.BaseMeta;
import template.meta.DBMeta;

import java.lang.reflect.Field;

public class DBTemplate extends Template {
    @Override
    public DBMeta parse(Class<?> clz) {
        return (DBMeta)super.parse(clz);
    }

    /**
     * 重写解析器
     */
    @Override
    protected DBMeta parse0(Class<?> clz) {
        //必须要有表名
        TableName tableName = clz.getDeclaredAnnotation(TableName.class);
        if(tableName==null){
            throw new IllegalArgumentException("DBTemplate just processes class with @TableName annotation! ");
        }
        DBMeta t = new DBMeta();
        //设置源
        t.setSource(clz);
        //设置信息：
        //设置表名
        t.setTableName(tableName.value());
        //反射
        Field[] fields = clz.getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            //基本信息
            ColumnName columnName = field.getAnnotation(ColumnName.class);
            if (columnName == null)continue;
            ExcelProperty excelProperty = field.getAnnotation(ExcelProperty.class);
            if (excelProperty== null)continue;
            //表信息
            ColumnInfo columnInfo = field.getAnnotation(ColumnInfo.class);
            if (columnInfo == null)continue;
            String s = excelProperty.value()[0];
            t.getMappingKeyFieldMap().put(new MappingKey(s,excelProperty.index()),field);
            t.getFieldColumnNameMap().put(field,columnName);
            t.getFieldExcelPropertyMap().put(field,excelProperty);
            t.getFieldColumnInfoMap().put(field,columnInfo);
        }
        return t;
    }
}
