package utils.easyexcel;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.excel.metadata.CellData;
import com.alibaba.excel.read.builder.ExcelReaderBuilder;
import com.alibaba.excel.read.listener.ReadListener;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

/**
 * EasyExcel工具类，调用EasyExcel的解析接口
 */
public class EasyExcelUtil {

    public interface ExcelHeadCallback {
        void onSuccess(Map<Integer, String> headMap);
    }

    /**
     * 获取头信息,向上回调
     */
    public static void invokeHead(InputStream is, ExcelHeadCallback callback){
        invokeHead(is,null,null,callback);
    }
    public static void invokeHead(InputStream is,Integer sheetNo,ExcelHeadCallback callback){
        invokeHead(is,sheetNo,null,callback);
    }
    public static void invokeHead(InputStream is,String sheetName,ExcelHeadCallback callback){
        invokeHead(is,null,sheetName,callback);
    }
    public static void invokeHead(InputStream is,Integer sheetNo,String sheetName, ExcelHeadCallback callback) {
        if (callback == null) return;
        ExcelReaderBuilder read = EasyExcel.read(is, new AnalysisEventListener<Object>() {
            @Override
            public void invokeHeadMap(Map<Integer, String> headMap, AnalysisContext context) {
                super.invokeHeadMap(headMap, context);
                callback.onSuccess(headMap);
            }

            @Override
            public void invokeHead(Map<Integer, CellData> headMap, AnalysisContext context) {
                super.invokeHead(headMap,context);
            }

            @Override
            public void invoke(Object o, AnalysisContext analysisContext) {

            }

            @Override
            public void doAfterAllAnalysed(AnalysisContext analysisContext) {

            }
        });
        if (sheetNo!= null){
            read.sheet(sheetNo).doRead();
        }else if(sheetName != null){
            read.sheet(sheetName).doRead();
        }else{
            read.sheet().doRead();
        }
    }

    /**
     * 读取Excel文件内容
     */
    public static void read(InputStream is,  Class<?> templateClz,ReadListener<Object> listener){
        read(is,templateClz,null,null,listener);
    }
    public static void read(InputStream is,  Class<?> templateClz,ReadListener<Object> listener,Integer sheetNo){
        read(is,templateClz,sheetNo,null,listener);
    }
    public static void read(InputStream is,  Class<?> templateClz,ReadListener<Object> listener,String sheetName){
        read(is,templateClz,null,sheetName,listener);
    }
    public static void read(InputStream is, Class<?> templateClz, Integer sheetNo,String sheetName,ReadListener<Object> listener){
        ExcelReaderBuilder read = EasyExcel.read(is, templateClz, listener);
        if (sheetNo!= null){
            read.sheet(sheetNo).doRead();
        }else if(sheetName != null){
            read.sheet(sheetName).doRead();
        }else{
            read.sheet().doRead();
        }
    }

    /**
     * 导出Excel
     */
    public static void write(OutputStream os,List<List<String>> head,List<List<Object>> content){
        try {
            EasyExcel.write(os)
                    .head(head)
                    .sheet("template")
                    .doWrite(content);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
