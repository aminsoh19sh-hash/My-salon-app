package com.mysalonypro.salony;

import org.json.JSONObject;

public class AppointmentsFragment extends BaseListFragment {
    @Override
    protected String path() {
        String email = Session.email(requireContext());
        if (!email.isEmpty()) {
            return "/api/customers/appointments?email=" + android.net.Uri.encode(email);
        }
        return "/api/appointments/me";
    }
    @Override protected int emptyText() { return R.string.empty_appts; }

    @Override
    protected RowAdapter.Row toRow(JSONObject o) {
        RowAdapter.Row r = new RowAdapter.Row();
        r.id = Api.pick(o, "id", "_id");
        r.title = Api.pick(o, "service_name", "serviceName", "salon_name", "salonName", "title");
        if (r.title.isEmpty()) r.title = getString(R.string.appointment);
        String d = Api.pick(o, "date", "starts_at", "start_time");
        String t = Api.pick(o, "time");
        r.sub = (d + " " + t).trim();
        r.extra = status(Api.pick(o, "status"));
        return r;
    }

    private String status(String s) {
        if (!isAdded()) return s;
        switch (s.toLowerCase()) {
            case "pending": return getString(R.string.status_pending);
            case "confirmed": return getString(R.string.status_confirmed);
            case "completed": return getString(R.string.status_completed);
            case "cancelled": case "canceled": return getString(R.string.status_cancelled);
            default: return s;
        }
    }
}
