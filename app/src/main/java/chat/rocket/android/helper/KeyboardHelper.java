package chat.rocket.android.helper;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import chat.rocket.android.widget.message.MessageFormLayout;

public class KeyboardHelper {
  public static void hideSoftKeyboard(Activity activity, MessageFormLayout messageFormLayout) {
    if(messageFormLayout!=null){
      messageFormLayout.setFocusable();
    }
    View currentFocus = activity.getCurrentFocus();
    if (currentFocus != null) {
      InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
      inputMethodManager.hideSoftInputFromWindow(currentFocus.getWindowToken(), 0);
    }
  }
  public static void hideSoftKeyboard(Activity activity) {
    View currentFocus = activity.getCurrentFocus();
    if (currentFocus != null) {
      InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
      inputMethodManager.hideSoftInputFromWindow(currentFocus.getWindowToken(), 0);
    }
  }

  public static void showSoftKeyboard(View view) {
    InputMethodManager inputMethodManager = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
    view.requestFocus();
    inputMethodManager.showSoftInput(view, 0);
  }

  public static void toggleKeyBoard(Context context) {
    InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
    inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
  }
}