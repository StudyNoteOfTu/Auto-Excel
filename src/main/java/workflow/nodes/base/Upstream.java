package workflow.nodes.base;

import workflow.nodes.operators.ConvertNode;
import workflow.nodes.operators.EmitNode;
import workflow.nodes.utils.Function;

public abstract class Upstream<T> implements ObservableSource<T> {
    /**
     * 作为中间的流节点，只有向上流订阅的关系
     */
    @Override
    public void end(DownStream<T> downStream) {
        subscribeActual(downStream);
    }
    //模板方法
    protected abstract void subscribeActual(DownStream<T> downStream);

    //发射器，流式调度入口
    public static <T> Upstream<T> create(ObservableEmitter<T> source){
        return new EmitNode<>(source);
    }

    //非程序入口，中间节点
    public Upstream<T> flow(Function<T, T> function){
        return new ConvertNode<>(this,function);
    }

    //转换工具
    public <R> ConvertNode<T,R> convert(Function<T, R> function){
        return new ConvertNode<>(this,function);
    }
}
