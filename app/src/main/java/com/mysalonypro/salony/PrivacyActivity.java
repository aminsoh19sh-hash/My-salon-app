package com.mysalonypro.salony;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;

import androidx.appcompat.app.AppCompatActivity;

public class PrivacyActivity extends BaseActivity {
    public static final String EXTRA_VIEW_ONLY = "view_only";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy);
        setSupportActionBar(findViewById(R.id.toolbar));

        boolean viewOnly = getIntent().getBooleanExtra(EXTRA_VIEW_ONLY, false);
        CheckBox check = findViewById(R.id.accept_check);
        Button accept = findViewById(R.id.accept_btn);
        Button manage = findViewById(R.id.manage_ads);

        manage.setOnClickListener(v -> AdsHelper.showPrivacyOptions(this));

        if (viewOnly) {
            check.setVisibility(android.view.View.GONE);
            accept.setVisibility(android.view.View.GONE);
            if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        } else {
            accept.setEnabled(false);
            check.setOnCheckedChangeListener((b, checked) -> accept.setEnabled(checked));
            accept.setOnClickListener(v -> {
                Session.setPrivacyAccepted(this, true);
                Class<?> next = Session.token(this) == null ? LoginActivity.class : MainActivity.class;
                startActivity(new Intent(this, next));
                finish();
            });
        }
    }

    @Override
    public boolean onSupportNavigateUp() { finish(); return true; }
}
