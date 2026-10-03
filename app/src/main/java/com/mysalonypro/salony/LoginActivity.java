package com.mysalonypro.salony;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONException;
import org.json.JSONObject;

public class LoginActivity extends BaseActivity {
    private boolean register = false;
    private TextInputLayout nameLayout;
    private TextInputEditText name, email, password;
    private Button submit;
    private TextView toggle;
    private ProgressBar progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        nameLayout = findViewById(R.id.name_layout);
        name = findViewById(R.id.name);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        submit = findViewById(R.id.submit);
        toggle = findViewById(R.id.toggle);
        progress = findViewById(R.id.progress);

        View btnLanguage = findViewById(R.id.btn_language);
        if (btnLanguage != null) {
            btnLanguage.setOnClickListener(v -> toggleLanguage());
        }

        toggle.setOnClickListener(v -> { register = !register; render(); });
        submit.setOnClickListener(v -> submit());
        render();
    }

    private void toggleLanguage() {
        String current = Session.getLanguage(this);
        String next = "ar".equals(current) ? "en" : "ar";
        LocaleHelper.apply(this, next);
        Intent intent = getIntent();
        finish();
        startActivity(intent);
    }

    private void render() {
        nameLayout.setVisibility(register ? View.VISIBLE : View.GONE);
        submit.setText(register ? R.string.register : R.string.login);
        toggle.setText(register ? R.string.have_account : R.string.no_account);
    }

    private void busy(boolean b) {
        progress.setVisibility(b ? View.VISIBLE : View.GONE);
        submit.setEnabled(!b);
    }

    private String text(TextInputEditText t) { return t.getText() == null ? "" : t.getText().toString().trim(); }

    private void submit() {
        String n = text(name), e = text(email), p = text(password);
        if (e.isEmpty() || p.isEmpty() || (register && n.isEmpty())) {
            Toast.makeText(this, R.string.fill_fields, Toast.LENGTH_SHORT).show();
            return;
        }
        JSONObject body = new JSONObject();
        try {
            body.put("email", e);
            body.put("password", p);
            if (register) {
                body.put("name", n);
                body.put("type", "mixed");
            }
        } catch (JSONException ignored) { }
        busy(true);
        auth(register ? "/api/auth/register" : "/api/auth/login", body, register, e);
    }

    private void auth(String path, JSONObject body, boolean retryWithLogin, String emailFallback) {
        Api.call(this, "POST", path, body, new Api.Callback() {
            @Override public void onSuccess(Object d) {
                JSONObject o = d instanceof JSONObject ? (JSONObject) d : new JSONObject();
                String token = o.optString("token", "");
                if (token.isEmpty()) {
                    if (retryWithLogin) {  // بعض الخوادم لا ترجع توكن عند التسجيل
                        JSONObject login = new JSONObject();
                        try { login.put("email", body.optString("email")); login.put("password", body.optString("password")); }
                        catch (JSONException ignored) { }
                        auth("/api/auth/login", login, false, emailFallback);
                    } else {
                        busy(false);
                        Toast.makeText(LoginActivity.this, R.string.unexpected_response, Toast.LENGTH_LONG).show();
                    }
                    return;
                }
                JSONObject user = o.optJSONObject("user");
                if (user == null) user = o.optJSONObject("salon");
                String display = user != null ? Api.pick(user, "name", "full_name", "email")
                        : Api.pick(o, "name", "full_name");
                if (display.isEmpty()) display = emailFallback;
                String phone = user != null ? Api.pick(user, "phone", "telephone") : "";
                Session.save(LoginActivity.this, token, display, emailFallback, phone);
                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                finish();
            }
            @Override public void onError(String m) {
                busy(false);
                Toast.makeText(LoginActivity.this, m, Toast.LENGTH_LONG).show();
            }
        });
    }
}
