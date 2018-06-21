package chat.rocket.android.service.observer;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import bolts.Task;
import chat.rocket.android.ToastUtils;
import chat.rocket.android.api.FileUploadingHelper;
import chat.rocket.android.helper.LogIfError;
import chat.rocket.android.helper.OkHttpHelper;
import chat.rocket.android.helper.eventbus.BaseEvent;
import chat.rocket.android.helper.eventbus.EventTags;
import chat.rocket.android.log.RCLog;
import chat.rocket.core.SyncState;
import chat.rocket.persistence.realm.RealmHelper;
import chat.rocket.persistence.realm.models.ddp.RealmUser;
import chat.rocket.persistence.realm.models.internal.FileUploading;
import chat.rocket.persistence.realm.models.internal.RealmSession;
import io.realm.Realm;
import io.realm.RealmResults;
import me.jessyan.progressmanager.ProgressListener;
import me.jessyan.progressmanager.ProgressManager;
import me.jessyan.progressmanager.body.ProgressInfo;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


/**
 * execute file uploading and requesting sendMessage with attachment.
 */
public class FileUploadingWithUfsObserver extends AbstractModelObserver<FileUploading> {
    private FileUploadingHelper methodCall;
    private Context mContext;
    private String uplId;
    public FileUploadingWithUfsObserver(Context context, String hostname,
                                        RealmHelper realmHelper) {
        super(context, hostname, realmHelper);
        methodCall = new FileUploadingHelper(realmHelper);
        mContext=context;
            realmHelper.executeTransaction(realm -> {
                // resume pending operations.
                RealmResults<FileUploading> pendingUploadRequests = realm.where(FileUploading.class)
                        .equalTo(FileUploading.SYNC_STATE, SyncState.SYNCING)
                        .beginGroup()
                        .equalTo(FileUploading.STORAGE_TYPE, FileUploading.STORAGE_TYPE_GRID_FS)
                        .or()
                        .equalTo(FileUploading.STORAGE_TYPE, FileUploading.STORAGE_TYPE_FILE_SYSTEM)
                        .endGroup()
                        .findAll();
            for (FileUploading req : pendingUploadRequests) {
                RCLog.d("修改成未同步");
                req.setSyncState(SyncState.NOT_SYNCED);
            }

            // clean up records.
            realm.where(FileUploading.class)
                    .beginGroup()
                    .equalTo(FileUploading.SYNC_STATE, SyncState.SYNCED)
                    .or()
                    .equalTo(FileUploading.SYNC_STATE, SyncState.FAILED)
                    .endGroup()
                    .beginGroup()
                    .equalTo(FileUploading.STORAGE_TYPE, FileUploading.STORAGE_TYPE_GRID_FS)
                    .or()
                    .equalTo(FileUploading.STORAGE_TYPE, FileUploading.STORAGE_TYPE_FILE_SYSTEM)
                    .endGroup()
                    .findAll().deleteAllFromRealm();
            return null;
        }).continueWith(new LogIfError());
    }

    @Override
    public RealmResults<FileUploading> queryItems(Realm realm) {
        return realm.where(FileUploading.class)
                .equalTo(FileUploading.SYNC_STATE, SyncState.NOT_SYNCED)
                .beginGroup()
                .equalTo(FileUploading.STORAGE_TYPE, FileUploading.STORAGE_TYPE_GRID_FS)
                .or()
                .equalTo(FileUploading.STORAGE_TYPE, FileUploading.STORAGE_TYPE_FILE_SYSTEM)
                .endGroup()
                .findAll();
    }

