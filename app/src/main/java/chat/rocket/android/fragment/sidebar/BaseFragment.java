package chat.rocket.android.fragment.sidebar;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.ButterKnife;

/**
 * Created by lyq on 2018/5/11.
 */

public abstract class BaseFragment extends Fragment {
    protected Context mContext;
    public String mTabName;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View inflate = inflater.inflate(getLayoutId(), container, false);
        ButterKnife.bind(this, inflate);
        mContext=getActivity();
        initData();
        initView();
        return inflate;
    }

    protected void initData() {
    }

    protected void initView() {

    }
    public void setTabName(String name) {
        this.mTabName=name;
    }
    public String getmTabName(){
        return mTabName;
    }
    protected abstract int getLayoutId();
}
