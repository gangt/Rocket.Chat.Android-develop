
package chat.rocket.android.widget.emotionkeyboard.utils;

import android.support.v4.util.ArrayMap;
import android.util.Log;

import java.util.HashMap;

import chat.rocket.android.widget.R;


/**
 * @author : zejian
 * @time : 2016年1月5日 上午11:32:33
 * @email : shinezejian@163.com
 * @description :表情加载类,可自己添加多种表情，分别建立不同的map存放和不同的标志符即可
 */
public class EmotionUtils {

	/**
	 * 表情类型标志符
	 */
	public static final int EMOTION_CLASSIC_TYPE=0x0001;//经典表情

	/**
	 * key-表情文字;
	 * value-表情图片资源
	 */
	public static HashMap<String, Integer> EMPTY_MAP;
	public static HashMap<String, Integer> EMOTION_CLASSIC_MAP;
	public static String[] _shortNameToUnicodes={
			":grinning:",
			":grimacing:",
			":joy:",
			":smiley:",
			":smile:",
			":sweat_smile:",
			":laughing:",
			":innocent:",
			":wink:",
			":blush:",
			":relaxed:",
			":yum:",
			":relieved:",
			":heart_eyes:",
			":kissing_heart:",
			":kissing:",
			":kissing_smiling_eyes:",
			":kissing_closed_eyes:",
			":stuck_out_tongue_winking_eye:",
			":stuck_out_tongue_closed_eyes:",
			":stuck_out_tongue:",
			":sunglasses:",
			":smirk:",
			":no_mouth:",
			":neutral_face:",
			":expressionless:",
			":unamused:",
			":flushed:",
			":disappointed:",
			":worried:",
			":angry:",
			":rage:",
			":pensive:",
			":confused:",
			":persevere:",
			":confounded:",
			":tired_face:",
			":weary:",
			":triumph:",
			":open_mouth:",
			":scream:",
			":fearful:",
			":cold_sweat:",
			":hushed:",
			":anguished:",
			":cry:",
			":disappointed_relieved:",
			":sleepy:",
			":sweat:",
			":sob:",
			":dizzy_face:",
			":mask:",
			":sleeping:",
			":zzz:",
			":poop:",
			":smiling_imp:",
			":japanese_goblin:",
			":skull:",
			":ghost:",
			":raised_hands:",
			":clap:",
			":thumbsup:",
			":thumbsdown:",
			":punch:",
			":ok_hand:",
			":raised_hand:",
			":open_hands:",
			":muscle:",
			":pray:",
			":point_up:",
			":point_up_2:",
			":point_down:",
			":point_left:",
			":point_right:",
			":lips:",
			":tongue:",
			":eyes:",

	};

