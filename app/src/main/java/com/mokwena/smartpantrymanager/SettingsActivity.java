package com.mokwena.smartpantrymanager;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.materialswitch.MaterialSwitch;
import com.mokwena.smartpantrymanager.db.DatabaseHelper;
import com.mokwena.smartpantrymanager.util.PantryUtils;
import com.mokwena.smartpantrymanager.util.UiUtils;

/** Settings: expiring-soon alert toggle (SharedPreferences) and a pantry reset. */
public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        UiUtils.applyInsets(findViewById(R.id.root), findViewById(R.id.header));

        SharedPreferences prefs = getSharedPreferences(PantryUtils.PREFS, Context.MODE_PRIVATE);
        MaterialSwitch alerts = findViewById(R.id.switch_alerts);
        alerts.setChecked(prefs.getBoolean(PantryUtils.KEY_EXPIRY_ALERTS, true));
        alerts.setOnCheckedChangeListener((b, checked) ->
                prefs.edit().putBoolean(PantryUtils.KEY_EXPIRY_ALERTS, checked).apply());

        findViewById(R.id.btn_reset).setOnClickListener(v ->
                new AlertDialog.Builder(this)
                        .setTitle("Clear pantry?")
                        .setMessage("This removes every ingredient from your pantry. Recipes are not affected.")
                        .setPositiveButton("Clear all", (d, w) -> {
                            new DatabaseHelper(this).deleteAllPantryItems();
                            Toast.makeText(this, "Pantry cleared", Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton("Cancel", null)
                        .show());

        UiUtils.setupNav(this, R.id.nav_settings);
    }
}
