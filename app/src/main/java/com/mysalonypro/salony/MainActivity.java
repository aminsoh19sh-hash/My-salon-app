package com.mysalonypro.salony;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Session.token(this) == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }
        setContentView(R.layout.activity_main);
        setSupportActionBar(findViewById(R.id.toolbar));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setSubtitle(getString(R.string.greeting, Session.name(this)));
        }

        BottomNavigationView nav = findViewById(R.id.nav);
        nav.setOnItemSelectedListener(item -> {
            show(item.getItemId() == R.id.nav_salons ? new SalonsFragment() : new AppointmentsFragment());
            return true;
        });
        if (savedInstanceState == null) nav.setSelectedItemId(R.id.nav_salons);

        // الموافقة ثم الإعلانات (بعد قبول سياسة الخصوصية)
        FrameLayout adContainer = findViewById(R.id.ad_container);
        AdsHelper.start(this, () -> AdsHelper.loadBanner(this, adContainer));
    }

    private void show(Fragment f) {
        getSupportFragmentManager().beginTransaction().replace(R.id.container, f).commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_language) {
            toggleLanguage();
            return true;
        }
        if (item.getItemId() == R.id.action_privacy) {
            startActivity(new Intent(this, PrivacyActivity.class).putExtra(PrivacyActivity.EXTRA_VIEW_ONLY, true));
            return true;
        }
        if (item.getItemId() == R.id.action_logout) {
            Session.clear(this);
            Intent i = new Intent(this, LoginActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void toggleLanguage() {
        String current = Session.getLanguage(this);
        String next = "ar".equals(current) ? "en" : "ar";
        LocaleHelper.apply(this, next);
        Intent intent = getIntent();
        finish();
        startActivity(intent);
    }
}