	static {
		EMPTY_MAP = new HashMap<>();
		EMOTION_CLASSIC_MAP = new HashMap<>();

		EMOTION_CLASSIC_MAP.put(":grinning:", R.drawable.emoj1);
		EMOTION_CLASSIC_MAP.put(":grimacing:", R.drawable.emoj2);
		EMOTION_CLASSIC_MAP.put(":joy:", R.drawable.emoj4);
		EMOTION_CLASSIC_MAP.put(":smiley:", R.drawable.emoj5);
		EMOTION_CLASSIC_MAP.put(":smile:", R.drawable.emoj6);
		EMOTION_CLASSIC_MAP.put(":sweat_smile:", R.drawable.emoj7);
		EMOTION_CLASSIC_MAP.put(":laughing:", R.drawable.emoj8);
		EMOTION_CLASSIC_MAP.put(":innocent:", R.drawable.emoj9);
		EMOTION_CLASSIC_MAP.put(":wink:", R.drawable.emoj10);
		EMOTION_CLASSIC_MAP.put(":blush:", R.drawable.emoj11);
		EMOTION_CLASSIC_MAP.put(":relaxed:", R.drawable.emoj14);
		EMOTION_CLASSIC_MAP.put(":yum:", R.drawable.emoj15);
		EMOTION_CLASSIC_MAP.put(":relieved:", R.drawable.emoj16);
		EMOTION_CLASSIC_MAP.put(":heart_eyes:", R.drawable.emoj17);
		EMOTION_CLASSIC_MAP.put(":kissing_heart:", R.drawable.emoj18);
		EMOTION_CLASSIC_MAP.put(":kissing:", R.drawable.emoj19);
		EMOTION_CLASSIC_MAP.put(":kissing_smiling_eyes:", R.drawable.emoj20);
		EMOTION_CLASSIC_MAP.put(":kissing_closed_eyes:", R.drawable.emoj21);
		EMOTION_CLASSIC_MAP.put(":stuck_out_tongue_winking_eye:", R.drawable.emoj22);
		EMOTION_CLASSIC_MAP.put(":stuck_out_tongue_closed_eyes:", R.drawable.emoj23);
		EMOTION_CLASSIC_MAP.put(":stuck_out_tongue:", R.drawable.emoj24);
		EMOTION_CLASSIC_MAP.put(":sunglasses:", R.drawable.emoj27);
		EMOTION_CLASSIC_MAP.put(":smirk:", R.drawable.emoj29);
		EMOTION_CLASSIC_MAP.put(":no_mouth:", R.drawable.emoj30);
		EMOTION_CLASSIC_MAP.put(":neutral_face:", R.drawable.emoj31);
		EMOTION_CLASSIC_MAP.put(":expressionless:", R.drawable.emoj32);
		EMOTION_CLASSIC_MAP.put(":unamused:", R.drawable.emoj33);
		EMOTION_CLASSIC_MAP.put(":flushed:", R.drawable.emoj36);
		EMOTION_CLASSIC_MAP.put(":disappointed:", R.drawable.emoj37);
		EMOTION_CLASSIC_MAP.put(":worried:", R.drawable.emoj38);
		EMOTION_CLASSIC_MAP.put(":angry:", R.drawable.emoj39);
		EMOTION_CLASSIC_MAP.put(":rage:", R.drawable.emoj40);
		EMOTION_CLASSIC_MAP.put(":pensive:", R.drawable.emoj41);
		EMOTION_CLASSIC_MAP.put(":confused:", R.drawable.emoj42);
		EMOTION_CLASSIC_MAP.put(":persevere:", R.drawable.emoj45);
		EMOTION_CLASSIC_MAP.put(":confounded:", R.drawable.emoj46);
		EMOTION_CLASSIC_MAP.put(":tired_face:", R.drawable.emoj47);
		EMOTION_CLASSIC_MAP.put(":weary:", R.drawable.emoj48);
		EMOTION_CLASSIC_MAP.put(":triumph:", R.drawable.emoj49);
		EMOTION_CLASSIC_MAP.put(":open_mouth:", R.drawable.emoj50);
		EMOTION_CLASSIC_MAP.put(":scream:", R.drawable.emoj51);
		EMOTION_CLASSIC_MAP.put(":fearful:", R.drawable.emoj52);
		EMOTION_CLASSIC_MAP.put(":cold_sweat:", R.drawable.emoj53);
		EMOTION_CLASSIC_MAP.put(":hushed:", R.drawable.emoj54);
		EMOTION_CLASSIC_MAP.put(":anguished:", R.drawable.emoj55);
		EMOTION_CLASSIC_MAP.put(":cry:", R.drawable.emoj56);
		EMOTION_CLASSIC_MAP.put(":disappointed_relieved:", R.drawable.emoj57);
		EMOTION_CLASSIC_MAP.put(":sleepy:", R.drawable.emoj58);
		EMOTION_CLASSIC_MAP.put(":sweat:", R.drawable.emoj59);
		EMOTION_CLASSIC_MAP.put(":sob:", R.drawable.emoj60);
		EMOTION_CLASSIC_MAP.put(":dizzy_face:", R.drawable.emoj61);
		EMOTION_CLASSIC_MAP.put(":mask:", R.drawable.emoj64);
		EMOTION_CLASSIC_MAP.put(":sleeping:", R.drawable.emoj67);
		EMOTION_CLASSIC_MAP.put(":zzz:", R.drawable.emoj68);
		EMOTION_CLASSIC_MAP.put(":poop:", R.drawable.emoj69);
		EMOTION_CLASSIC_MAP.put(":smiling_imp:", R.drawable.emoj70);
		EMOTION_CLASSIC_MAP.put(":japanese_goblin:", R.drawable.emoj73);
		EMOTION_CLASSIC_MAP.put(":skull:", R.drawable.emoj74);
		EMOTION_CLASSIC_MAP.put(":ghost:", R.drawable.emoj75);
		EMOTION_CLASSIC_MAP.put(":raised_hands:", R.drawable.emoj76);
		EMOTION_CLASSIC_MAP.put(":clap:", R.drawable.emoj77);
		EMOTION_CLASSIC_MAP.put(":thumbsup:", R.drawable.emoj79);
		EMOTION_CLASSIC_MAP.put(":thumbsdown:", R.drawable.emoj80);
		EMOTION_CLASSIC_MAP.put(":punch:", R.drawable.emoj81);
		EMOTION_CLASSIC_MAP.put(":ok_hand:", R.drawable.emoj84);
		EMOTION_CLASSIC_MAP.put(":raised_hand:", R.drawable.emoj85);
		EMOTION_CLASSIC_MAP.put(":open_hands:", R.drawable.emoj86);
		EMOTION_CLASSIC_MAP.put(":muscle:", R.drawable.emoj87);
		EMOTION_CLASSIC_MAP.put(":pray:", R.drawable.emoj88);
		EMOTION_CLASSIC_MAP.put(":point_up:", R.drawable.emoj89);
		EMOTION_CLASSIC_MAP.put(":point_up_2:", R.drawable.emoj90);
		EMOTION_CLASSIC_MAP.put(":point_down:", R.drawable.emoj91);
		EMOTION_CLASSIC_MAP.put(":point_left:", R.drawable.emoj92);
		EMOTION_CLASSIC_MAP.put(":point_right:", R.drawable.emoj93);
		EMOTION_CLASSIC_MAP.put(":lips:", R.drawable.emoj100);
		EMOTION_CLASSIC_MAP.put(":tongue:", R.drawable.emoj101);
		EMOTION_CLASSIC_MAP.put(":eyes:", R.drawable.emoj105);

//		EMOTION_CLASSIC_MAP.put("[呵呵]", R.drawable.d_hehe);
//		EMOTION_CLASSIC_MAP.put("[嘻嘻]", R.drawable.d_xixi);
//		EMOTION_CLASSIC_MAP.put("[哈哈]", R.drawable.d_haha);
//		EMOTION_CLASSIC_MAP.put("[爱你]", R.drawable.d_aini);
//		EMOTION_CLASSIC_MAP.put("[挖鼻屎]", R.drawable.d_wabishi);
//		EMOTION_CLASSIC_MAP.put("[吃惊]", R.drawable.d_chijing);
//		EMOTION_CLASSIC_MAP.put("[晕]", R.drawable.d_yun);
//		EMOTION_CLASSIC_MAP.put("[泪]", R.drawable.d_lei);
//		EMOTION_CLASSIC_MAP.put("[馋嘴]", R.drawable.d_chanzui);
//		EMOTION_CLASSIC_MAP.put("[抓狂]", R.drawable.d_zhuakuang);
//		EMOTION_CLASSIC_MAP.put("[哼]", R.drawable.d_heng);
//		EMOTION_CLASSIC_MAP.put("[可爱]", R.drawable.d_keai);
//		EMOTION_CLASSIC_MAP.put("[怒]", R.drawable.d_nu);
//		EMOTION_CLASSIC_MAP.put("[汗]", R.drawable.d_han);
//		EMOTION_CLASSIC_MAP.put("[害羞]", R.drawable.d_haixiu);
//		EMOTION_CLASSIC_MAP.put("[睡觉]", R.drawable.d_shuijiao);
//		EMOTION_CLASSIC_MAP.put("[钱]", R.drawable.d_qian);
//		EMOTION_CLASSIC_MAP.put("[偷笑]", R.drawable.d_touxiao);
//		EMOTION_CLASSIC_MAP.put("[笑cry]", R.drawable.d_xiaoku);
//		EMOTION_CLASSIC_MAP.put("[doge]", R.drawable.d_doge);
//		EMOTION_CLASSIC_MAP.put("[喵喵]", R.drawable.d_miao);
//		EMOTION_CLASSIC_MAP.put("[酷]", R.drawable.d_ku);
//		EMOTION_CLASSIC_MAP.put("[衰]", R.drawable.d_shuai);
//		EMOTION_CLASSIC_MAP.put("[闭嘴]", R.drawable.d_bizui);
//		EMOTION_CLASSIC_MAP.put("[鄙视]", R.drawable.d_bishi);
//		EMOTION_CLASSIC_MAP.put("[花心]", R.drawable.d_huaxin);
//		EMOTION_CLASSIC_MAP.put("[鼓掌]", R.drawable.d_guzhang);
//		EMOTION_CLASSIC_MAP.put("[悲伤]", R.drawable.d_beishang);
//		EMOTION_CLASSIC_MAP.put("[思考]", R.drawable.d_sikao);
//		EMOTION_CLASSIC_MAP.put("[生病]", R.drawable.d_shengbing);
//		EMOTION_CLASSIC_MAP.put("[亲亲]", R.drawable.d_qinqin);
//		EMOTION_CLASSIC_MAP.put("[怒骂]", R.drawable.d_numa);
//		EMOTION_CLASSIC_MAP.put("[太开心]", R.drawable.d_taikaixin);
//		EMOTION_CLASSIC_MAP.put("[懒得理你]", R.drawable.d_landelini);
//		EMOTION_CLASSIC_MAP.put("[右哼哼]", R.drawable.d_youhengheng);
//		EMOTION_CLASSIC_MAP.put("[左哼哼]", R.drawable.d_zuohengheng);
//		EMOTION_CLASSIC_MAP.put("[嘘]", R.drawable.d_xu);
//		EMOTION_CLASSIC_MAP.put("[委屈]", R.drawable.d_weiqu);
//		EMOTION_CLASSIC_MAP.put("[吐]", R.drawable.d_tu);
//		EMOTION_CLASSIC_MAP.put("[可怜]", R.drawable.d_kelian);
//		EMOTION_CLASSIC_MAP.put("[打哈气]", R.drawable.d_dahaqi);
//		EMOTION_CLASSIC_MAP.put("[挤眼]", R.drawable.d_jiyan);
//		EMOTION_CLASSIC_MAP.put("[失望]", R.drawable.d_shiwang);
//		EMOTION_CLASSIC_MAP.put("[顶]", R.drawable.d_ding);
//		EMOTION_CLASSIC_MAP.put("[疑问]", R.drawable.d_yiwen);
//		EMOTION_CLASSIC_MAP.put("[困]", R.drawable.d_kun);
//		EMOTION_CLASSIC_MAP.put("[感冒]", R.drawable.d_ganmao);
//		EMOTION_CLASSIC_MAP.put("[拜拜]", R.drawable.d_baibai);
//		EMOTION_CLASSIC_MAP.put("[黑线]", R.drawable.d_heixian);
//		EMOTION_CLASSIC_MAP.put("[阴险]", R.drawable.d_yinxian);
//		EMOTION_CLASSIC_MAP.put("[打脸]", R.drawable.d_dalian);
//		EMOTION_CLASSIC_MAP.put("[傻眼]", R.drawable.d_shayan);
//		EMOTION_CLASSIC_MAP.put("[猪头]", R.drawable.d_zhutou);
//		EMOTION_CLASSIC_MAP.put("[熊猫]", R.drawable.d_xiongmao);
//		EMOTION_CLASSIC_MAP.put("[兔子]", R.drawable.d_tuzi);
	}

	/**
	 * 根据名称获取当前表情图标R值
	 * @param EmotionType 表情类型标志符
	 * @param imgName 名称
	 * @return
	 */
	public static int getImgByName(int EmotionType,String imgName) {
		Integer integer=null;
		switch (EmotionType){
			case EMOTION_CLASSIC_TYPE:
				integer = EMOTION_CLASSIC_MAP.get(imgName);
				break;
			default:
				Log.e("","the emojiMap is null!! Handle Yourself ");
				break;
		}
		return integer == null ? -1 : integer;
	}

	/**
	 * 根据类型获取表情数据
	 * @param EmotionType
	 * @return
	 */
	public static HashMap<String, Integer> getEmojiMap(int EmotionType){
		HashMap EmojiMap=null;
		switch (EmotionType){
			case EMOTION_CLASSIC_TYPE:
				EmojiMap=EMOTION_CLASSIC_MAP;
				break;
			default:
				EmojiMap=EMPTY_MAP;
				break;
		}
		return EmojiMap;
	}
}
