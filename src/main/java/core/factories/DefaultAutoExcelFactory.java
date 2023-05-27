package core.factories;

import core.AutoExcel;
import utils.bytecode.BytecodeGenerator;
import utils.bytecode.DefaultBytecodeGenerator;
import utils.bytecode.IBytecodeGenerator;
import utils.classloader.ClassProvider;

public class DefaultAutoExcelFactory extends AutoExcelFactory<DefaultAutoExcelFactory.DefaultAutoExcel> {
    @Override
    public DefaultAutoExcel get() {
        return new DefaultAutoExcel();
    }

    public static class DefaultAutoExcel extends AutoExcel{
        @Override
        protected IBytecodeGenerator bytecodeGenerator() {
            return new DefaultBytecodeGenerator();
        }

        @Override
        protected ClassProvider classProvider() {
            return new ClassProvider(AutoExcel.class.getClassLoader());
        }
    }
}
