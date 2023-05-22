package workflow.nodes.base;

public interface ObservableSource<T> {
    //最后一层的订阅关系
    void end(DownStream<T> downStream);
}
