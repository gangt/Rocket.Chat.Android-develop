package chat.rocket.android.video.presenter;

/**
 * Created by Administrator on 2018/4/28/026.
 */

public class PresenterFactory {


    public static BasePresenter getInstance(Class<? extends BasePresenter> clazz){

        BasePresenter presenter = null ;
        try {
            presenter = (BasePresenter) Class.forName(clazz.getName()).newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return  presenter ;

    }


}
