package workflow.nodes.utils;

public interface Function<T,R> {
    R block(T t);
}
