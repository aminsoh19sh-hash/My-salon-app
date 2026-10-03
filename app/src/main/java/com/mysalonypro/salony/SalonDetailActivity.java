package com.mysalonypro.salony;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class SalonDetailActivity extends BaseActivity {
    public static final String EXTRA_ID = "salon_id";
    public static final String EXTRA_NAME = "salon_name";

    private String salonId, date = "", time = "";
    private RowAdapter adapter;
    private ProgressBar progress;
    private TextView total;
    private Button btnDate, btnTime, confirm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        salonId = getIntent().getStringExtra(EXTRA_ID);
        setSupportActionBar(findViewById(R.id.toolbar));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(getIntent().getStringExtra(EXTRA_NAME));
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        progress = findViewById(R.id.progress);
        total = findViewById(R.id.total);
        btnDate = findViewById(R.id.btn_date);
        btnTime = findViewById(R.id.btn_time);
        confirm = findViewById(R.id.confirm);

        RecyclerView rv = findViewById(R.id.list);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RowAdapter(true, r -> updateTotal());
        rv.setAdapter(adapter);

        btnDate.setOnClickListener(v -> pickDate());
        btnTime.setOnClickListener(v -> pickTime());
        confirm.setOnClickListener(v -> book());
        updateTotal();
        loadServices();
    }

    private void loadServices() {
        Api.call(this, "GET", "/api/salons/" + salonId, null, new Api.Callback() {
            @Override public void onSuccess(Object data) {
                progress.setVisibility(View.GONE);
                JSONArray arr = Api.toArray(data);
                List<RowAdapter.Row> rows = new ArrayList<>();
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject o = arr.optJSONObject(i);
                    if (o == null) continue;
                    RowAdapter.Row r = new RowAdapter.Row();
                    r.raw = o;
                    r.id = Api.pick(o, "id", "_id");
                    r.title = Api.pick(o, "name", "title");
                    String dur = Api.pick(o, "duration", "duration_minutes");
                    r.sub = dur.isEmpty() ? "" : getString(R.string.duration_mins, dur);
                    r.value = o.optDouble("price", 0);
                    r.extra = r.value > 0 ? fmt(r.value) + " " + Config.CURRENCY : "";
                    rows.add(r);
                }
                adapter.setRows(rows);
            }
            @Override public void onError(String m) {
                progress.setVisibility(View.GONE);
                Toast.makeText(SalonDetailActivity.this, m, Toast.LENGTH_LONG).show();
            }
        });
    }

    private String fmt(double v) {
        return v == Math.floor(v) ? String.valueOf((long) v) : String.format(Locale.ENGLISH, "%.2f", v);
    }

    private void updateTotal() {
        double sum = 0;
        for (RowAdapter.Row r : adapter.selected()) sum += r.value;
        total.setText(getString(R.string.total, fmt(sum), Config.CURRENCY));
    }

    private void pickDate() {
        Calendar c = Calendar.getInstance();
        DatePickerDialog d = new DatePickerDialog(this, (v, y, m, day) -> {
            date = String.format(Locale.ENGLISH, "%04d-%02d-%02d", y, m + 1, day);
            btnDate.setText(date);
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
        d.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        d.show();
    }

    private void pickTime() {
        new TimePickerDialog(this, (v, h, min) -> {
            time = String.format(Locale.ENGLISH, "%02d:%02d", h, min);
            btnTime.setText(time);
        }, 10, 0, true).show();
    }

    private void book() {
        List<RowAdapter.Row> sel = adapter.selected();
        if (sel.isEmpty()) { toast(R.string.select_service); return; }
        if (date.isEmpty() || time.isEmpty()) { toast(R.string.select_datetime); return; }

        JSONObject body = new JSONObject();
        RowAdapter.Row first = sel.get(0);
        try {
            body.put("salonId", salonId);
            body.put("serviceId", first.id);
            body.put("date", date);
            body.put("time", time);
            String name = Session.name(this);
            body.put("customerName", name.isEmpty() ? "عميل" : name);
            body.put("customerEmail", Session.email(this));
            body.put("customerPhone", Session.phone(this));
            body.put("notes", "");
        } catch (JSONException ignored) { }

        confirm.setEnabled(false);
        Api.call(this, "POST", "/api/appointments", body, new Api.Callback() {
            @Override public void onSuccess(Object data) {
                toast(R.string.booked);
                // إعلان بيني في لحظة طبيعية (بعد إتمام المهمة) ثم إغلاق الشاشة
                AdsHelper.showInterstitial(SalonDetailActivity.this, SalonDetailActivity.this::finish);
            }
            @Override public void onError(String m) {
                confirm.setEnabled(true);
                Toast.makeText(SalonDetailActivity.this, m, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void toast(int res) { Toast.makeText(this, res, Toast.LENGTH_SHORT).show(); }

    @Override public boolean onSupportNavigateUp() { finish(); return true; }
}
