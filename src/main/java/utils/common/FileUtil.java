package utils.common;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * 文件处理工具，可用于字节码保存
 */
public class FileUtil {


    public interface Suffix {
        String CLASS = ".class";
        String XLS = ".xls";
    }

    public static File newFile(String path, String name, String suffix) throws IOException {
        try {
            File dir = new File(path);
            if (!dir.exists()) {
                boolean mkdirs = dir.mkdirs();
            }
            File file = new File(path + File.separator + name + suffix);
            boolean newFile = file.createNewFile();
            return file;
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        }
    }


    public static void storeFile(byte[] bytes, String path, String name, String suffix) {
        try {
            File dir = new File(path);
            if (!dir.exists()) {
                boolean mkdirs = dir.mkdirs();
            }
//            File file = new File(path+File.separator+name+".class");
            File file = new File(path + File.separator + name + suffix);
            boolean newFile = file.createNewFile();
            FileOutputStream fos = new FileOutputStream(path + File.separator + name + suffix);
            fos.write(bytes);
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
