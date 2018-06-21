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
 * Created by Administrator on 2018/5/4/006.
 */

@SuppressLint("AppCompatCustomView")
public class CustomShowTextView extends TextView {
    private Disposable disposable ;
    public CustomShowTextView(Context context) {
        super(context);
    }

    public CustomShowTextView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomShowTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void stop(){
        if(disposable !=null && !disposable.isDisposed()){
            disposable.dispose();
            setVisibility(GONE);
        }
    }


    @SuppressLint({"RxLeakedSubscription", "RxSubscribeOnError"})
    public void  show(){
            disposable = Flowable.interval(0, 1, TimeUnit.SECONDS)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .onBackpressureBuffer()
                    .subscribe(aLong -> {
                        if(aLong > 20){
                            RCLog.d("come--------->>>>"+ "CustomShowTextView : "+aLong);
                            disposable.dispose();
                            setVisibility(GONE);
                        }
                    });
    }

}
