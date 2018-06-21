package chat.rocket.android.video.helper;


import android.annotation.SuppressLint;
import android.widget.TextView;

import java.util.concurrent.TimeUnit;

import chat.rocket.android.log.RCLog;
import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by Administrator on 2018/5/3/003.
 */

public class CountTimeEngine {
    private TextView view;
    private Disposable disposable;
    private String time;
    private static CountTimeEngine instance ;

    private CountTimeEngine (){}

    public  static CountTimeEngine getInstance(){
        if(instance == null){
            instance = new CountTimeEngine();
        }
        return instance ;
    }


    public CountTimeEngine(TextView view) {
        this.view = view;
    }

    public void stopIfNeccessary() {
        try {
            if (disposable != null && !disposable.isDisposed()) {
                RCLog.d("come--------->>>>" + "CountTimeEngine  stop");
                TempFileUtils.getInstance().setTime(time);
                disposable.dispose();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateView(TextView view) {
        this.view = view;
    }


    @SuppressLint({"RxLeakedSubscription", "RxSubscribeOnError"})
    public void start() {
        TempFileUtils.getInstance().setTime(null);
        RCLog.d("come--------->>>>" + "CountTimeEngine  start");
        disposable= Flowable.interval(0, 1, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .onBackpressureBuffer()
                .subscribe(aLong -> {
                    RCLog.d("come--------->>>>" + "updateUI" + aLong);
                    updateUI(aLong);
                });
    }

    private void updateUI(Long aLong) {
        String sseconds;
        String sminutes;

        long minutes = aLong / 60;
        long seconds = aLong % 60;

        if (seconds < 10) {
            sseconds = "0" + seconds;
        } else {
            sseconds = String.valueOf(seconds);
        }
        if (minutes < 10) {
            sminutes = "0" + minutes;
        } else {
            sminutes = String.valueOf(minutes);
        }
        time = sminutes + " : " + sseconds;
        view.setText(time);
    }
}
