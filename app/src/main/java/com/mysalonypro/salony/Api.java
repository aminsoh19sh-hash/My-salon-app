package com.mysalonypro.salony;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** عميل HTTP بسيط للتحدث مع خادم Salon Pro. */
public final class Api {
    private Api() {}

    public interface Callback {
        void onSuccess(Object data);   // JSONObject أو JSONArray
        void onError(String message);
    }

    private static final ExecutorService IO = Executors.newFixedThreadPool(3);
    private static final Handler UI = new Handler(Looper.getMainLooper());

    public static void call(Context ctx, String method, String path, JSONObject body, Callback cb) {
        final Context app = ctx.getApplicationContext();
        IO.execute(() -> {
            HttpURLConnection con = null;
            try {
                con = (HttpURLConnection) new URL(Config.BASE_URL + path).openConnection();
                con.setRequestMethod(method);
                con.setConnectTimeout(10000);
                con.setReadTimeout(15000);
                con.setRequestProperty("Accept", "application/json");
                String token = Session.token(app);
                if (token != null) con.setRequestProperty("Authorization", "Bearer " + token);
                if (body != null) {
                    con.setDoOutput(true);
                    con.setRequestProperty("Content-Type", "application/json; charset=utf-8");
                    try (OutputStream os = con.getOutputStream()) {
                        os.write(body.toString().getBytes(StandardCharsets.UTF_8));
                    }
                }
                int code = con.getResponseCode();
                String text = read(code >= 400 ? con.getErrorStream() : con.getInputStream());
                if (code >= 400) {
                    String msg = "خطأ " + code;
                    try {
                        JSONObject e = new JSONObject(text);
                        msg = e.optString("error", e.optString("message", msg));
                    } catch (Exception ignored) { }
                    fail(cb, msg);
                } else {
                    Object data = text.trim().isEmpty() ? new JSONObject() : new JSONTokener(text).nextValue();
                    UI.post(() -> cb.onSuccess(data));
                }
            } catch (Exception e) {
                fail(cb, app.getString(R.string.server_error));
            } finally {
                if (con != null) con.disconnect();
            }
        });
    }

    private static void fail(Callback cb, String msg) { UI.post(() -> cb.onError(msg)); }

    private static String read(InputStream is) throws Exception {
        if (is == null) return "";
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buf = new byte[4096];
        int n;
        while ((n = is.read(buf)) > 0) out.write(buf, 0, n);
        return out.toString("UTF-8");
    }

    /** يقبل مصفوفة مباشرة أو كائناً يحوي مصفوفة (data / items / salons ...). */
    public static JSONArray toArray(Object d) {
        if (d instanceof JSONArray) return (JSONArray) d;
        if (d instanceof JSONObject) {
            JSONObject o = (JSONObject) d;
            Iterator<String> it = o.keys();
            while (it.hasNext()) {
                Object v = o.opt(it.next());
                if (v instanceof JSONArray) return (JSONArray) v;
            }
        }
        return new JSONArray();
    }

    /** أول مفتاح غير فارغ من القائمة. */
    public static String pick(JSONObject o, String... keys) {
        for (String k : keys) {
            String v = o.optString(k, "");
            if (!v.isEmpty() && !v.equals("null")) return v;
        }
        return "";
    }
}
