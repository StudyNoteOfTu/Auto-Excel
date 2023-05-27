package utils.common.db;

import com.alibaba.excel.annotation.ExcelProperty;
import template.DBTemplate;
import template.annotations.common.ColumnName;
import template.annotations.db.ColumnInfo;
import template.annotations.db.MappingKey;
import template.meta.BaseMeta;
import template.meta.DBMeta;
import utils.common.collections.LruMap;
import utils.common.db.jdbc.sqlUtils.CRUDTemplate;
import utils.common.db.jdbc.sqlUtils.handler.IResultSetHandler;
import utils.common.db.jdbc.sqlUtils.sqlBean.SqlParam;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class TableUtils {
    //线程池
    private static ThreadPoolExecutor tp = new ThreadPoolExecutor(0, 20, 3, TimeUnit.SECONDS, new ArrayBlockingQueue<>(20), new ThreadPoolExecutor.CallerRunsPolicy());
    //插入数据拼接的Sql语句缓存
    private static Map<Class<?>, String> insertSqlCache = new LruMap<>(50);
    private static Map<Class<?>, String> getAllSqlCache = new LruMap<>(50);
    private static Map<Class<?>, List<String>> getAllColumnCache = new LruMap<>(50);

    /**
     * 执行Sql查询功能，结果以List形式返回
     */
    public static List<Map<MappingKey, String>> execSql(Class<?> templateClz, String sql) {
        return CRUDTemplate.executeQuery(new SqlParam(sql), new IResultSetHandler() {
            @Override
            public List handle(ResultSet... resultSets) throws Exception {
                ResultSet res = resultSets[0];
                List<Map<MappingKey, String>> resultList = new LinkedList<>();
                while (res.next()) {
                    //拿到clz的模板信息
                    DBTemplate template = new DBTemplate();
                    DBMeta meta = template.parse(templateClz);
                    //根据模板信息，将查询结果填入Map中
                    Map<MappingKey, String> searchedEntity = new HashMap<>();
                    //拿到模板中的所有field
                    Map<MappingKey, Field> fieldMap = meta.getMappingKeyFieldMap();
                    Map<Field, ExcelProperty> excelPropertyMap = meta.getFieldExcelPropertyMap();
                    Map<Field, ColumnName> tableColumnMap = meta.getFieldColumnNameMap();
                    for (Field field : fieldMap.values()) {
                        //拿到其excelProperty和tableColumn注解信息
                        String propertyName = excelPropertyMap.get(field).value()[0];
                        int idx = excelPropertyMap.get(field).index();
                        String dbColName = tableColumnMap.get(field).name();
                        //映射结果存入map中
                        searchedEntity.put(new MappingKey(propertyName, idx), res.getString(dbColName));
                    }
                    resultList.add(searchedEntity);
                }
                return resultList;
            }
        });
    }

    /**
     * 映射关系
     */
    public static Map<MappingKey, String> mapColumn(Class<?> clz, List<MappingKey> excelColNameList) {
        DBTemplate dbTemplate = new DBTemplate();
        DBMeta meta = dbTemplate.parse(clz);
        //结果
        Map<MappingKey, String> resultMap = new LinkedHashMap<>();
        //从模板中提取出对应的 table列名
        Map<MappingKey, Field> map0 = meta.getMappingKeyFieldMap();
        Map<Field, ColumnName> fieldColumnNameMap = meta.getFieldColumnNameMap();
        Map<Field, ColumnInfo> fieldColumnInfoMap = meta.getFieldColumnInfoMap();
        //逐个提取
        //修改：如果index=-1就选一个同名的即可
        //这里复杂度上去了，不过一般列不会很多，所以时间不会耗时太多
        for (MappingKey mappingKey : excelColNameList) {
            if (mappingKey.getIndex() == -1) {
                //全搜索（搜到哪个是哪个）
                for (Map.Entry<MappingKey, Field> item : map0.entrySet()) {
                    if (item.getKey().getName().equals(mappingKey.getName())) {
                        Field field = item.getValue();
                        ColumnName tableColumn = fieldColumnNameMap.get(field);
                        resultMap.put(mappingKey, tableColumn.name());
                    }
                }
            } else {
                Field field = map0.get(mappingKey);
                if (field == null) {
                    //如果没有搜索到，跳过
                    continue;
                }
                ColumnName tableColumn = fieldColumnNameMap.get(field);
                resultMap.put(mappingKey, tableColumn.name());
            }
        }
        return resultMap;
    }

    /**
     * 表头信息
     */
    public static List<List<MappingKey>> head(Class<?> clz) {
        DBTemplate dbTemplate = new DBTemplate();
        DBMeta meta = dbTemplate.parse(clz);
        Map<Field, ExcelProperty> fieldExcelPropertyMap = meta.getFieldExcelPropertyMap();
        List<List<MappingKey>> result = new ArrayList<>();
        for (Map.Entry<Field, ExcelProperty> entry : fieldExcelPropertyMap.entrySet()) {
            ArrayList<MappingKey> child = new ArrayList<>();
            child.add(new MappingKey(entry.getValue().value()[0], entry.getValue().index()));
            result.add(child);
        }
        return result;
    }

    /**
     * 建表
     */
    public static DBMeta create(Class<?> clz) {
        DBTemplate t = new DBTemplate();
        DBMeta meta = t.parse(clz);
        //开始建表
        Map<Field, ColumnName> fieldColumnNameMap = meta.getFieldColumnNameMap();
        Map<Field, ColumnInfo> fieldColumnInfoMap = meta.getFieldColumnInfoMap();
        StringBuilder sb = new StringBuilder();
        sb.append("create table if not exists ")
                .append(meta.getTableName())
                .append("(\n");
        //给个默认升序id
        sb.append("id int not null auto_increment primary key,\n");
        //开始拼接建表语句
        Iterator<Map.Entry<Field, ColumnName>> iterator = fieldColumnNameMap.entrySet().iterator();
        Map.Entry<Field, ColumnName> next;
        while (iterator.hasNext()) {
            next = iterator.next();
            ColumnName columnName = next.getValue();
            ColumnInfo columnInfo = fieldColumnInfoMap.get(next.getKey());
            sb.append(columnName.name())
                    .append(" ")
                    .append(columnInfo.type())
                    .append("(")
                    .append(columnInfo.len())
                    .append(")");
            if (iterator.hasNext()) {
                sb.append(",\n");
            } else {
                sb.append(")ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;\n");
            }
        }
        String sqlStatement = sb.toString();
        long id = CRUDTemplate.executeUpdateGetKey(new SqlParam(sqlStatement));
        meta.setId(String.valueOf(id));
        return meta;
    }

    /**
     * 填充数据
     */
    public static void insert(Class<?> clz, List<Object> data) {
        StringBuilder sb = new StringBuilder();
        if (insertSqlCache.containsKey(clz)) {
            sb.append(insertSqlCache.get(clz));
        } else {
            synchronized (TableUtils.class) {
                if (insertSqlCache.containsKey(clz)) {
                    sb.append(insertSqlCache.get(clz));
                } else {
                    //拼接开始
                    //先拿到template
                    //有没有可能死锁？
                    DBTemplate t = new DBTemplate();
                    DBMeta meta = t.parse(clz);
                    sb.append("insert into ")
                            .append(meta.getTableName())
                            .append("(");
                    Map<Field, ColumnName> map = meta.getFieldColumnNameMap();
                    Iterator<Field> iterator = map.keySet().iterator();
                    Field cur;//写在外面避免内存抖动
                    while (iterator.hasNext()) {
                        cur = iterator.next();
                        ColumnName columnName = map.get(cur);
                        String colName = columnName.name();
                        sb.append(colName);
                        if (iterator.hasNext()) {
                            sb.append(",");
                        } else {
                            sb.append(")");
                        }
                    }
                    sb.append(" values(");
                    int size = map.entrySet().size();
                    for (int i = 0; i < size; i++) {
                        sb.append("?");
                        if (i < size - 1) {
                            sb.append(",");
                        } else {
                            sb.append(");");
                        }
                    }
                    //更新到sql语句缓存中
                    insertSqlCache.putIfAbsent(clz, sb.toString());
                }
            }
        }
        //数据填充
        DBTemplate t = new DBTemplate();
        DBMeta meta = t.parse(clz);
        Map<Field, ColumnName> map = meta.getFieldColumnNameMap();
        try {
            for (Object o : data) {
                tp.execute(() -> {
                    Map<Field, Object> params = new HashMap<>();
                    map.forEach(((field, columnName) -> {
                        field.setAccessible(true);
                        try {
                            params.putIfAbsent(field, field.get(o));
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    }));
                    int index = 0;
                    Object[] param = new Object[map.size()];
                    Iterator<Field> iterator = map.keySet().iterator();
                    while (iterator.hasNext()) {
                        param[index++] = params.get(iterator.next());
                    }
                    CRUDTemplate.executeUpdate(new SqlParam(sb.toString(), param));
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static List<List<Object>> getAll(Class<?> clz) {
        DBTemplate template = new DBTemplate();
        DBMeta meta = template.parse(clz);
        String tableName = meta.getTableName();

        StringBuilder sb = new StringBuilder();
        if (getAllSqlCache.containsKey(clz) && getAllColumnCache.containsKey(clz)) {
            sb.append(getAllSqlCache.get(clz));
        } else {
            Map<Field, ColumnName> map = meta.getFieldColumnNameMap();
            sb.append("select ");
            //linkedHashMap
            Iterator<Map.Entry<Field, ColumnName>> iterator = map.entrySet().iterator();
            ArrayList<String> columnName = new ArrayList<>();
            while (iterator.hasNext()) {
                Map.Entry<Field, ColumnName> next = iterator.next();
                String name = next.getValue().name();
                columnName.add(name);
                sb.append(name);
                if (iterator.hasNext()) {
                    sb.append(",");
                } else {
                    sb.append(" ");
                }
            }
            sb.append("from ");
            sb.append(tableName);
            getAllSqlCache.put(clz, sb.toString());
            getAllColumnCache.put(clz, columnName);
        }
        List<List<Object>> result = CRUDTemplate.executeQuery(new SqlParam(sb.toString()), new IResultSetHandler() {
            @Override
            public List handle(ResultSet... resultSets) throws Exception {
                ResultSet res = resultSets[0];
                List<List<Object>> list = new ArrayList<>();
                while (res.next()) {
                    ArrayList<Object> arr = new ArrayList<>();
                    List<String> columnName = getAllColumnCache.get(clz);
                    for (String s : columnName) {
                        arr.add(res.getObject(s));
                    }
                    list.add(arr);
                }
                return list;
            }
        });
        return result;
    }
}
