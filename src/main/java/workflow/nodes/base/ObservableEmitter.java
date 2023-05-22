package workflow.nodes.base;

/**
 * 事件起源
 */
public interface ObservableEmitter<T> {
    void emit(Emitter<T> emitter);
}
