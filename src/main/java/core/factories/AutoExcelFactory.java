package core.factories;

import core.AutoExcel;

public abstract class AutoExcelFactory<T extends AutoExcel> {
    public abstract T get();
}
