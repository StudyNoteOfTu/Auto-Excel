package core;

import com.alibaba.excel.read.listener.ReadListener;
import core.factories.AutoExcelFactory;
import utils.bytecode.DefaultBytecodeGenerator;
import utils.bytecode.IBytecodeGenerator;
import utils.classloader.ClassProvider;
import utils.easyexcel.EasyExcelUtil;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

/**
 * 核心入口
 *      由于EasyExcel的处理只有回调，所以这里的解析结果也将是回调设计
 *      考虑到有调用顺序：
 *          1. 拿到Excel表头
 *          2. 生成字节码 （适配Adapter）
 *          3. 解析Excel
 *          4. 得到解析结果
 *          5. 结果转换 （转换Converter）
 *          6. 结果处理
 *      其中，4,5,6均为回调。1,2,3,4 是固定流程
 *      可以考虑借鉴响应式编程RxJava的上下流方式进行事件传递
 *      上下游每一个节点通过装饰器模式进行包装，将事件放在onNext中执行，从而避免回调地狱
 *      不一定在Android平台，所以暂不默认实现线程切换
 *
 */
public abstract class AutoExcel {
    /**
     * 类加载器
     */
    private ClassProvider mClassProvider;
    /**
     * 字节码生成器【可拓展】
     */
    private IBytecodeGenerator mBytecodeGenerator;

    protected AutoExcel(){
        //initialize
        mClassProvider = classProvider();
        mBytecodeGenerator = bytecodeGenerator();
    }

    //唯一的获取方法：
    //工厂方法还需要额外配置一些属性，除了factory之外，还允许在这个get函数中传入其他配置参数
    public static <T extends AutoExcel> T get(AutoExcelFactory<T> f){
        return f.get();
    }

    protected abstract IBytecodeGenerator bytecodeGenerator();

    protected abstract ClassProvider classProvider();

    public ClassProvider getClassProvider() {
        return mClassProvider;
    }

    public IBytecodeGenerator getBytecodeGenerator() {
        return mBytecodeGenerator;
    }

    //程序入口
    //字节码处理工具
    public byte[] generate(String clzName,Map<Integer,String> map){
        mBytecodeGenerator.setName(clzName);
        return mBytecodeGenerator.generate(map);
    }

    public Class<?> findClass(String name,byte[] bytes) throws ClassNotFoundException {
        return mClassProvider.findClass(name,bytes);
    }

    public Class<?> findClass(String path, String name) throws ClassNotFoundException {
        return mClassProvider.findClass(path, name);
    }

    //ExcelUtils
    //获取表头
    public void invokeHead(InputStream is, EasyExcelUtil.ExcelHeadCallback callback){
        EasyExcelUtil.invokeHead(is,null,null,callback);
    }
    public void invokeHead(InputStream is, Integer sheetNo, EasyExcelUtil.ExcelHeadCallback callback){
        EasyExcelUtil.invokeHead(is,sheetNo,null,callback);
    }
    public void invokeHead(InputStream is, String sheetName, EasyExcelUtil.ExcelHeadCallback callback){
        EasyExcelUtil.invokeHead(is,null,sheetName,callback);
    }
    //读取Excel
    public void read(InputStream is, Class<?> templateClz, ReadListener<Object> listener){
        EasyExcelUtil.read(is,templateClz,null,null,listener);
    }
    public void read(InputStream is,  Class<?> templateClz,ReadListener<Object> listener,Integer sheetNo){
        EasyExcelUtil.read(is,templateClz,sheetNo,null,listener);
    }
    public void read(InputStream is,  Class<?> templateClz,ReadListener<Object> listener,String sheetName){
        EasyExcelUtil.read(is,templateClz,null,sheetName,listener);
    }
    //导出Excel
    public void write(OutputStream os, List<List<String>> head, List<List<Object>> content){
        EasyExcelUtil.write(os,head,content);
    }



}
