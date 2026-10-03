package com.mysalonypro.salony;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class RowAdapter extends RecyclerView.Adapter<RowAdapter.VH> {

    public static class Row {
        public String id = "", title = "", sub = "", extra = "";
        public double value;
        public boolean checked;
        public JSONObject raw;
    }

    public interface Click { void on(Row r); }

    private final List<Row> rows = new ArrayList<>();
    private final boolean selectable;
    private final Click click;

    public RowAdapter(boolean selectable, Click click) {
        this.selectable = selectable;
        this.click = click;
    }

    public void setRows(List<Row> r) {
        rows.clear();
        rows.addAll(r);
        notifyDataSetChanged();
    }

    public List<Row> selected() {
        List<Row> out = new ArrayList<>();
        for (Row r : rows) if (r.checked) out.add(r);
        return out;
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup p, int t) {
        return new VH(LayoutInflater.from(p.getContext()).inflate(R.layout.item_row, p, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        Row r = rows.get(pos);
        h.title.setText(r.title);
        h.sub.setText(r.sub);
        h.sub.setVisibility(r.sub.isEmpty() ? View.GONE : View.VISIBLE);
        h.extra.setText(r.extra);
        h.check.setVisibility(selectable ? View.VISIBLE : View.GONE);
        h.check.setChecked(r.checked);
        h.itemView.setOnClickListener(v -> {
            if (selectable) { r.checked = !r.checked; h.check.setChecked(r.checked); }
            if (click != null) click.on(r);
        });
    }

    @Override public int getItemCount() { return rows.size(); }

    static class VH extends RecyclerView.ViewHolder {
        final TextView title, sub, extra;
        final CheckBox check;
        VH(View v) {
            super(v);
            title = v.findViewById(R.id.title);
            sub = v.findViewById(R.id.sub);
            extra = v.findViewById(R.id.extra);
            check = v.findViewById(R.id.check);
        }
    }
}
