package com.mokwena.smartpantrymanager;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.mokwena.smartpantrymanager.adapter.PantryAdapter;
import com.mokwena.smartpantrymanager.db.DatabaseHelper;
import com.mokwena.smartpantrymanager.model.PantryItem;
import com.mokwena.smartpantrymanager.util.PantryUtils;
import com.mokwena.smartpantrymanager.util.UiUtils;

import java.util.List;

/** Home screen: the pantry list (Read + Delete, and entry points for Create and Update). */
public class MainActivity extends AppCompatActivity implements PantryAdapter.Listener {

    private DatabaseHelper db;
    private PantryAdapter adapter;
    private RecyclerView recycler;
    private View emptyView;
    private TextView banner, subtitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        UiUtils.applyInsets(findViewById(R.id.root), findViewById(R.id.header));

        db = new DatabaseHelper(this);
        recycler = findViewById(R.id.recycler);
        emptyView = findViewById(R.id.empty_view);
        banner = findViewById(R.id.banner);
        subtitle = findViewById(R.id.subtitle);

        adapter = new PantryAdapter(this);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        recycler.setAdapter(adapter);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(v -> startActivity(new Intent(this, AddEditItemActivity.class)));

        UiUtils.setupNav(this, R.id.nav_pantry);
    }

    /** Reload from the database every time the screen comes back to the foreground. */
    @Override
    protected void onResume() {
        super.onResume();
        loadItems();
    }

    private void loadItems() {
        boolean alerts = getSharedPreferences(PantryUtils.PREFS, Context.MODE_PRIVATE)
                .getBoolean(PantryUtils.KEY_EXPIRY_ALERTS, true);
        List<PantryItem> items = db.getAllPantryItems();
        adapter.setItems(items, alerts);

        boolean empty = items.isEmpty();
        emptyView.setVisibility(empty ? View.VISIBLE : View.GONE);
        recycler.setVisibility(empty ? View.GONE : View.VISIBLE);
        subtitle.setText(items.size() + (items.size() == 1 ? " ingredient" : " ingredients") + " in your pantry");

        int expiring = 0;
        for (PantryItem p : items) {
            if (PantryUtils.daysUntil(p.getExpiryDate()) <= 3) expiring++;
        }
        if (alerts && expiring > 0) {
            banner.setText("\u26A0\uFE0F  " + expiring + (expiring == 1 ? " item is" : " items are")
                    + " expiring soon or expired");
            banner.setVisibility(View.VISIBLE);
        } else {
            banner.setVisibility(View.GONE);
        }
    }

    @Override
    public void onEdit(PantryItem item) {
        Intent intent = new Intent(this, AddEditItemActivity.class);
        intent.putExtra(AddEditItemActivity.EXTRA_ITEM_ID, item.getId());
        startActivity(intent);
    }

    @Override
    public void onDelete(PantryItem item) {
        new AlertDialog.Builder(this)
                .setTitle("Delete ingredient?")
                .setMessage("Remove " + item.getName() + " from your pantry?")
                .setPositiveButton("Delete", (d, w) -> {
                    db.deletePantryItem(item.getId());
                    Toast.makeText(this, item.getName() + " deleted", Toast.LENGTH_SHORT).show();
                    loadItems();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
