package chat.rocket.android.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import chat.rocket.android.R;
import chat.rocket.android.RocketChatConstants;

/**
 * Created by zhangxiugao on 2018/1/25
 */

public class OprationAdapter extends BaseAdapter {
    private Context mContext;
    private String[] texts;
    private int[] recourceId;

    public OprationAdapter(Context context, String roomType){
        this.mContext = context;
//            texts = new String[]{"附件","搜索","归档","提及","收藏","固定"};
        if(RocketChatConstants.M.equals(roomType)){// 会议巢
            texts = new String[]{"搜索","总结","归档"};
            recourceId = new int[]{R.drawable.group_search,R.drawable.group_meeting,
                    R.drawable.group_cundang,R.drawable.group_tiji,
                    R.drawable.group_souchang,R.drawable.group_gudin};
        }else {
            texts = new String[]{"搜索","附件","归档"};
            recourceId = new int[]{R.drawable.group_search,R.drawable.group_fujian,
                    R.drawable.group_cundang,R.drawable.group_tiji,
                    R.drawable.group_souchang,R.drawable.group_gudin};
        }
        inits();
    }

    private void inits() {
    }

    @Override
    public int getCount() {
        return texts.length;
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = View.inflate(mContext, R.layout.listitem_group, null);
        ImageView iv_icon = view.findViewById(R.id.iv_icon);
        TextView tv_name = view.findViewById(R.id.tv_name);
        iv_icon.setImageResource(recourceId[position]);
        tv_name.setText(texts[position]);

        return view;
    }

}
