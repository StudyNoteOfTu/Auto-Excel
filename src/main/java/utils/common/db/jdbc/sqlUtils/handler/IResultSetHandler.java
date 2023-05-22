package utils.common.db.jdbc.sqlUtils.handler;


import java.sql.ResultSet;
import java.util.List;

public interface IResultSetHandler {

    List handle(ResultSet... resultSets) throws Exception;

}