import core.factories.DBAutoExcelFactory;
import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import template.annotations.db.MappingKey;
import utils.common.FileUtil;
import utils.common.db.TableUtils;
import utils.easyexcel.EasyExcelUtil;
import utils.easyexcel.ExcelReadListener;
import core.AutoExcel;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public class DemoModel {

    //测试
    public static void main(String[] args) {
        DemoModel model = new DemoModel();
        System.out.println("---------代码预热-------------");
        String xlsPath0 = "C:\\Users\\fengyitu\\Downloads\\patient_list.xls";
        File xlsFile0 = new File(xlsPath0);
        String clzPath0 = "D:\\A后端毕设\\字节码测试\\3";
        String clzName0 = "preTestClz";
        model.preTest(xlsFile0, clzPath0, clzName0);

        System.out.println("---------测试导入，第一轮-------------");
        //1. 导入
        String xlsPath = "C:\\Users\\fengyitu\\Downloads\\patient_list.xls";
        File xlsFile = new File(xlsPath);
        String clzPath = "D:\\A后端毕设\\字节码测试\\2";
        String clzName = "clz20230523";

        String xlsPath2 = "C:\\Users\\fengyitu\\Downloads\\patient_list - 副本.xls";
        File xlsFile2 = new File(xlsPath2);
        String clzPath2 = "D:\\A后端毕设\\字节码测试\\2";
        String clzName2 = "clz20230523_second";
        try {
            System.out.println("---------导入文件1-------------");
            model.excel2table(xlsFile, clzPath, clzName);
            System.out.println("---------导入文件2-------------");
            model.excel2table(xlsFile2, clzPath2, clzName2);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("---------测试导入，第二轮------------");
        //1. 导入
        try {
            System.out.println("---------再次导入文件1-------------");
            model.excel2table(xlsFile, clzPath, clzName);
            System.out.println("---------再次导入文件2-------------");
            model.excel2table(xlsFile2, clzPath2, clzName2);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //2. 导出
        try {
            System.out.println("---------导出文件1-------------");
            File file = FileUtil.newFile(clzPath, "导出原文件1", FileUtil.Suffix.XLS);
            model.table2excel(file, clzPath, clzName);
            System.out.println("---------导出文件2-------------");
            File file2 = FileUtil.newFile(clzPath2, "导出原文件2", FileUtil.Suffix.XLS);
            model.table2excel(file2, clzPath2, clzName2);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //完整示例 - 默认实现：
    public void preTest(File file, String path, String name) {
        AtomicLong time = new AtomicLong(System.currentTimeMillis());
        //1.拿到Excel表头
        DBAutoExcelFactory.DBAutoExcel autoExcel = AutoExcel.get(new DBAutoExcelFactory());
        Disposable subscribe = Observable.create((ObservableOnSubscribe<Map<Integer, String>>) emitter -> {
            time.set(System.currentTimeMillis());
            autoExcel.invokeHead(Files.newInputStream(file.toPath()), emitter::onNext);
        }).map(map -> {
            //2. 生成字节码
            time.set(System.currentTimeMillis());
            return autoExcel.generate(name, map);
        }).map(bytes -> {
            time.set(System.currentTimeMillis());
            //3.文件存储
            FileUtil.storeFile(bytes, path, name, FileUtil.Suffix.CLASS);
            time.set(System.currentTimeMillis());
            //4.类加载，字节码转为类对象
            return autoExcel.findClass(name, bytes);
        }).subscribe((Consumer<Class<?>>) clz -> {
            //读取数据
            try {
                EasyExcelUtil.read(Files.newInputStream(file.toPath()), clz, new ExcelReadListener(list -> {

                }));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * 解析事件流，1,2,3,4是默认实现，故而这里给到默认实现
     */
    public void excel2table(File file, String path, String name) {
        AtomicLong time = new AtomicLong(System.currentTimeMillis());
        //1.拿到Excel表头
        DBAutoExcelFactory.DBAutoExcel autoExcel = AutoExcel.get(new DBAutoExcelFactory());
        Disposable subscribe = Observable.create((ObservableOnSubscribe<Map<Integer, String>>) emitter -> {
            time.set(System.currentTimeMillis());
            autoExcel.invokeHead(Files.newInputStream(file.toPath()), emitter::onNext);
        }).map(map -> {
            System.out.println("step1,拿到Excel表头，耗时：" + (System.currentTimeMillis() - time.get()) + "ms");
            //2. 生成字节码
            time.set(System.currentTimeMillis());
            byte[] generate = autoExcel.generate(name, map);
            System.out.println("step2,动态生成字节码，耗时：" + (System.currentTimeMillis() - time.get()) + "ms");
            return generate;
        }).map(bytes -> {
            time.set(System.currentTimeMillis());
            //3.文件存储
            FileUtil.storeFile(bytes, path, name, FileUtil.Suffix.CLASS);
            System.out.println("step3,文件存储,耗时：" + (System.currentTimeMillis() - time.get()) + "ms");
            time.set(System.currentTimeMillis());
            //4.类加载，字节码转为类对象
            Class<?> aClass = autoExcel.findClass(name, bytes);
            System.out.println("step4,类加载，字节码转为类对象,耗时：" + (System.currentTimeMillis() - time.get()) + "ms");
            return aClass;
        }).map(clz -> {
            //5.建表
            time.set(System.currentTimeMillis());
            try {
                TableUtils.create(clz);
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("step5,建表，耗时：" + (System.currentTimeMillis() - time.get()) + "ms");
            return clz;
        }).subscribe((Consumer<Class<?>>) clz -> {
            System.out.println("step6，读取数据，插入到数据库");
            //读取数据
            try {
                EasyExcelUtil.read(Files.newInputStream(file.toPath()), clz, new ExcelReadListener(list -> {
                    //读取到一部分数据，插入到数据库
                    TableUtils.insert(clz, list);
                }));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public void table2excel(File file, String path, String name) {
        DBAutoExcelFactory.DBAutoExcel autoExcel = AutoExcel.get(new DBAutoExcelFactory());
        Disposable subscribe = Observable.create((ObservableOnSubscribe<Class<?>>) observableEmitter -> {
            System.out.println("step1,拿到字节码");
            Class<?> clz = autoExcel.findClass(path, name);
            observableEmitter.onNext(clz);
        }).subscribe(clz -> {
            System.out.println("step2,获取头信息");
            List<List<MappingKey>> origin = autoExcel.head(clz);
            List<List<String>> head = new ArrayList<>();
            for (List<MappingKey> mappingKeys : origin) {
                head.add(Collections.singletonList(mappingKeys.get(0).getName()));
            }
            try {
                //获取数据
                System.out.println("step3,获取数据");
                List<List<Object>> content = autoExcel.getAll(clz);
                //写入文件
                System.out.println("step4,写入文件");
                OutputStream outputStream = Files.newOutputStream(file.toPath());
                autoExcel.write(outputStream, head, content);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });


    }

}
