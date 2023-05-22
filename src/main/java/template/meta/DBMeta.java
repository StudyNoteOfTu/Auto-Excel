package template.meta;

import template.annotations.db.ColumnInfo;
import template.annotations.db.MappingKey;

import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 数据库的模板类元
 */
public class DBMeta extends BaseMeta {
    /**
     * 额外增加了表字段
     */
    private String tableName;
    private String id;
    private final Map<MappingKey,Field> mMappingKeyFieldMap;
    private final Map<Field, ColumnInfo> mFieldColumnInfoMap;

    public DBMeta() {
        mFieldColumnInfoMap = new LinkedHashMap<>();
        mMappingKeyFieldMap = new LinkedHashMap<>();
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Map<MappingKey, Field> getMappingKeyFieldMap() {
        return mMappingKeyFieldMap;
    }

    public Map<Field, ColumnInfo> getFieldColumnInfoMap() {
        return mFieldColumnInfoMap;
    }
}
