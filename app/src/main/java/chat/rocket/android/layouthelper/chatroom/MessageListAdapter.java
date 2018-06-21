package chat.rocket.android.layouthelper.chatroom;

import android.content.Context;
import android.support.v7.util.DiffUtil;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ProgressBar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import chat.rocket.android.R;
import chat.rocket.android.helper.TextUtils;
import chat.rocket.android.layouthelper.ExtModelListAdapter;
import chat.rocket.android.widget.AbsoluteUrl;
import chat.rocket.core.models.Message;

/**
 * target list adapter for chat room.
 */
public class MessageListAdapter extends ExtModelListAdapter<Message, PairedMessage, AbstractMessageViewHolder> {

  private static final int VIEW_TYPE_UNKNOWN = 0;
  private static final int VIEW_TYPE_NORMAL_MESSAGE = 1;
  private static final int VIEW_TYPE_MYSELF_MESSAGE = 2;
  private static final int VIEW_TYPE_SYSTEM_MESSAGE = 3;
  private String hostname;
  private AbsoluteUrl absoluteUrl;

  private boolean autoloadImages = false;
  private boolean hasNext;
  private boolean isLoaded;
//  private Context mContext;
  private OnImageClickListener onImageClickListener;
  private OnImageLongClickListener onImageLongClickListener;
  private OnAttachItemLongClickListener onAttachItemLongClickListener;
  private OnMessageFailedClickListener onMessageFailedClickListener;


  public MessageListAdapter(Context context, String hostname) {
    super(context);
//    this.mContext = context;
    this.hostname = hostname;
    this.hasNext = true;
  }

  public void setAbsoluteUrl(AbsoluteUrl absoluteUrl) {
    this.absoluteUrl = absoluteUrl;
    notifyDataSetChanged();
  }

  public void setAutoloadImages(boolean autoloadImages) {
    this.autoloadImages = autoloadImages;
  }

  /**
   * update Footer state considering hasNext and isLoaded.
   */
  public void updateFooter(boolean hasNext, boolean isLoaded) {
    this.hasNext = hasNext;
    this.isLoaded = isLoaded;
    notifyFooterChanged();
  }

  @Override
  protected int getHeaderLayout() {
    return R.layout.list_item_message_header;
  }

  @Override
  protected int getFooterLayout() {
     if (!hasNext || isLoaded) {
      return R.layout.list_item_message_start_of_conversation;
    } else {
      return R.layout.list_item_message_loading;
    }
  }

  @Override
  protected int getRealmModelViewType(PairedMessage model) {
    if (model.target != null) {
      if (TextUtils.isEmpty(model.target.getType())&&!model.isMySelf()) {
        return VIEW_TYPE_NORMAL_MESSAGE;
      } else if(TextUtils.isEmpty(model.target.getType())&&model.isMySelf()){
        return VIEW_TYPE_MYSELF_MESSAGE;
      }else {
        return VIEW_TYPE_SYSTEM_MESSAGE;
      }
    }
    return VIEW_TYPE_UNKNOWN;
  }

  @Override
  protected int getRealmModelLayout(int viewType) {
    switch (viewType) {
      case VIEW_TYPE_NORMAL_MESSAGE:
        return R.layout.list_item_normal_message;
      case VIEW_TYPE_MYSELF_MESSAGE:
        return R.layout.list_item_myself_message;
      case VIEW_TYPE_SYSTEM_MESSAGE:
        return R.layout.list_item_system_message;
      default:
        return R.layout.simple_screen;
    }
  }

