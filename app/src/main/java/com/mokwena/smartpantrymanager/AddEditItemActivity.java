package com.mokwena.smartpantrymanager;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.mokwena.smartpantrymanager.db.DatabaseHelper;
import com.mokwena.smartpantrymanager.model.PantryItem;
import com.mokwena.smartpantrymanager.util.PantryUtils;
import com.mokwena.smartpantrymanager.util.UiUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/** Add a new ingredient, or edit an existing one when an item_id is passed in the Intent. */
public class AddEditItemActivity extends AppCompatActivity {

    public static final String EXTRA_ITEM_ID = "item_id";

    private DatabaseHelper db;
    private long itemId = -1;

    private TextInputLayout nameLayout, qtyLayout, dateLayout;
    private TextInputEditText nameInput, qtyInput, dateInput;
    private Spinner unitSpinner;
    private TextView unitError;
    private List<String> unitOptions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit);
        UiUtils.applyInsets(findViewById(R.id.root), findViewById(R.id.header));

        db = new DatabaseHelper(this);
        nameLayout = findViewById(R.id.layout_name);
        qtyLayout = findViewById(R.id.layout_quantity);
        dateLayout = findViewById(R.id.layout_date);
        nameInput = findViewById(R.id.input_name);
        qtyInput = findViewById(R.id.input_quantity);
        dateInput = findViewById(R.id.input_date);
        unitSpinner = findViewById(R.id.spinner_unit);
        unitError = findViewById(R.id.tv_unit_error);

        unitOptions = new ArrayList<>();
        unitOptions.add("Select unit");
        for (String u : PantryUtils.UNITS) unitOptions.add(u);
        ArrayAdapter<String> unitAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, unitOptions);
        unitSpinner.setAdapter(unitAdapter);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        dateInput.setOnClickListener(v -> showDatePicker());
        findViewById(R.id.btn_clear_date).setOnClickListener(v -> dateInput.setText(""));

        // An item_id extra means "edit"; otherwise this is "add".
        itemId = getIntent().getLongExtra(EXTRA_ITEM_ID, -1);
        TextView title = findViewById(R.id.tv_title);
        MaterialButton save = findViewById(R.id.btn_save);
        if (itemId != -1) {
            title.setText("Edit ingredient");
            save.setText("Save changes");
            PantryItem existing = db.getPantryItem(itemId);
            if (existing != null) {
                nameInput.setText(existing.getName());
                qtyInput.setText(PantryUtils.formatQty(existing.getQuantity()));
                dateInput.setText(existing.getExpiryDate());
                int idx = unitOptions.indexOf(existing.getUnit());
                unitSpinner.setSelection(Math.max(idx, 0));
            }
        } else {
            title.setText("Add ingredient");
        }
        save.setOnClickListener(v -> saveItem());
    }

    private void showDatePicker() {
        Calendar cal = Calendar.getInstance();
        new DatePickerDialog(this, (view, y, m, d) ->
                dateInput.setText(String.format(Locale.US, "%04d-%02d-%02d", y, m + 1, d)),
                cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
    }

    /** Validates the form, then inserts or updates the record. */
    private void saveItem() {
        nameLayout.setError(null);
        qtyLayout.setError(null);
        unitError.setVisibility(View.GONE);
        boolean valid = true;

        String name = nameInput.getText() == null ? "" : nameInput.getText().toString().trim();
        if (name.length() < 2) {
            nameLayout.setError("Enter an ingredient name (at least 2 letters)");
            valid = false;
        } else if (!name.matches("[\\p{L} \\-']+")) {
            nameLayout.setError("Use letters only, no numbers or symbols");
            valid = false;
        }

        double qty = 0;
        String qtyText = qtyInput.getText() == null ? "" : qtyInput.getText().toString().trim();
        if (qtyText.isEmpty()) {
            qtyLayout.setError("Enter a quantity");
            valid = false;
        } else {
            try {
                qty = Double.parseDouble(qtyText);
                if (qty <= 0) {
                    qtyLayout.setError("Quantity must be greater than 0");
                    valid = false;
                } else if (qty > 100000) {
                    qtyLayout.setError("That quantity looks too large");
                    valid = false;
                }
            } catch (NumberFormatException e) {
                qtyLayout.setError("Enter a valid number");
                valid = false;
            }
        }

        if (unitSpinner.getSelectedItemPosition() == 0) {
            unitError.setVisibility(View.VISIBLE);
            valid = false;
        }
        if (!valid) return;

        String unit = unitOptions.get(unitSpinner.getSelectedItemPosition());
        String date = dateInput.getText() == null ? "" : dateInput.getText().toString().trim();

        PantryItem item = new PantryItem(itemId, name, qty, unit, date.isEmpty() ? null : date);
        if (itemId == -1) {
            db.insertPantryItem(item);                       // CREATE
            Toast.makeText(this, name + " added to pantry", Toast.LENGTH_SHORT).show();
        } else {
            db.updatePantryItem(item);                       // UPDATE
            Toast.makeText(this, name + " updated", Toast.LENGTH_SHORT).show();
        }
        finish();
    }
}
