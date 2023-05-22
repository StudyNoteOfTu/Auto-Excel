package utils.common.db.jdbc.sqlUtils;


import utils.common.db.jdbc.sqlUtils.handler.IResultSetHandler;
import utils.common.db.jdbc.sqlUtils.sqlBean.SqlParam;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class CRUDTemplate {

    //不抽取到JDBCUtil中，因为那个util是直接和数据库连接的操作，最好不要混到一起
    public static int executeUpdateGetKey(SqlParam sqlParam){
        Connection conn = null;
        PreparedStatement ps = null;
        String sql = sqlParam.getSql();
        Object[] params = sqlParam.getParams();
        int id = 0;
        try {
            //1.获得数据库连接
            conn = JdbcUtil.getConn();
            //2.创建语句
            ps =  conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            //3.设置参数
            for (int i = 0 ; i < params.length ; i ++){
                //下标从1开始
                ps.setObject(i+1,params[i]);
            }
            //4.执行sql
            ps.executeUpdate();
            ResultSet res = ps.getGeneratedKeys();
            if (res.next()){
                id = res.getInt(1);
                System.out.println("CRUDTemplate update one ,with getKey: id = " +id);
            }
        }catch(Exception e){
            e.printStackTrace();
        }finally {
            JdbcUtil.close(conn,null,ps);
        }
        return id;
    }

    /**
     * save delete update 方法有重复方法，抽出来
     * 1.设计一个方法
     * 2.要求传入两个参数
     * 一个sql语句
     * 一个参数 第一个参数为sql语句末班，第二个为可变参数设置语句参数值 本质 数组
     * 3.返回值为int，受影响的行数
     */
    public static int executeUpdate(SqlParam sqlParam){
        Connection conn = null;
        PreparedStatement ps = null;
        String sql = sqlParam.getSql();
        Object[] params = sqlParam.getParams();
        try {
            //1.获得数据库连接
            conn = JdbcUtil.getConn();
            //2.创建语句
            ps =  conn.prepareStatement(sql);
            //3.设置参数
            for (int i = 0 ; i < params.length ; i ++){
                //下标从1开始
                ps.setObject(i+1,params[i]);
            }
            //4.执行sql
            return ps.executeUpdate();
        }catch(Exception e){
            e.printStackTrace();
        }finally {
            JdbcUtil.close(conn,null,ps);
        }
        return 0;
    }

    public static int executeUpdateTables(SqlParam... sqlParams){
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet res = null;

        try {
            conn = JdbcUtil.getConn();

            SqlParam sqlParam;
            String sql;
            Object[] params;
            for (int i = 0; i < sqlParams.length; i++){
                sqlParam = sqlParams[i];
                sql = sqlParam.getSql();
                params = sqlParam.getParams();
                ps = conn.prepareStatement(sql);
                for (int j = 0; j < params.length; j++) {
                    ps.setObject(j+1,params[j]);
                }
                ps.executeUpdate();
            }
        }catch(Exception e){
            e.printStackTrace();
        }finally {
            JdbcUtil.close(conn,res,ps);
        }
        return 0;
    }

    public static List executeQuery(SqlParam sqlParam, IResultSetHandler handler){
        List list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet res = null;

        String sql = sqlParam.getSql();
        Object[] params = sqlParam.getParams();
        try {
            conn = JdbcUtil.getConn();

            ps = conn.prepareStatement(sql);

            for (int i =0 ; i < params.length ; i++){
                ps.setObject(i+1,params[i]);
            }

            res =  ps.executeQuery();
            list = handler.handle(res);
        }catch(Exception e){
            e.printStackTrace();
        }finally {
            JdbcUtil.close(conn,res,ps);
        }
        return list;
    }


    public static List executeQueryTables(IResultSetHandler handler, SqlParam... sqlParams){
        List list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet res = null;

        ResultSet[] resultSets = new ResultSet[sqlParams.length];
        try {
            conn = JdbcUtil.getConn();

            SqlParam sqlParam;
            String sql;
            Object[] params;
            for (int i = 0; i < sqlParams.length; i++){
                sqlParam = sqlParams[i];
                sql = sqlParam.getSql();
                params = sqlParam.getParams();
                ps = conn.prepareStatement(sql);
                for (int j = 0; j < params.length; j++) {
                    ps.setObject(j+1,params[j]);
                }
                resultSets[i] = ps.executeQuery();
            }
            list = handler.handle(resultSets);
        }catch(Exception e){
            e.printStackTrace();
        }finally {
            JdbcUtil.close(conn,res,ps);
        }
        return list;
    }
}

