package utils.classloader;

import java.io.File;
import java.io.FileInputStream;

/**
 * 提供Class类
 *      提供Class二进制数据流
 *      提供Class本身
 *      提供Class字节码存储路径
 * 最终统一对内提供Class实体
 */
public class ClassProvider extends ClassLoader{

    //传入一个系统内的ClassLoader
    public ClassProvider(ClassLoader parent) {
        super(parent);
    }

    /**
     * 加载字节码的二进制数据
     * @param path 文件所在父路径（注意是全类名之上的父路径）
     * @param name 全类名转为文件名  com.company.User->com/company/User
     */
    private byte[] loadByte(String path, String name) throws Exception {
        name = name.replaceAll("\\.", "/");
        FileInputStream fis = new FileInputStream(path + File.separator + name + ".class");
        int len = fis.available();
        byte[] data = new byte[len];
        fis.read(data);
        fis.close();
        return data;
    }

    /**
     * 根据二进制数据进行加载
     */
    public Class<?> findClass(String name,byte[] bytes) throws ClassNotFoundException {
        try{
            Class<?> aClass = findLoadedClass(name);
            if (aClass!=null)return aClass;
            return defineClass(name,bytes,0,bytes.length);
        }catch (Exception e){
            e.printStackTrace();
            throw new ClassNotFoundException();
        }
    }

    /**
     * 根据路径进行加载，生成Class<>对象
     * @param path 父路径
     * @param name 全类名
     * @return Class对象
     */
    public Class<?> findClass(String path, String name) throws ClassNotFoundException {
        try {
            //查看该字节码之前是否加载过
            Class<?> aClass = findLoadedClass(name);
            if (aClass!=null)return aClass;
            byte[] data = loadByte(path, name);
            return defineClass(name, data, 0, data.length);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ClassNotFoundException();
        }
    }

    @Override
    @Deprecated
    protected Class<?> findClass(String name) throws RuntimeException{
        try {
            return super.findClass(name);
        } catch (ClassNotFoundException e) {
        }
        return null;
    }
}
