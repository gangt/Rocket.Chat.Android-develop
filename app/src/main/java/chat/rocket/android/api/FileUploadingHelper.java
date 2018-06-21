package chat.rocket.android.api;

import android.content.Context;
import android.net.Uri;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import bolts.Task;
import chat.rocket.android.helper.TextUtils;
import chat.rocket.android.log.RCLog;
import chat.rocket.core.JsonConstants;
import chat.rocket.persistence.realm.RealmHelper;
import chat.rocket.persistence.realm.models.internal.FileUploading;

/**
 * MethodCall for uploading file.
 */
public class FileUploadingHelper extends MethodCallHelper {
    public FileUploadingHelper(Context context, String hostname) {
        super(context, hostname);
    }

    public FileUploadingHelper(RealmHelper realmHelper) {
        super(realmHelper);
    }

    public Task<JSONObject> uploadS3Request(String filename, long filesize, String mimeType,
                                            String roomId) {
        return uploadRequest("rocketchat-uploads", filename, filesize, mimeType, roomId);
    }

    public Task<JSONObject> uploadGoogleRequest(String filename, long filesize, String mimeType,
                                                String roomId) {
        return uploadRequest("rocketchat-uploads-gs", filename, filesize, mimeType, roomId);
    }

    public Task<Void> sendFileMessage(String roomId, String storageType, JSONObject fileObj) {
        return call("sendFileMessage", TIMEOUT_MS, () -> new JSONArray()
                .put(new JSONObject()
                        .put("roomId", roomId)
                        .put("store", TextUtils.isEmpty(storageType) ? JSONObject.NULL : storageType)
                        .put("file", fileObj)))
                .onSuccessTask(task -> Task.forResult(null))
                .continueWithTask(task -> {
                    Object result = task.getResult();
//              RCLog.d("sendFileMessage->>"+task.getResult());
                    return null;
                });
    }

    public Task<JSONObject> ufsCreate(String filename, long filesize, String mimeType, String store,
                                      String roomId) {
        return call("ufsCreate", TIMEOUT_MS, () -> new JSONArray().put(new JSONObject()
                .put("name", filename)
                .put("size", filesize)
                .put("type", mimeType)
                .put("store", store)
                .put("rid", roomId)
        )).onSuccessTask(CONVERT_TO_JSON_OBJECT);
    }

    public Task<JSONObject> ufsComplete(String fileId, String token, String store) {
        return call("ufsComplete", TIMEOUT_MS, () -> new JSONArray()
                .put(fileId)
                .put(store)
                .put(token)
        ).onSuccessTask(CONVERT_TO_JSON_OBJECT);
    }

    private Task<JSONObject> uploadRequest(String uploadType, String filename,
                                           long filesize, String mimeType,
                                           String roomId) {
        return call("slingshot/uploadRequest", TIMEOUT_MS, () -> new JSONArray()
                .put(uploadType)
                .put(new JSONObject()
                        .put("name", filename)
                        .put("size", filesize)
                        .put("type", mimeType))
                .put(new JSONObject().put("rid", roomId)))
                .onSuccessTask(CONVERT_TO_JSON_OBJECT);
    }

    public Task<JSONObject> copyFile(String fileId, String roomId) {
        return call("copyFile", TIMEOUT_MS, () ->
                new JSONArray()
                        .put(new JSONObject()
                                .put("fileId", fileId)
                                .put("roomId", roomId)))
                .onSuccessTask(CONVERT_TO_JSON_OBJECT);
    }
}
