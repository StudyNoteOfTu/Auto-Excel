package workflow.nodes.base;

public interface DownStream<T> {

    //建立订阅关系,与上游建立关系
    void onLinked();

    //事件处理
    void onNext(T t);

    void onComplete();

    void onError(Throwable throwable);

}
