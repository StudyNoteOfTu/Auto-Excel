package utils.common.db.jdbc.sqlUtils.sqlBean;


import java.util.Arrays;

public class SqlParam {


    String sql;

    Object[] params;

    public SqlParam(String sql, Object... params) {
        this.sql = sql;
        this.params = params;
    }


    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    public Object[] getParams() {
        return params;
    }

    public void setParams(Object[] params) {
        this.params = params;
    }

    @Override
    public String toString() {
        return "SqlParam{" +
                "sql='" + sql + '\'' +
                ", params=" + Arrays.toString(params) +
                '}';
    }

}
