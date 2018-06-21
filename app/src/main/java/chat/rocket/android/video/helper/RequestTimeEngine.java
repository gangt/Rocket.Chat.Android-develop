package chat.rocket.android.video.helper;

import android.annotation.SuppressLint;

import java.util.concurrent.TimeUnit;

import chat.rocket.android.log.RCLog;
import chat.rocket.android.video.presenter.BasePresenter;
import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by Administrator on 2018/5/3/003.
 */

public class RequestTimeEngine {

    private BasePresenter presenter ;
    private  Disposable disposable ;
    private static  RequestTimeEngine instance ;

    private RequestTimeEngine(){}

    public static RequestTimeEngine getInstance(){
        if(instance == null){
            instance = new RequestTimeEngine();
        }
        return instance ;
    }

    public void attatchData(BasePresenter presenter) {
        this.presenter = presenter;
    }

    public void stopIfNeccessary(){
        if(disposable !=null && !disposable.isDisposed()){
            RCLog.d("come--------->>>>"+ " RequestTimeEngine  stop ");
            disposable.dispose();
            disposable = null ;
        }
    }

    @SuppressLint({"RxLeakedSubscription", "RxSubscribeOnError"})
    public void start(){
         disposable = Flowable.interval(0, 1, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .onBackpressureBuffer()
                .subscribe(aLong -> {
                    RCLog.d("come--------->>>>"+ ": "+aLong);

                    if (aLong == 30) {

                        presenter.remindCall(30);
                    }
                    if(aLong == 55){
                        presenter.remindCall(55);
                    }
                    if (aLong == 60) {
                        presenter.remindCall(60);
                        disposable.dispose();
                    }
                });
    }

}
