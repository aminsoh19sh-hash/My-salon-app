package com.mysalonypro.salony;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseListFragment extends Fragment {
    protected abstract String path();
    protected abstract RowAdapter.Row toRow(JSONObject o);
    protected abstract int emptyText();
    protected void onRow(RowAdapter.Row r) { }

    private RowAdapter adapter;
    private SwipeRefreshLayout swipe;
    private TextView empty;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater i, @Nullable ViewGroup c, @Nullable Bundle s) {
        return i.inflate(R.layout.fragment_list, c, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle s) {
        swipe = v.findViewById(R.id.swipe);
        empty = v.findViewById(R.id.empty);
        empty.setText(emptyText());
        RecyclerView rv = v.findViewById(R.id.list);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new RowAdapter(false, this::onRow);
        rv.setAdapter(adapter);
        swipe.setColorSchemeResources(R.color.plum);
        swipe.setOnRefreshListener(this::load);
        load();
    }

    protected void load() {
        swipe.setRefreshing(true);
        Api.call(requireContext(), "GET", path(), null, new Api.Callback() {
            @Override public void onSuccess(Object data) {
                if (!isAdded()) return;
                swipe.setRefreshing(false);
                JSONArray arr = Api.toArray(data);
                List<RowAdapter.Row> rows = new ArrayList<>();
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject o = arr.optJSONObject(i);
                    if (o != null) { RowAdapter.Row r = toRow(o); r.raw = o; rows.add(r); }
                }
                adapter.setRows(rows);
                empty.setVisibility(rows.isEmpty() ? View.VISIBLE : View.GONE);
            }
            @Override public void onError(String m) {
                if (!isAdded()) return;
                swipe.setRefreshing(false);
                Toast.makeText(requireContext(), m, Toast.LENGTH_LONG).show();
            }
        });
    }
}
