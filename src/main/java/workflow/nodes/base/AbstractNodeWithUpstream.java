package workflow.nodes.base;

public abstract class AbstractNodeWithUpstream<T,R> extends Upstream<R> {
    //装饰器模式，持有基本类
    protected final ObservableSource<T> source;

    protected AbstractNodeWithUpstream(ObservableSource<T> source) {
        this.source = source;
    }
}
