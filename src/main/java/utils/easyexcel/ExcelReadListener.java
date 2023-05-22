package utils.easyexcel;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;

import java.util.ArrayList;
import java.util.List;

public class ExcelReadListener extends AnalysisEventListener<Object> {

    public interface InvokeCallback{
        void onInvoke(List<Object> list);
    }

    final InvokeCallback callback;

    //这个次数没关系了，因为我们做了缓存
    private static final int BATCH_COUNT = 20;
    private List<Object> cachedDataList = new ArrayList<>();

    public ExcelReadListener(InvokeCallback callback) {
        this.callback = callback;
    }

    @Override
    public void invoke(Object o, AnalysisContext analysisContext) {
        cachedDataList.add(o);
        if (cachedDataList.size() >= BATCH_COUNT) {
            saveData();
            //清空缓存
            cachedDataList = new ArrayList<>(BATCH_COUNT);
        }
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
        saveData();
    }

    private void saveData(){
        callback.onInvoke(cachedDataList);
    }
}
