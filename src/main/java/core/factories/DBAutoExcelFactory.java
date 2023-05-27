package core.factories;

import core.AutoExcel;
import template.annotations.db.MappingKey;
import template.meta.BaseMeta;
import template.meta.DBMeta;
import utils.bytecode.DBBytecodeGenerator;
import utils.bytecode.IBytecodeGenerator;
import utils.classloader.ClassProvider;
import utils.common.db.TableUtils;

import java.util.List;
import java.util.Map;

public class DBAutoExcelFactory extends AutoExcelFactory<DBAutoExcelFactory.DBAutoExcel> {
    @Override
    public DBAutoExcel get() {
        return new DBAutoExcel();
    }

    public static class DBAutoExcel extends AutoExcel{
        @Override
        protected IBytecodeGenerator bytecodeGenerator() {
            return new DBBytecodeGenerator();
        }

        @Override
        protected ClassProvider classProvider() {
            return new ClassProvider(AutoExcel.class.getClassLoader());
        }

        @Override
        public byte[] generate(String clzName, Map<Integer, String> map) {
            return super.generate(clzName, map);
        }

        //额外增加数据库相关操作:

        public void execSql(Class<?> clz,String sql){
            TableUtils.execSql(clz,sql);
        }

        public Map<MappingKey, String> mapColumn(Class<?> clz, List<MappingKey> excelColNameList){
            return TableUtils.mapColumn(clz,excelColNameList);
        }

        public List<List<MappingKey>> head(Class<?> clz){
            return TableUtils.head(clz);
        }

        public DBMeta create(Class<?> clz){
            return TableUtils.create(clz);
        }

        public void insert(Class<?> clz, List<Object> data){
            TableUtils.insert(clz,data);
        }

        public List<List<Object>> getAll(Class<?> clz) {
            return TableUtils.getAll(clz);
        }

    }
}
