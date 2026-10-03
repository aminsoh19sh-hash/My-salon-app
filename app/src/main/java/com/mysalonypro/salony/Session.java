package com.mysalonypro.salony;

import android.content.Context;
import android.content.SharedPreferences;

public final class Session {
    private Session() {}

    private static SharedPreferences p(Context c) {
        return c.getSharedPreferences("session", Context.MODE_PRIVATE);
    }

    public static String token(Context c) { return p(c).getString("token", null); }
    public static String name(Context c) { return p(c).getString("name", ""); }
    public static String email(Context c) { return p(c).getString("email", ""); }
    public static String phone(Context c) { return p(c).getString("phone", ""); }

    public static void save(Context c, String token, String name, String email, String phone) {
        p(c).edit()
                .putString("token", token)
                .putString("name", name)
                .putString("email", email)
                .putString("phone", phone)
                .apply();
    }

    public static void save(Context c, String token, String name) {
        save(c, token, name, "", "");
    }

    public static void clear(Context c) {
        p(c).edit().remove("token").remove("name").remove("email").remove("phone").apply();
    }

    public static boolean privacyAccepted(Context c) { return p(c).getBoolean("privacy_ok", false); }
    public static void setPrivacyAccepted(Context c, boolean v) { p(c).edit().putBoolean("privacy_ok", v).apply(); }

    public static String getLanguage(Context c) { return p(c).getString("app_lang", "ar"); }
    public static void setLanguage(Context c, String lang) { p(c).edit().putString("app_lang", lang).apply(); }
}
