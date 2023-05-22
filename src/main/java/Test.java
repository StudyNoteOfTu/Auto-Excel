import utils.bytecode.DBBytecodeGenerator;
import utils.common.FileUtil;
import workflow.main.AutoExcel;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;

public class Test {
    public static void main(String[] args) throws Exception {
        String clzName = "test";
        AutoExcel autoExcel = new AutoExcel.Builder().bytecodeGenerator(new DBBytecodeGenerator(clzName)).build();
        File file = new File("C:\\Users\\fengyitu\\Downloads\\patient_list.xls");
        autoExcel.excel2table(file,"D:\\A后端毕设\\字节码测试\\2",clzName,clzName);
        File file1 = FileUtil.newFile("D:\\A后端毕设\\字节码测试\\2", "downloadtest", FileUtil.Suffix.XLS);
        autoExcel.table2excel(file1,"D:\\A后端毕设\\字节码测试\\2",clzName,clzName);
    }
}
