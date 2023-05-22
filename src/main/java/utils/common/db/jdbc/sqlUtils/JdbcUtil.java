package utils.common.db.jdbc.sqlUtils;


import com.alibaba.druid.pool.DruidDataSourceFactory;

import javax.sql.DataSource;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Properties;

//必须要配置 db.properties 才能使用
public class JdbcUtil {


    //连接池对象
    public static DataSource ds = null;

    static{
//        //只需要执行一次,静态字段按顺序加载
        //连接池
        try{
            //1.加载配置文件
            Properties p = new Properties();
//            FileInputStream fileInputStream = new FileInputStream("db.properties");
            InputStream fileInputStream = JdbcUtil.class.getClassLoader().getResourceAsStream("db.properties");
            p.load(fileInputStream);
            //注意这里的配置文件是内容按需求格式写的
            ds = DruidDataSourceFactory.createDataSource(p);

        }catch (Exception e){
            e.printStackTrace();
        }
    }


    /**
     * 连接
     * @return 数据库连接
     */
    public static Connection getConn(){
        Connection conn = null;
        try{
            //获取连接对象
//            conn =  DriverManager.getConnection(JdbcUtil.url, JdbcUtil.user, JdbcUtil.password);
            conn = ds.getConnection();
        }catch (Exception e){
            e.printStackTrace();
        }
        return conn;
    }


    /**
     * 释放资源
     * @param conn 连接
     * @param res ResultSet
     * @param st Statement
     */
    public static void close(Connection conn , ResultSet res, Statement st){
        //释放资源
        if (res != null){
            try{
                res.close();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        if (st !=null){
            try{
                st.close();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        if (conn !=null){
            try{
                conn.close();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

}