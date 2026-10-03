package com.mysalonypro.salony;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        View logo = findViewById(R.id.logo);
        View title = findViewById(R.id.title);
        View tagline = findViewById(R.id.tagline);
        logo.setScaleX(0.3f); logo.setScaleY(0.3f); logo.setAlpha(0f); logo.setRotation(-90f);
        title.setAlpha(0f); title.setTranslationY(40f);
        tagline.setAlpha(0f);

        logo.animate().alpha(1f).scaleX(1f).scaleY(1f).rotation(0f).setDuration(900).start();
        title.animate().alpha(1f).translationY(0f).setStartDelay(500).setDuration(700).start();
        tagline.animate().alpha(1f).setStartDelay(1000).setDuration(700).start();

        new Handler(Looper.getMainLooper()).postDelayed(this::next, 2400);
    }

    private void next() {
        if (isFinishing() || isDestroyed()) return;
        Class<?> target;
        if (!Session.privacyAccepted(this)) target = PrivacyActivity.class;
        else if (Session.token(this) == null) target = LoginActivity.class;
        else target = MainActivity.class;
        startActivity(new Intent(this, target));
        finish();
    }
}
