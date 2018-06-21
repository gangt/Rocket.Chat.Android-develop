package chat.rocket.android.activity.business;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;

import chat.rocket.android.R;
import chat.rocket.android.RocketChatApplication;
import chat.rocket.android.ToastUtils;
import chat.rocket.android.helper.CallOtherOpeanFile;
import chat.rocket.android.helper.DownloadUtil;
import chat.rocket.android.helper.PermissionsUtils;
import chat.rocket.android.widget.helper.FileUtils;

/**
 * Created by helloworld on 2018/3/23
 */

public class FileViewActivity extends BusinessBaseActivity implements View.OnClickListener{

    private static final int DOWNLOAD_FAILED = 1;
    private static final int DOWNLOAD_SUCCESS = 2;
    private ImageView iv_back,iv_file;
    private String attachmentLink;
    private Button btn_download;
    private TextView tv_fileTitle;
    /**
     * 下载或要打开的文件，按照时间搓拼接的名字
     */
    private String currentFileName;
    private File currentFile;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_view);

        initView();
        initData();
        initHandler();
        setListener();
    }

    private void initData() {
        String attachmentTitle = getIntent().getStringExtra("attachmentTitle");
        attachmentLink = getIntent().getStringExtra("attachmentLink");
        long timestamp = getIntent().getLongExtra("timestamp", 0);
        tv_fileTitle.setText(attachmentTitle);

        currentFileName = DownloadUtil.get().getFileName(attachmentTitle, timestamp);
        String appDownloadDir = FileUtils.getAppDownloadDir(RocketChatApplication.getInstance());
        currentFile = new File(appDownloadDir+currentFileName);
        if(currentFile.exists()){
            btn_download.setText(R.string.open_file);
        }else{
            btn_download.setText(R.string.download);
        }

        showTitleIcon(attachmentTitle, iv_file);
    }

    private void setListener() {
        iv_back.setOnClickListener(this);
        btn_download.setOnClickListener(this);
    }

    private void initView() {
        TextView tv_title = findViewById(R.id.tv_title);
        iv_back = findViewById(R.id.iv_back);
        iv_file = findViewById(R.id.iv_file);
        findViewById(R.id.tv_create).setVisibility(View.GONE);
        tv_title.setText(R.string.file_view);

        btn_download = findViewById(R.id.btn_download);
        tv_fileTitle = findViewById(R.id.tv_fileTitle);
    }

    private void postDataWithParame() {
        String appDownloadDir = FileUtils.getAppDownloadDir(RocketChatApplication.getInstance());
        showProgressDialog();
        PermissionsUtils.verifyStoragePermissions(this);
        DownloadUtil.get().download(attachmentLink, appDownloadDir, currentFileName, new DownloadUtil.OnDownloadListener() {
            @Override
            public void onDownloadSuccess() {
                handler.obtainMessage(DOWNLOAD_SUCCESS).sendToTarget();
            }
            @Override
            public void onDownloading(int progress) {
//                progressBar.setProgress(progress);
            }

            @Override
            public void onDownloadFailed() {
                handler.sendEmptyMessage(DOWNLOAD_FAILED);
            }
        });
    }

    private Handler handler;
    @SuppressLint("HandlerLeak")
    private void initHandler(){
        handler = new Handler(){
            @Override
            public void handleMessage(android.os.Message msg) {
                super.handleMessage(msg);
                dismissProgressDialog();
                switch (msg.what){
                    case DOWNLOAD_FAILED:
                        ToastUtils.showToast("下载失败");
                        break;
                    case DOWNLOAD_SUCCESS:
                        ToastUtils.showToast("下载完成");
                        btn_download.setText(R.string.open_file);
                        break;
                }
            }
        };
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.iv_back:
                finish();
                break;
            case R.id.btn_download:
                if(currentFile.exists()){
                    CallOtherOpeanFile.openFile(FileViewActivity.this, currentFile);
                }else {
                    postDataWithParame();
                }
                break;
        }
    }

    private void showTitleIcon(String title, ImageView view) {
        if(FileUtils.isWord(title)){
            view.setImageResource(chat.rocket.android.widget.R.drawable.send_word);
        }else if(FileUtils.isExcel(title)){
            view.setImageResource(chat.rocket.android.widget.R.drawable.send_excel);
        }else if(FileUtils.isPPT(title)){
            view.setImageResource(chat.rocket.android.widget.R.drawable.send_ppt);
        }else if(FileUtils.isMp3(title)){
            view.setImageResource(chat.rocket.android.widget.R.drawable.send_mp3);
        }else if(FileUtils.isTxt(title)){
            view.setImageResource(chat.rocket.android.widget.R.drawable.send_txt);
        }else if(FileUtils.isZip(title)){
            view.setImageResource(chat.rocket.android.widget.R.drawable.send_zip);
        }else if(FileUtils.isPDF(title)){
            view.setImageResource(chat.rocket.android.widget.R.drawable.send_pdf);
        }
    }
}