  @Override
  protected AbstractMessageViewHolder onCreateRealmModelViewHolder(int viewType, View itemView) {
    setOnClickListener(itemView);

    switch (viewType) {
      case VIEW_TYPE_NORMAL_MESSAGE:
        return new MessageNormalViewHolder(itemView, absoluteUrl, hostname, this,false);
      case VIEW_TYPE_MYSELF_MESSAGE:
        return new MessageNormalViewHolder(itemView, absoluteUrl, hostname, this,true);
      case VIEW_TYPE_SYSTEM_MESSAGE:
        return new MessageSystemViewHolder(itemView, absoluteUrl, hostname);
      default:
        return new AbstractMessageViewHolder(itemView, absoluteUrl, hostname) {
          @Override
          protected void bindMessage(PairedMessage pairedMessage, boolean autoloadImages) {}
        };
    }
  }

//  float xDown = 0f, yDown = 0f, xUp;
//  boolean isLongClickModule = false;
//  boolean isLongClicking = false;
//  private boolean isLongPressed(float lastX, float lastY,
//                                float thisX, float thisY,
//                                long lastDownTime, long thisEventTime,
//                                long longPressTime) {
//    float offsetX = Math.abs(thisX - lastX);
//    float offsetY = Math.abs(thisY - lastY);
//    long intervalTime = thisEventTime - lastDownTime;
//    if (offsetX <= 10 && offsetY <= 10 && intervalTime >= longPressTime) {
//      return true;
//    }
//    return false;
//  }
//  boolean intercept=false;
  private void setOnClickListener(View itemView) {
    View user_avatar = itemView.findViewById(R.id.user_avatar);
    View errorImageView = itemView.findViewById(R.id.errorImageView);
    ProgressBar progressBar = itemView.findViewById(R.id.progressBar);
    View message_attachments=itemView.findViewById(R.id.message_attachments);
    if(message_attachments!=null){
//      message_attachments.setOnTouchListener(new View.OnTouchListener() {
//        @Override
//        public boolean onTouch(View v, MotionEvent event) {
//          if (event.getAction() == MotionEvent.ACTION_DOWN) {
//            xDown = event.getX();
//            yDown = event.getY();
//
//          } else if (event.getAction() == MotionEvent.ACTION_UP) {// 松开处理
//            //获取松开时的x坐标
//            if (isLongClickModule) {
//              isLongClickModule = false;
//              isLongClicking = false;
//            }
//            xUp = event.getX();
//
//            //按下和松开绝对值差当大于20时滑动，否则不显示
//            if ((xUp - xDown) > 20) {
//              //添加要处理的内容
//            } else if ((xUp - xDown) < -20) {
//              //添加要处理的内容
//            } else if (0 == (xDown - xUp)) {
//              /**处理点击事件*/
//              intercept=false;
//              int viewWidth = v.getWidth();
//              if (xDown < viewWidth / 3) {
//                //靠左点击
//              } else if (xDown > viewWidth / 3 && xDown < viewWidth * 2 / 3) {
//                //中间点击
//              } else {
//                //靠右点击
//              }
//            }
//          } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
//            //当滑动时背景为选中状态 //检测是否长按,在非长按时检测
//            if (!isLongClickModule) {
//              isLongClickModule = isLongPressed(xDown, yDown, event.getX(),
//                      event.getY(), event.getDownTime(), event.getEventTime(), 300);
//            }
//            if (isLongClickModule && !isLongClicking) {
//              //处理长按事件
//              isLongClicking = true;
//              intercept=true;
//              onAttachItemLongClickListener.onAttachItemLongClick(itemView.getTag());
//            }
//          } else {
//            //其他模式
//          }
//          return intercept;
//        }
//      });
//      message_attachments.setOnLongClickListener(view -> {
//        if (onAttachItemLongClickListener != null) {
//          onAttachItemLongClickListener.onAttachItemLongClick(itemView.getTag());
//        }
//        return true;
//      });
    }
    if(user_avatar != null) {
      user_avatar.setOnClickListener(v -> {
        if (onImageClickListener != null) {
          onImageClickListener.onImageClick(itemView.getTag());
        }
      });
      user_avatar.setOnLongClickListener(v -> {
        if (onImageLongClickListener != null) {
          onImageLongClickListener.onImageLongClick(itemView.getTag());

        }
        return true;
       });
    }
    if(errorImageView != null) {
      errorImageView.setOnClickListener(v -> {
        if (onMessageFailedClickListener != null) {
          onMessageFailedClickListener.onMessageFailedClick(itemView.getTag());
        }
      });
    }
  }

  @Override
  protected List<PairedMessage> mapResultsToViewModel(List<Message> results) {
    if (results.isEmpty()) {
      return Collections.emptyList();
    }
//    Collections.reverse(results);
    ArrayList<PairedMessage> extMessages = new ArrayList<>();
    for (int i = 0; i < results.size() - 1; i++) {
      extMessages.add(new PairedMessage(results.get(i), results.get(i + 1)));
    }
    extMessages.add(new PairedMessage(results.get(results.size() - 1), null));
//    Collections.reverse(extMessages);
    return extMessages;
  }

  @Override
  protected boolean shouldAutoloadImages() {
    return autoloadImages;
  }

  @Override
  protected DiffUtil.Callback getDiffCallback(List<PairedMessage> oldData,
                                              List<PairedMessage> newData) {
    return new PairedMessageDiffCallback(oldData, newData);
  }

  private static class PairedMessageDiffCallback extends DiffUtil.Callback {

    private final List<PairedMessage> oldList;
    private final List<PairedMessage> newList;

    public PairedMessageDiffCallback(List<PairedMessage> oldList, List<PairedMessage> newList) {
      this.oldList = oldList;
      this.newList = newList;
    }

    @Override
    public int getOldListSize() {
      if (oldList == null) {
        return 0;
      }
      return oldList.size();
    }

    @Override
    public int getNewListSize() {
      if (newList == null) {
        return 0;
      }
      return newList.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
      PairedMessage oldMessage = oldList.get(oldItemPosition);
      PairedMessage newMessage = newList.get(newItemPosition);

      return oldMessage.getId().equals(newMessage.getId());
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
      PairedMessage oldMessage = oldList.get(oldItemPosition);
      PairedMessage newMessage = newList.get(newItemPosition);

      return oldMessage.equals(newMessage);
    }
  }

  public void setOnMessageFailedClickListener(OnMessageFailedClickListener<PairedMessage> onItemClickListener) {
    this.onMessageFailedClickListener = onItemClickListener;
  }

  public void setOnImageClickListener(OnImageClickListener<PairedMessage> onImageClickListener) {
    this.onImageClickListener = onImageClickListener;
  }

  public void setOnImageLongClickListener(OnImageLongClickListener<PairedMessage> onImageLongClickListener) {
    this.onImageLongClickListener = onImageLongClickListener;
  }

  public void setOnAttachItemLongClickListener(OnAttachItemLongClickListener<PairedMessage> onAttachItemLongClickListener) {
    this.onAttachItemLongClickListener = onAttachItemLongClickListener;
  }

  public interface OnImageClickListener<PairedMessage> {
    void onImageClick(PairedMessage pairedMessage);
  }

  public interface OnImageLongClickListener<PairedMessage> {
    void onImageLongClick(PairedMessage pairedMessage);
  }

  public interface OnAttachItemLongClickListener<PairedMessage> {
    void onAttachItemLongClick(PairedMessage pairedMessage);
  }

  public interface OnMessageFailedClickListener<PairedMessage> {
    void onMessageFailedClick(PairedMessage message);
  }
}