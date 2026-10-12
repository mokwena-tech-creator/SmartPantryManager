package com.mokwena.smartpantrymanager.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mokwena.smartpantrymanager.R;
import com.mokwena.smartpantrymanager.model.PantryItem;
import com.mokwena.smartpantrymanager.util.PantryUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Custom adapter that shows each pantry item as a card inside a RecyclerView. */
public class PantryAdapter extends RecyclerView.Adapter<PantryAdapter.ViewHolder> {

    public interface Listener {
        void onEdit(PantryItem item);
        void onDelete(PantryItem item);
    }

    private final List<PantryItem> items = new ArrayList<>();
    private final Listener listener;
    private boolean highlightExpiry = true;

    public PantryAdapter(Listener listener) {
        this.listener = listener;
    }

    public void setItems(List<PantryItem> newItems, boolean highlightExpiry) {
        items.clear();
        items.addAll(newItems);
        this.highlightExpiry = highlightExpiry;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pantry, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        final PantryItem item = items.get(position);
        String name = item.getName() == null ? "" : item.getName().trim();
        h.avatar.setText(name.isEmpty() ? "?" : name.substring(0, 1).toUpperCase(Locale.getDefault()));
        h.name.setText(name.isEmpty() ? name : name.substring(0, 1).toUpperCase(Locale.getDefault()) + name.substring(1));
        h.quantity.setText(PantryUtils.formatQty(item.getQuantity()) + " " + item.getUnit());

        int days = PantryUtils.daysUntil(item.getExpiryDate());
        if (days == PantryUtils.NO_DATE) {
            style(h.expiry, "No expiry date", R.drawable.bg_chip_ok, R.color.ok_text);
        } else if (days < 0) {
            style(h.expiry, "Expired", highlightExpiry ? R.drawable.bg_chip_bad : R.drawable.bg_chip_ok,
                    highlightExpiry ? R.color.bad_text : R.color.ok_text);
        } else if (days <= 3) {
            String text = days == 0 ? "Expires today" : "Expires in " + days + (days == 1 ? " day" : " days");
            style(h.expiry, text, highlightExpiry ? R.drawable.bg_chip_warn : R.drawable.bg_chip_ok,
                    highlightExpiry ? R.color.warn_text : R.color.ok_text);
        } else {
            style(h.expiry, "Expires " + item.getExpiryDate(), R.drawable.bg_chip_ok, R.color.ok_text);
        }

        h.itemView.setOnClickListener(v -> listener.onEdit(item));
        h.delete.setOnClickListener(v -> listener.onDelete(item));
    }

    private void style(TextView tv, String text, int bgRes, int colorRes) {
        tv.setText(text);
        tv.setBackgroundResource(bgRes);
        tv.setTextColor(tv.getResources().getColor(colorRes, null));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView avatar, name, quantity, expiry;
        final ImageButton delete;

        ViewHolder(@NonNull View v) {
            super(v);
            avatar = v.findViewById(R.id.tv_avatar);
            name = v.findViewById(R.id.tv_name);
            quantity = v.findViewById(R.id.tv_quantity);
            expiry = v.findViewById(R.id.tv_expiry);
            delete = v.findViewById(R.id.btn_delete);
        }
    }
}
