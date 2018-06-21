package chat.rocket.android.widget.emotionkeyboard.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import java.util.List;

import chat.rocket.android.widget.R;
import chat.rocket.android.widget.emotionkeyboard.utils.EmotionUtils;

/**
 * Created by zejian
 * Time  16/1/7 下午4:46
 * Email shinezejian@163.com
 * Description:
 */
public class EmotionGridViewAdapter extends BaseAdapter {

	private Context context;
	private List<String> emotionNames;
	private int itemWidth;
    private int emotion_map_type;
	
	public EmotionGridViewAdapter(Context context, List<String> emotionNames, int itemWidth, int emotion_map_type) {
		this.context = context;
		this.emotionNames = emotionNames;
		this.itemWidth = itemWidth;
		this.emotion_map_type=emotion_map_type;
	}
	
	@Override
	public int getCount() {
		// +1 最后一个为删除按钮
		return emotionNames.size() + 1;
//		return Emojione._shortNameToUnicode.size()+1;
	}


	@Override
	public String getItem(int position) {
		return emotionNames.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ImageView iv_emotion = new ImageView(context);
//		TextView iv_emotion = new TextView(context);
//		iv_emotion.setTextSize(22);
//		iv_emotion.setTextColor(Color.BLACK);
		// 设置内边距
		iv_emotion.setPadding(itemWidth/16, itemWidth/16, itemWidth/16, itemWidth/16);
		LayoutParams params = new LayoutParams(itemWidth, itemWidth);
//		params.gravity= Gravity.CENTER;
		iv_emotion.setLayoutParams(params);
		//判断是否为最后一个item
		if(position == getCount() - 1) {
			iv_emotion.setImageResource(R.drawable.compose_emotion_delete);
			iv_emotion.setVisibility(View.GONE);
			ImageView iv_emotion1 = new ImageView(context);
			iv_emotion1.setPadding(itemWidth/16, itemWidth/16, itemWidth/16, itemWidth/16);
			iv_emotion1.setLayoutParams(params);
			iv_emotion1.setImageResource(R.drawable.compose_emotion_delete);
			return iv_emotion1;
		} else {
			String emotionName = emotionNames.get(position);
			iv_emotion.setImageResource(EmotionUtils.getImgByName(emotion_map_type,emotionName));
//			iv_emotion.setText(Emojione.shortnameToUnicode(":"+(String)(Emojione._shortNameToUnicode.keySet().toArray()[position])+":",false));
//			iv_emotion.setText(Emojione.shortnameToUnicode(":"+emotionName+":",false));
		}
		
		return iv_emotion;
	}

}
