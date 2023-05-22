package workflow.nodes.base;

public abstract class EndFlow<T> implements DownStream<T> {
    @Override
    public void onLinked() {

    }

    @Override
    public void onComplete() {

    }

    @Override
    public void onError(Throwable throwable) {

    }
}