    @Override
    public void onUpdateResults(List<FileUploading> results) {
        if (results.isEmpty()) {
            return;
        }

        List<FileUploading> uploadingList = realmHelper.executeTransactionForReadResults(realm ->
                realm.where(FileUploading.class).equalTo(FileUploading.SYNC_STATE, SyncState.SYNCING)
                        .findAll());
        RCLog.d("正在同步的文件数量::"+uploadingList.size());
//        if (uploadingList.size() >= 1) {
//            return;
//        }

        RealmUser currentUser = realmHelper.executeTransactionForRead(realm ->
                RealmUser.queryCurrentUser(realm).findFirst());
        RealmSession session = realmHelper.executeTransactionForRead(realm ->
                RealmSession.queryDefaultSession(realm).findFirst());
        if (currentUser == null || session == null) {
            return;
        }
        final String cookie = String.format("rc_uid=%s; rc_token=%s",
                currentUser.getId(), session.getToken());

        FileUploading fileUploading = results.get(0);
        final String roomId = fileUploading.getRoomId();

        uplId = fileUploading.getUplId();
        final String filename = fileUploading.getFilename();
        final long filesize = fileUploading.getFilesize();
        if (filesize==-1){
            realmHelper.executeTransaction(realm -> {
                         return realm.createOrUpdateObjectFromJson(FileUploading.class, new JSONObject()
                                .put(FileUploading.ID, uplId)
                                .put(FileUploading.SYNC_STATE, SyncState.FAILED)
                                 .put(FileUploading.ERROR,"文件不存在")
                        );
                    });
            BaseEvent baseEvent = new BaseEvent();
            baseEvent.setCode(EventTags.UPLOAD_DIALOG);
            EventBus.getDefault().post(baseEvent);
            ToastUtils.showToast("文件不存在！");
            return;
        }
        final String mimeType = fileUploading.getMimeType();
        final Uri fileUri = Uri.parse(fileUploading.getUri());
//        String filePathFromContentUri="";
//        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
//            try {
//                filePathFromContentUri = ConvertUriToFilePath.getPathFromURI(mContext,fileUri);
//            } catch (Exception e) {
//                filePathFromContentUri=Uri.decode(fileUri.getPath());
//            }
//            if (TextUtils.isEmpty(filePathFromContentUri)){
//                filePathFromContentUri=Uri.decode(fileUri.getPath());
//            }
//        }else {
        String filePathFromContentUri=Uri.decode(fileUri.getPath());
//        }
        File file = new File(filePathFromContentUri);
        final String store = FileUploading.STORAGE_TYPE_GRID_FS.equals(fileUploading.getStorageType())
                ? "rocketchat_uploads"
                : (FileUploading.STORAGE_TYPE_FILE_SYSTEM.equals(fileUploading.getStorageType())
                ? "fileSystem" : null);

        realmHelper.executeTransaction(realm -> {
                    RCLog.d("修改状态----------------------------------------");
                    return realm.createOrUpdateObjectFromJson(FileUploading.class, new JSONObject()
                            .put(FileUploading.ID, uplId)
                            .put(FileUploading.SYNC_STATE, SyncState.SYNCING)
                    );
                }
        ).onSuccessTask(_task -> methodCall.ufsCreate(filename, filesize, mimeType, store, roomId)
        ).onSuccessTask(task -> {
            final JSONObject info = task.getResult();
            final String fileId = info.getString("fileId");
            final String token = info.getString("token");
            final String url = info.getString("url");

            final int bufSize = 16384; //16KB   2147483647
            final byte[] buffer = new byte[(int) filesize];
            int offset = 0;
            final MediaType contentType = MediaType.parse(mimeType);
            RequestBody requestBody = RequestBody.create(contentType, file);
            try (InputStream inputStream = context.getContentResolver().openInputStream(fileUri)) {
                int read;
//                while ((read = inputStream.read(buffer)) > 0) {
//                    offset += read;
//                    double progress = 1.0 * offset / filesize;
                    Request request = new Request.Builder()
                            .url(url)
                            .header("Cookie", cookie)
                            .post(requestBody)
                            .build();

//                    boolean complete=(1.0 * offset / filesize)>=1;
                    ProgressManager.getInstance().addRequestListener(url, getUploadListener());
                    OkHttpHelper.INSTANCE.getClientForUploadFile().newCall(request).enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            System.out.print(e.getMessage());
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            String name = Thread.currentThread().getName();

//                            if(complete){
                                         methodCall.ufsComplete(fileId, token, store) .onSuccessTask(task -> {
//                                                     RCLog.d("文件上传成功，发送信息：-----------------------------"+task.getResult());//上传中文文件会导致报错
                                                     JSONObject result=customizeJson(task.getResult());
                                                     return  methodCall.sendFileMessage(roomId, null, result);
                                                 }
                                         ).onSuccessTask(task -> realmHelper.executeTransaction(realm ->{
                                                     BaseEvent baseEvent = new BaseEvent();
                                                     baseEvent.setCode(EventTags.REFRESH_PIC);
                                                     baseEvent.setMsg(uplId);
                                                     EventBus.getDefault().post(baseEvent);
                                                     RCLog.d("数据库同步服务器成功----------------------------------------");
                                                     return   realm.createOrUpdateObjectFromJson(FileUploading.class, new JSONObject()
                                                             .put(FileUploading.ID, uplId)
                                                             .put(FileUploading.SYNC_STATE, SyncState.SYNCED)
                                                             .put(FileUploading.ERROR, JSONObject.NULL)
                                                     );
                                                 }

                                         )).continueWithTask(task -> {
                                             if (task.isFaulted()) {
                                                 RCLog.w(task.getError());
                                                 RCLog.d("数据库同步服务器失败----------------------------------------");
                                                 return realmHelper.executeTransaction(realm ->
                                                         realm.createOrUpdateObjectFromJson(FileUploading.class, new JSONObject()
                                                                 .put(FileUploading.ID, uplId)
                                                                 .put(FileUploading.SYNC_STATE, SyncState.FAILED)
                                                                 .put(FileUploading.ERROR, task.getError().getMessage())
                                                         ));
                                             } else {
                                                 return Task.forResult(null);
                                             }
                                 });
                            }
//                        }
                    });
//                    if (response.isSuccessful()) {
//                        final JSONObject obj = new JSONObject()
//                                .put(FileUploading.ID, uplId)
//                                .put(FileUploading.UPLOADED_SIZE, offset);
//                        realmHelper.executeTransaction(realm ->
//                                     realm.createOrUpdateObjectFromJson(FileUploading.class, obj));
//                    } else {
//                        return Task.forError(new Exception(response.message()));
//                    }
//                }
            }
            return  null;
//            return methodCall.ufsComplete(fileId, token, store);
        }).continueWithTask(task -> {
            if (task.isFaulted()) {
                realmHelper.executeTransaction(realm -> {
                    return realm.createOrUpdateObjectFromJson(FileUploading.class, new JSONObject()
                            .put(FileUploading.ID, uplId)
                            .put(FileUploading.SYNC_STATE, SyncState.FAILED)
                            .put(FileUploading.ERROR,task.getError().getMessage())
                    );
                });
            }
            return Task.forResult(null);
        });

    }
    @NonNull
    private ProgressListener getUploadListener() {
        return new ProgressListener() {
            @Override
            public void onProgress(ProgressInfo progressInfo) {
                // 如果你不屏蔽用户重复点击上传或下载按钮,就可能存在同一个 Url 地址,上一次的上传或下载操作都还没结束,
                // 又开始了新的上传或下载操作,那现在就需要用到 id(请求开始时的时间) 来区分正在执行的进度信息
                // 这里我就取最新的上传进度用来展示,顺便展示下 id 的用法

//                if (mLastUploadingingInfo == null) {
//                    mLastUploadingingInfo = progressInfo;
//                }

                //因为是以请求开始时的时间作为 Id ,所以值越大,说明该请求越新
                if (progressInfo.getId() < progressInfo.getId()) {
                    return;
                } else if (progressInfo.getId() > progressInfo.getId()) {
                    progressInfo = progressInfo;
                }
                long offset = progressInfo.getCurrentbytes();
                int progress = progressInfo.getPercent();
                JSONObject obj = null;
                try {
                    obj = new JSONObject()
                            .put(FileUploading.ID, uplId)
                            .put(FileUploading.UPLOADED_SIZE, (int) offset);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                JSONObject finalObj = obj;
                realmHelper.executeTransaction(realm ->
                        realm.createOrUpdateObjectFromJson(FileUploading.class, finalObj));
                Log.d("Upload", "--Upload-- " + progress + " %  " + progressInfo.getSpeed() + " byte/s  " + progressInfo.toString());
                if (progressInfo.isFinish()) {
                    //说明已经上传完成
                    Log.d("Upload", "--Upload-- finish");
                }
            }

            @Override
            public void onError(long id, Exception e) {

            }
        };
    }

    public static JSONObject customizeJson(JSONObject jsonObject) throws JSONException {
        if (!jsonObject.isNull("path")) {
            try {
                String  path = jsonObject.getString("path");
                jsonObject.remove("path");
                jsonObject.put("path", Uri.decode(path));
            } catch (JSONException e) {
            }
        }
        if (!jsonObject.isNull("url")) {
            try {
                String  url = jsonObject.getString("url");
                jsonObject.remove("url");
                jsonObject.put("url", Uri.decode(url));
            } catch (JSONException e) {
            }
        }
        return jsonObject;
    }
}
