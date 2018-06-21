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

public class ReInvitedTimeEngine {

    private BasePresenter presenter ;
    private  Disposable disposable ;
    private int limitTime ;
    private static ReInvitedTimeEngine instance ;

    private ReInvitedTimeEngine(){}

    public  static ReInvitedTimeEngine getInstance(){
        if(instance == null){
            instance = new ReInvitedTimeEngine();
        }
        return  instance ;

    }

    public void bindData(BasePresenter presenter, int limitTime) {
        this.presenter = presenter;
        this.limitTime = limitTime ;
    }

/*
    public ReInvitedTimeEngine(BasePresenter presenter, int limitTime) {
        this.presenter = presenter;
        this.limitTime = limitTime ;
    }*/

    public void stopIfNeccessary(){
        if(disposable !=null && !disposable.isDisposed()){
            RCLog.d("come--------->>>>"+ " ReInvitedTimeEngine  stop ");
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
                    RCLog.d("come--------->>>>"+ "ReInvitedTimeEngine: "+aLong);
                    if(aLong == limitTime ){
                        disposable.dispose();
                        presenter.closeActivityImediately();
                    }
                });
    }

}
