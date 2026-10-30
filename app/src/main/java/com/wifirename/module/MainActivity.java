package com.wifirename.module;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class MainActivity extends Activity {

    private EditText edtSsid;
    private EditText edtLabel;
    private ListView list;
    private ArrayAdapter<String> adapter;
    private final List<String> rows = new ArrayList<>();
    private final List<String> ssids = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        edtSsid = findViewById(R.id.edt_ssid);
        edtLabel = findViewById(R.id.edt_label);
        list = findViewById(R.id.list);

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, rows);
        list.setAdapter(adapter);
        list.setEmptyView(findViewById(R.id.empty));

        findViewById(R.id.btn_save).setOnClickListener(v -> save());
        list.setOnItemClickListener((parent, view, position, id) -> edit(position));
        list.setOnItemLongClickListener((parent, view, position, id) -> {
            delete(position);
            return true;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        Prefs.makeWorldReadable(this);
        refresh();
    }

    private void save() {
        String ssid = edtSsid.getText().toString().trim();
        String label = edtLabel.getText().toString().trim();
        if (ssid.isEmpty()) {
            Toast.makeText(this, R.string.err_empty_ssid, Toast.LENGTH_SHORT).show();
            return;
        }
        if (label.isEmpty()) {
            Toast.makeText(this, R.string.err_empty_label, Toast.LENGTH_SHORT).show();
            return;
        }
        SharedPreferences.Editor editor = getSharedPreferences(Prefs.PREFS_NAME, MODE_PRIVATE).edit();
        editor.putString(ssid, label);
        editor.apply();
        Prefs.makeWorldReadable(this);
        edtSsid.getText().clear();
        edtLabel.getText().clear();
        Toast.makeText(this, R.string.toast_saved, Toast.LENGTH_SHORT).show();
        refresh();
    }

    private void edit(int position) {
        SharedPreferences sp = getSharedPreferences(Prefs.PREFS_NAME, MODE_PRIVATE);
        String ssid = ssids.get(position);
        edtSsid.setText(ssid);
        edtLabel.setText(sp.getString(ssid, ""));
        edtSsid.setSelection(ssid.length());
        edtLabel.requestFocus();
    }

    private void delete(int position) {
        String ssid = ssids.get(position);
        new AlertDialog.Builder(this)
                .setTitle(R.string.delete_title)
                .setMessage(getString(R.string.delete_msg, ssid))
                .setPositiveButton(R.string.btn_delete, (d, w) -> {
                    SharedPreferences.Editor editor = getSharedPreferences(Prefs.PREFS_NAME, MODE_PRIVATE).edit();
                    editor.remove(ssid);
                    editor.apply();
                    Prefs.makeWorldReadable(this);
                    Toast.makeText(this, R.string.toast_removed, Toast.LENGTH_SHORT).show();
                    refresh();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void refresh() {
        Map<String, ?> all = getSharedPreferences(Prefs.PREFS_NAME, MODE_PRIVATE).getAll();
        List<String> sorted = new ArrayList<>(all.keySet());
        Collections.sort(sorted);
        rows.clear();
        ssids.clear();
        for (String ssid : sorted) {
            rows.add(ssid + "  ->  " + String.valueOf(all.get(ssid)));
            ssids.add(ssid);
        }
        adapter.notifyDataSetChanged();
    }
}
