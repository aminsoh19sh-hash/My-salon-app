package com.mysalonypro.salony;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.RequestConfiguration;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.ump.ConsentInformation;
import com.google.android.ump.ConsentRequestParameters;
import com.google.android.ump.UserMessagingPlatform;

/**
 * إعلانات AdMob التجريبية بشكل قانوني:
 * 1) نموذج موافقة Google الرسمي (UMP) قبل أي طلب إعلان.
 * 2) تطبيق غير موجّه للأطفال + تصنيف محتوى PG.
 * 3) زر "إدارة خيارات الإعلانات" متاح للمستخدم دائماً.
 * 4) معرّفات Google التجريبية فقط.
 */
public final class AdsHelper {
    private AdsHelper() {}

    private static boolean initialized = false;
    private static InterstitialAd interstitial;

    /** يطلب الموافقة (عند اللزوم) ثم يهيّئ AdMob ثم ينفّذ onReady. */
    public static void start(Activity a, Runnable onReady) {
        ConsentInformation ci = UserMessagingPlatform.getConsentInformation(a);
        ci.requestConsentInfoUpdate(a, new ConsentRequestParameters.Builder().build(),
                () -> UserMessagingPlatform.loadAndShowConsentFormIfRequired(a, formError -> {
                    if (ci.canRequestAds()) init(a, onReady);
                }),
                error -> { if (ci.canRequestAds()) init(a, onReady); });
        // إن كانت الموافقة محفوظة مسبقاً يمكن التهيئة فوراً
        if (ci.canRequestAds()) init(a, onReady);
    }

    private static synchronized void init(Context c, Runnable onReady) {
        if (initialized) { if (onReady != null) new Handler(Looper.getMainLooper()).post(onReady); return; }
        initialized = true;
        MobileAds.setRequestConfiguration(new RequestConfiguration.Builder()
                .setTagForChildDirectedTreatment(RequestConfiguration.TAG_FOR_CHILD_DIRECTED_TREATMENT_FALSE)
                .setTagForUnderAgeOfConsent(RequestConfiguration.TAG_FOR_UNDER_AGE_OF_CONSENT_FALSE)
                .setMaxAdContentRating(RequestConfiguration.MAX_AD_CONTENT_RATING_PG)
                .build());
        MobileAds.initialize(c.getApplicationContext(), status ->
                new Handler(Looper.getMainLooper()).post(() -> {
                    loadInterstitial(c.getApplicationContext());
                    if (onReady != null) onReady.run();
                }));
    }

    public static void loadBanner(Activity a, FrameLayout container) {
        if (!initialized) return;
        DisplayMetrics dm = a.getResources().getDisplayMetrics();
        int widthDp = (int) (dm.widthPixels / dm.density);
        AdView view = new AdView(a);
        view.setAdUnitId(Config.ADMOB_BANNER);
        view.setAdSize(AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(a, widthDp));
        container.removeAllViews();
        container.addView(view);
        view.loadAd(new AdRequest.Builder().build());
    }

    public static void loadInterstitial(Context c) {
        InterstitialAd.load(c, Config.ADMOB_INTERSTITIAL, new AdRequest.Builder().build(),
                new InterstitialAdLoadCallback() {
                    @Override public void onAdLoaded(@NonNull InterstitialAd ad) { interstitial = ad; }
                    @Override public void onAdFailedToLoad(@NonNull LoadAdError e) { interstitial = null; }
                });
    }

    /** يعرض إعلاناً بينياً في لحظة طبيعية (بعد إتمام حجز) ثم ينفّذ after. */
    public static void showInterstitial(Activity a, Runnable after) {
        if (interstitial == null) { after.run(); return; }
        InterstitialAd ad = interstitial;
        interstitial = null;
        ad.setFullScreenContentCallback(new FullScreenContentCallback() {
            @Override public void onAdDismissedFullScreenContent() {
                loadInterstitial(a.getApplicationContext());
                after.run();
            }
            @Override public void onAdFailedToShowFullScreenContent(@NonNull AdError e) { after.run(); }
        });
        ad.show(a);
    }

    /** يفتح نموذج تغيير الموافقة إن كان مطلوباً في منطقة المستخدم. */
    public static void showPrivacyOptions(Activity a) {
        ConsentInformation ci = UserMessagingPlatform.getConsentInformation(a);
        ci.requestConsentInfoUpdate(a, new ConsentRequestParameters.Builder().build(), () -> {
            if (ci.getPrivacyOptionsRequirementStatus()
                    == ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED) {
                UserMessagingPlatform.showPrivacyOptionsForm(a, e -> { });
            } else {
                Toast.makeText(a, R.string.no_ad_options, Toast.LENGTH_LONG).show();
            }
        }, e -> Toast.makeText(a, R.string.no_ad_options, Toast.LENGTH_LONG).show());
    }
}
