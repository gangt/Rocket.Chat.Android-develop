package chat.rocket.android.video.view;


import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.TextView;

import java.util.concurrent.TimeUnit;

import chat.rocket.android.log.RCLog;
import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by Administrator on 2018/5/18/018.
 */

@SuppressLint("AppCompatCustomView")
public class LoadingTextView extends TextView {
    private String txt;

    public LoadingTextView(Context context) {
        super(context);
    }

    public LoadingTextView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public LoadingTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    Disposable disposable ;

    @SuppressLint("RxSubscribeOnError")
    public void show(String txt){
        this.txt = txt;
        disposable = Flowable.interval(0, 500, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .onBackpressureBuffer()
                .subscribe(aLong -> {
                    RCLog.d("come--------->>>>" + "LoadingTextView:"+aLong);
                    updateUI(aLong %3);
                });
    }

    public  void stopIfNeccessary(){
        try {
            if(disposable !=null && !disposable.isDisposed() ){
                RCLog.d("come--------->>>>" + "LoadingTextView  stop");
                disposable.dispose();
                setVisibility(GONE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    private void updateUI(Long aLong) {
        int num = Integer.parseInt(aLong+"");
        switch (num){
            case  0:
                setText(txt+".");
                break ;
            case  1:
                setText(txt+"..");
                break ;
            case  2:
                setText(txt+"...");
                break ;
        }
    }

}
