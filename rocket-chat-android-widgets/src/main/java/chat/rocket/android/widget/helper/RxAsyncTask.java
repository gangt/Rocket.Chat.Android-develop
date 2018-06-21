package chat.rocket.android.widget.helper;

/**
 * Created by jumper on 2018/3/27.
 */

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * Author KINCAI
 * .
 * description TODO
 * .
 * Time 2016-12-31 23:16
 */
public abstract class RxAsyncTask<Param, Progress, Result> {

    private final String TAG = this.getClass().getSimpleName();
    private Flowable<Progress[]> mFlowable2;

    public RxAsyncTask() {
    }

    @SafeVarargs
    private final void rxTask(final Param... params) {

        Flowable.create(new FlowableOnSubscribe<Result>() {
            @Override
            public void subscribe(FlowableEmitter<Result> e) throws Exception {
                e.onNext(RxAsyncTask.this.call(params));
                e.onComplete();
            }
        }, BackpressureStrategy.BUFFER).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Result>() {
                    @Override
                    public void onSubscribe(Subscription s) {
                        s.request(Long.MAX_VALUE);
                    }

                    @Override
                    public void onNext(Result result) {
                        RxAsyncTask.this.onResult(result);
                    }

                    @Override
                    public void onError(Throwable t) {
                        RxAsyncTask.this.onError(t);
                    }

                    @Override
                    public void onComplete() {
                        RxAsyncTask.this.onCompleted();
                    }
                });
    }

    protected abstract Result call(Param... params);

    /**任务开始之前调用(在当前调用者所在线程执行)*/
    protected void onPreExecute() {
    }

    /** 执行结果返回*/
    protected void onResult(Result result) {
    }

    /**进度更新*/
    protected void onProgressUpdate(Progress... progresses) {
    }

    /**RxJava中的onComplete回调*/
    protected void onCompleted() {
    }
    /**RxJava中的onError回调*/
    protected void onError(Throwable e) {
    }

    /**进度更新 子线程转主线程 回调给 onProgressUpdate()方法*/
    protected void publishProgress(final Progress... progresses) {
        if (mFlowable2 == null) {
            mFlowable2 = Flowable.create(new FlowableOnSubscribe<Progress[]>() {
                @Override
                public void subscribe(FlowableEmitter<Progress[]> e) throws Exception {
                    e.onNext(progresses);
                }
            }, BackpressureStrategy.BUFFER).observeOn(AndroidSchedulers.mainThread());
        }

        mFlowable2.subscribe(new Consumer<Progress[]>() {
            @Override
            public void accept(Progress[] progress) throws Exception {
                onProgressUpdate(progresses);
            }
        });

    }

    @SafeVarargs
    public final void execute(Param... params) {
        onPreExecute();
        rxTask(params);
    }
}
