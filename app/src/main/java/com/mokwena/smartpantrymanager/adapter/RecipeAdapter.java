package com.mokwena.smartpantrymanager.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mokwena.smartpantrymanager.R;
import com.mokwena.smartpantrymanager.model.Recipe;

import java.util.ArrayList;
import java.util.List;

/** Custom adapter that lists the suggested recipes as cards. */
public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.ViewHolder> {

    public interface Listener {
        void onRecipeClick(Recipe recipe);
    }

    private final List<Recipe> recipes = new ArrayList<>();
    private final Listener listener;

    public RecipeAdapter(Listener listener) {
        this.listener = listener;
    }

    public void setRecipes(List<Recipe> newRecipes) {
        recipes.clear();
        recipes.addAll(newRecipes);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recipe, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        final Recipe recipe = recipes.get(position);
        int n = recipe.getIngredients().size();
        h.name.setText(recipe.getName());
        h.info.setText(n + (n == 1 ? " ingredient" : " ingredients") + "  \u2022  ready to cook \u2713");
        h.itemView.setOnClickListener(v -> listener.onRecipeClick(recipe));
    }

    @Override
    public int getItemCount() {
        return recipes.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView name, info;

        ViewHolder(@NonNull View v) {
            super(v);
            name = v.findViewById(R.id.tv_recipe_name);
            info = v.findViewById(R.id.tv_recipe_info);
        }
    }
}
