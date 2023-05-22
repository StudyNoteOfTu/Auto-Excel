package workflow.main;

import template.annotations.db.MappingKey;
import utils.bytecode.DBBytecodeGenerator;
import utils.bytecode.DefaultBytecodeGenerator;
import utils.bytecode.IBytecodeGenerator;
import utils.classloader.ClassProvider;
import utils.common.FileUtil;
import utils.common.db.TableUtils;
import utils.easyexcel.EasyExcelUtil;
import utils.easyexcel.ExcelReadListener;
import workflow.nodes.base.*;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
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
public class AutoExcel {
    /**
     * 类加载器
     */
    private ClassProvider mClassProvider;
    /**
     * 字节码生成器【可拓展】
     */
    private IBytecodeGenerator mBytecodeGenerator;

    //---------------构建者---------------------
    public static class Builder{
        AutoExcel instance;

        ClassProvider classProvider;
        IBytecodeGenerator bytecodeGenerator;

        public Builder(){
            instance = new AutoExcel();
        }

        public Builder bytecodeGenerator(IBytecodeGenerator b){
            this.bytecodeGenerator = b;
            return this;
        }

        public Builder classProvider(ClassProvider c){
            this.classProvider = c;
            return this;
        }

        public AutoExcel build(){
            instance.mClassProvider = classProvider == null? new ClassProvider(this.getClass().getClassLoader()):classProvider;
            instance.mBytecodeGenerator = bytecodeGenerator == null? new DefaultBytecodeGenerator("default"):bytecodeGenerator;
            return instance;
        }
    }

    //示例 - 默认实现：
    /**
     * 解析事件流，1,2,3,4是默认实现，故而这里给到默认实现
     */
    public void excel2table(File file, String path, String name, String clzName) throws Exception{
        //1.拿到Excel表头
        Upstream.create((ObservableEmitter<Map<Integer, String>>) emitter -> {
            //事件起源，获取表头信息
            //拿到了表头信息，向下传递
            System.out.println("step1,拿到Excel表头");
            try {
                EasyExcelUtil.invokeHead(Files.newInputStream(file.toPath()), emitter::onNext);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).convert(t->{
            System.out.println("step2,生成字节码");
            //2. 生成字节码
            if (!(mBytecodeGenerator instanceof DBBytecodeGenerator)){
                throw new RuntimeException("excel2table function needs DBBytecodeGenerator!");
            }
            return mBytecodeGenerator.generate(t);
        }).convert(bytes -> {
            System.out.println("step3,文件存储");
            //3.文件存储
            FileUtil.storeFile(bytes,path,name,FileUtil.Suffix.CLASS);
            System.out.println("step4,类加载，字节码转为类对象");
            //4.类加载，字节码转为类对象
            ClassProvider provider = mClassProvider;
            Class clz = null;
            try {
                clz = provider.findClass(clzName,bytes);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            return clz;
        }).flow(clz->{
            //5.建表
            System.out.println("step5,建表");
            try {
                TableUtils.create(clz);
            }catch (Exception e){
                e.printStackTrace();
            }
            return clz;
        }).end(new EndFlow<Class>(){
            @Override
            public void onNext(Class clz) {
                System.out.println("step6，读取数据，插入到数据库");
                //读取数据
                try {
                    EasyExcelUtil.read(Files.newInputStream(file.toPath()),clz,new ExcelReadListener(list->{
                        //读取到一部分数据，插入到数据库
                        TableUtils.insert(clz,list);
                    }));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }


    public void table2excel(File file,String path, String name, String clzName){
        //1.拿到字节码
        Upstream.create((ObservableEmitter<Class<?>>) emitter -> {
            System.out.println("step1,拿到字节码");
            ClassProvider provider = mClassProvider;
            Class clz = null;
            try {
                clz = provider.findClass(path,name);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            emitter.onNext(clz);
        }).end(new EndFlow<Class<?>>() {
            @Override
            public void onNext(Class<?> clz) {
                //2.获取头信息
                System.out.println("step2,获取头信息");
                List<List<MappingKey>> origin = TableUtils.head(clz);
                List<List<String>> head = new ArrayList<>();
                for (List<MappingKey> mappingKeys : origin) {
                    head.add(Collections.singletonList(mappingKeys.get(0).getName()));
                }
                try {
                    //获取数据
                    System.out.println("step3,获取数据");
                    List<List<Object>> content = TableUtils.getAll(clz);
                    //写入文件
                    System.out.println("step4,写入文件");
                    OutputStream outputStream = Files.newOutputStream(file.toPath());
                    EasyExcelUtil.write(outputStream,head,content);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

    }


}
