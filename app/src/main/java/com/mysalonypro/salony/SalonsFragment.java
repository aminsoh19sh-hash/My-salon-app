package com.mysalonypro.salony;

import android.content.Intent;

import org.json.JSONObject;

public class SalonsFragment extends BaseListFragment {
    @Override protected String path() { return "/api/salons"; }
    @Override protected int emptyText() { return R.string.empty_salons; }

    @Override
    protected RowAdapter.Row toRow(JSONObject o) {
        RowAdapter.Row r = new RowAdapter.Row();
        r.id = Api.pick(o, "id", "_id");
        r.title = Api.pick(o, "name", "title");
        r.sub = Api.pick(o, "address", "city", "location");
        String rating = Api.pick(o, "rating");
        r.extra = rating.isEmpty() ? "" : "★ " + rating;
        return r;
    }

    @Override
    protected void onRow(RowAdapter.Row r) {
        Intent i = new Intent(requireContext(), SalonDetailActivity.class);
        i.putExtra(SalonDetailActivity.EXTRA_ID, r.id);
        i.putExtra(SalonDetailActivity.EXTRA_NAME, r.title);
        startActivity(i);
    }
}
