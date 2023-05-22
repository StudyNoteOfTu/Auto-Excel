package workflow.nodes.operators;

import workflow.nodes.base.Emitter;
import workflow.nodes.base.DownStream;
import workflow.nodes.base.ObservableEmitter;
import workflow.nodes.base.Upstream;

/**
 * invoke head
 * 获取Excel表头的工作节点，作为一个上流，继承自Upstream
 * 其他的工作节点作为流中间节点，继承自WorkNode
 */
public class EmitNode<T> extends Upstream<T> {

    final ObservableEmitter<T> emitter;

    public EmitNode(ObservableEmitter<T> emitter) {
        this.emitter = emitter;
    }

    @Override
    protected void subscribeActual(DownStream<T> downStream) {
        //将事件起源交出去，由Emitter完成
        //与上游建立关系完成
        downStream.onLinked();
        //创建一个Emitter，将downstream给到事件起源
        SourceEmitter<T> source = new SourceEmitter<>(downStream);
        //事件起源启动
        emitter.emit(source);
    }

    static class SourceEmitter<T> implements Emitter<T>{
        //下游
        final DownStream<T> downStream;
        //onError 与 onComplete 互斥
        volatile boolean done;

        public SourceEmitter(DownStream<T> downStream) {
            this.downStream = downStream;
        }

        @Override
        public void onNext(T t) {
            if (done)return;
            downStream.onNext(t);
        }

        @Override
        public void onComplete() {
            if(done) return;
            downStream.onComplete();
            done = true;
        }

        @Override
        public void onError(Throwable throwable) {
            if (done)return;
            downStream.onError(throwable);
            done = true;
        }
    }
}
