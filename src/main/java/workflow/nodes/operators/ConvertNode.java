package workflow.nodes.operators;

import workflow.nodes.base.AbstractNodeWithUpstream;
import workflow.nodes.base.DownStream;
import workflow.nodes.base.ObservableSource;
import workflow.nodes.utils.Function;

public class ConvertNode<T,R> extends AbstractNodeWithUpstream<T,R> {

    Function<T,R> function;

    public ConvertNode(ObservableSource<T> source, Function<T,R> function) {
        super(source);
        this.function = function;
    }

    @Override
    protected void subscribeActual(DownStream<R> downStream) {
        source.end(new ConverterNode<>(downStream,function));
    }

    static class ConverterNode<T,R> implements DownStream<T>{

        final DownStream<R> downStream;
        final Function<T,R> converter;

        public ConverterNode(DownStream<R> downStream, Function<T, R> converter) {
            this.downStream = downStream;
            this.converter = converter;
        }

        @Override
        public void onLinked() {
            downStream.onLinked();
        }

        @Override
        public void onNext(T t) {
            if(converter!=null){
                R r = converter.block(t);
                downStream.onNext(r);
            }
        }

        @Override
        public void onComplete() {
            downStream.onComplete();
        }

        @Override
        public void onError(Throwable throwable) {
            downStream.onError(throwable);
        }
    }
}
