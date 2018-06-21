package chat.rocket.android.activity.business;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.JsResult;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.TextView;

import chat.rocket.android.R;

/**
 * Created by jumper_C on 2018/6/8.
 */

public class WebViewActivity extends BusinessBaseActivity {
    WebView webview;
    @SuppressLint("JavascriptInterface")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);
        findViewById(R.id.iv_back).setOnClickListener(view -> onBackPressed());
        webview=findViewById(R.id.webview);
        findViewById(R.id.tv_create).setVisibility(View.GONE);
        TextView title=findViewById(R.id.tv_title);
        title.setText(getIntent().getStringExtra("title"));
        ProgressBar mProgressBar=findViewById(R.id.progress);
        String webUrl = getIntent().getStringExtra("link");
        WebSettings webSettings = webview.getSettings();
        webSettings.setAllowFileAccess(true);
        webSettings.setBuiltInZoomControls(true);//允许缩放
        webSettings.setDisplayZoomControls(false);//不显示缩放控件
        webSettings.setJavaScriptEnabled(true);
        webSettings.setBlockNetworkImage(false);
        webSettings.setNeedInitialFocus(false);
        webSettings.setSupportZoom(true);
        webSettings.setLoadWithOverviewMode(true);//适应屏幕
//        webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);//把所有内容放大webview等宽的一列中4.4以上失效
        webSettings.setUseWideViewPort(true); //将图片调整到适合webview的大小
        webSettings.setLoadsImagesAutomatically(true);//自动加载图片
        webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
        mProgressBar.setMax(100);
//        webview.addJavascriptInterface(new JavascriptInterface(), "imagelistner");
        webview.setWebViewClient(new BrowserWebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Log.i("用户单击超连接", url);
                //判断用户单击的是那个超连接用来拦截跳转

                return false;
            }

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                handler.proceed();
            }
        });
        webview.setWebChromeClient(new WebChromeClient()    // 页面对话框弹出事件
        {
            @Override
            public boolean onJsAlert(WebView view, String url, String message,
                                     JsResult result) {
                view.loadUrl(url);
                return super.onJsAlert(view, url, message, result);
            }

            @Override
            public void onProgressChanged(WebView view, int progress) {
                // Activity和Webview根据加载程度决定进度条的进度大小
                // 当加载到100%的时候 进度条自动消失
                if (webview != null) {
                    WebViewActivity.this.setProgress(progress * 100);
                    mProgressBar.setProgress(webview.getProgress());
                    if (mProgressBar.getProgress() == 100) {
                        mProgressBar.setVisibility(View.GONE);
                    }
                }
            }

        });
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);

        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.TEXT_AUTOSIZING);
        } else {
            webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NORMAL);
        }
        webview.loadUrl(webUrl.startsWith("http")?webUrl:"http://"+webUrl);
        webview.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent ev) {
                ((WebView) v).requestDisallowInterceptTouchEvent(true);
                return false;
            }
        });

    }

    private class BrowserWebViewClient extends WebViewClient {

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            // html加载完成之后，添加监听图片的点击js函数
            addImageClickListner();
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
        }

        @Override
        public void onReceivedError(WebView view, int errorCode,
                                    String description, String failingUrl) {
            super.onReceivedError(view, errorCode, description, failingUrl);
        }
    }

    /**
     * 注入js函数监听
     */
    private void addImageClickListner() {
//         这段js函数的功能就是，遍历所有的img几点，并添加onclick函数，在还是执行的时候调用本地接口传递url过去
        webview.loadUrl("javascript:(function(){" +
                "var objs = document.getElementsByTagName(\"img\"); " +
                "for(var i=0;i<objs.length;i++)  " +
                "{"
                + "    objs[i].onclick=function()  " +
                "    {  "
                + "        window.imagelistner.openImage(this.src);  " +
                "    }  " +
                "}" +
                "})()");
    }


}
