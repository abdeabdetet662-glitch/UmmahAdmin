package com.ummah.admin;

import android.content.Context;
import android.util.Log;

import com.google.auth.oauth2.GoogleCredentials;

import org.json.JSONObject;

import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * FcmSender — إرسال Push Notification عبر FCM HTTP v1 API
 */
public class FcmSender {

    private static final String TAG = "FcmSender";
    private static final String PROJECT_ID = "ummah-15bad";
    private static final String SCOPE = "https://www.googleapis.com/auth/firebase.messaging";
    private static final String FCM_URL = "https://fcm.googleapis.com/v1/projects/"
            + PROJECT_ID + "/messages:send";

    private static String cachedToken = null;
    private static long tokenExpiry = 0;

    private static synchronized String getAccessToken(Context ctx) throws Exception {
        long now = System.currentTimeMillis();
        if (cachedToken != null && now < tokenExpiry - 60_000) {
            return cachedToken;
        }

        InputStream is = ctx.getAssets().open("service-account.json");
        GoogleCredentials cred = GoogleCredentials.fromStream(is).createScoped(SCOPE);
        cred.refreshIfExpired();

        cachedToken = cred.getAccessToken().getTokenValue();
        tokenExpiry = cred.getAccessToken().getExpirationTime().getTime();

        Log.d(TAG, "🔑 OAuth token (" + ((tokenExpiry - now) / 1000) + "s)");
        return cachedToken;
    }

    public static void sendToToken(Context ctx, String fcmToken,
                                    String title, String body,
                                    Map<String, String> data,
                                    Callback cb) {
        new Thread(() -> {
            try {
                if (fcmToken == null || fcmToken.isEmpty()) {
                    if (cb != null) cb.onError("Token فارغ");
                    return;
                }

                String accessToken = getAccessToken(ctx);

                JSONObject notification = new JSONObject();
                notification.put("title", title);
                notification.put("body", body);

                JSONObject androidNotif = new JSONObject();
                androidNotif.put("channel_id", "ummah_default");
                androidNotif.put("sound", "default");

                JSONObject android = new JSONObject();
                android.put("priority", "HIGH");
                android.put("notification", androidNotif);

                JSONObject msg = new JSONObject();
                msg.put("token", fcmToken);
                msg.put("notification", notification);
                msg.put("android", android);

                if (data != null && !data.isEmpty()) {
                    JSONObject dataObj = new JSONObject();
                    for (Map.Entry<String, String> e : data.entrySet()) {
                        dataObj.put(e.getKey(), e.getValue());
                    }
                    msg.put("data", dataObj);
                }

                JSONObject root = new JSONObject();
                root.put("message", msg);

                OkHttpClient client = new OkHttpClient.Builder()
                        .connectTimeout(15, TimeUnit.SECONDS)
                        .writeTimeout(15, TimeUnit.SECONDS)
                        .readTimeout(15, TimeUnit.SECONDS)
                        .build();

                RequestBody reqBody = RequestBody.create(
                        root.toString(),
                        MediaType.parse("application/json; charset=utf-8"));

                Request request = new Request.Builder()
                        .url(FCM_URL)
                        .addHeader("Authorization", "Bearer " + accessToken)
                        .addHeader("Content-Type", "application/json")
                        .post(reqBody)
                        .build();

                Response response = client.newCall(request).execute();
                String resp = response.body() != null ? response.body().string() : "";

                Log.d(TAG, "📤 " + response.code() + " | " + resp);

                if (response.isSuccessful()) {
                    if (cb != null) cb.onSuccess();
                } else {
                    if (cb != null) cb.onError("HTTP " + response.code() + ": " + resp);
                }

            } catch (Exception e) {
                Log.e(TAG, "❌ فشل الإرسال", e);
                if (cb != null) cb.onError(e.getMessage());
            }
        }).start();
    }

    public interface Callback {
        void onSuccess();
        void onError(String error);
    }

    
    /**
     * ═══ إرسال لـ Topic (كل المشتركين) ═══
     * يستعمل topic بدل token — يوصل لجميع الأجهزة
     */
    public static void sendToTopic(Context ctx, String topic,
                                    String title, String body,
                                    Map<String, String> data,
                                    Callback cb) {
        new Thread(() -> {
            try {
                String accessToken = getAccessToken(ctx);

                JSONObject notification = new JSONObject();
                notification.put("title", title);
                notification.put("body", body);

                JSONObject androidNotif = new JSONObject();
                androidNotif.put("channel_id", "ummah_default");
                androidNotif.put("sound", "default");

                JSONObject android = new JSONObject();
                android.put("priority", "HIGH");
                android.put("notification", androidNotif);

                JSONObject msg = new JSONObject();
                msg.put("topic", topic);
                msg.put("notification", notification);
                msg.put("android", android);

                if (data != null && !data.isEmpty()) {
                    JSONObject dataObj = new JSONObject();
                    for (Map.Entry<String, String> e : data.entrySet()) {
                        dataObj.put(e.getKey(), e.getValue());
                    }
                    msg.put("data", dataObj);
                }

                JSONObject root = new JSONObject();
                root.put("message", msg);

                OkHttpClient client = new OkHttpClient.Builder()
                        .connectTimeout(15, TimeUnit.SECONDS)
                        .writeTimeout(15, TimeUnit.SECONDS)
                        .readTimeout(15, TimeUnit.SECONDS)
                        .build();

                RequestBody reqBody = RequestBody.create(
                        root.toString(),
                        MediaType.parse("application/json; charset=utf-8"));

                Request request = new Request.Builder()
                        .url(FCM_URL)
                        .addHeader("Authorization", "Bearer " + accessToken)
                        .addHeader("Content-Type", "application/json")
                        .post(reqBody)
                        .build();

                Response response = client.newCall(request).execute();
                String resp = response.body() != null ? response.body().string() : "";

                Log.d(TAG, "📢 Topic [" + topic + "]: " + response.code() + " | " + resp);

                if (response.isSuccessful()) {
                    if (cb != null) cb.onSuccess();
                } else {
                    if (cb != null) cb.onError("HTTP " + response.code() + ": " + resp);
                }

            } catch (Exception e) {
                Log.e(TAG, "❌ Topic send failed", e);
                if (cb != null) cb.onError(e.getMessage());
            }
        }).start();
    }
}
