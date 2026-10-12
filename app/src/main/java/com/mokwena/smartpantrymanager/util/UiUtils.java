package com.mokwena.smartpantrymanager.util;

import android.app.Activity;
import android.content.Intent;
import android.view.View;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.mokwena.smartpantrymanager.MainActivity;
import com.mokwena.smartpantrymanager.R;
import com.mokwena.smartpantrymanager.RecipesActivity;
import com.mokwena.smartpantrymanager.SettingsActivity;

/** Android-specific UI helpers shared by the screens. */
public final class UiUtils {

    private UiUtils() { }

    /** Keeps the header below the status bar and the bottom bar above the gesture bar. */
    public static void applyInsets(View root, View header) {
        final int headerTop = header == null ? 0 : header.getPaddingTop();
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, 0, bars.right, bars.bottom);
            if (header != null) {
                header.setPadding(header.getPaddingLeft(), headerTop + bars.top,
                        header.getPaddingRight(), header.getPaddingBottom());
            }
            return insets;
        });
    }

    /** Wires the bottom navigation bar; each tab opens its Activity using an Intent. */
    public static void setupNav(Activity current, int selectedId) {
        BottomNavigationView nav = current.findViewById(R.id.bottom_nav);
        nav.setSelectedItemId(selectedId);
        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == selectedId) return true;
            Intent intent;
            if (id == R.id.nav_pantry) {
                intent = new Intent(current, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            } else {
                intent = new Intent(current, id == R.id.nav_recipes
                        ? RecipesActivity.class : SettingsActivity.class);
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            current.startActivity(intent);
            if (!(current instanceof MainActivity)) current.finish();
            return true;
        });
    }
}
