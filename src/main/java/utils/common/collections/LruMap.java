package utils.common.collections;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * LruMap,最少最近使用原则 进行缓存处理
 */
public class LruMap<T,R> extends LinkedHashMap<T,R> {
    int capacity;

    public LruMap(int initialCapacity) {
        super(initialCapacity+1,1.0f,true);
        this.capacity = initialCapacity;
    }

    //超过了就删掉最少访问的
    @Override
    protected boolean removeEldestEntry(Map.Entry<T, R> eldest) {
        return size()>capacity;
    }

    @Override
    public R remove(Object key) {
        //截获删除的节点，从其他map中把它也删掉
        //其实也不用截获，它本身被删除之后就没有引用了
        //如果还有引用，在这里要做后续断掉引用的操作，避免内存泄漏
        return super.remove(key);
    }

}
