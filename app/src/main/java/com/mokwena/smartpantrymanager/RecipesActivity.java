package com.mokwena.smartpantrymanager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mokwena.smartpantrymanager.adapter.RecipeAdapter;
import com.mokwena.smartpantrymanager.db.DatabaseHelper;
import com.mokwena.smartpantrymanager.model.Recipe;
import com.mokwena.smartpantrymanager.util.RecipeMatcher;
import com.mokwena.smartpantrymanager.util.UiUtils;

import java.util.List;

/** Suggested Recipes: runs the strict-matching rule against the current pantry. */
public class RecipesActivity extends AppCompatActivity implements RecipeAdapter.Listener {

    private DatabaseHelper db;
    private RecipeAdapter adapter;
    private RecyclerView recycler;
    private View emptyView;
    private TextView subtitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipes);
        UiUtils.applyInsets(findViewById(R.id.root), findViewById(R.id.header));

        db = new DatabaseHelper(this);
        recycler = findViewById(R.id.recycler);
        emptyView = findViewById(R.id.empty_view);
        subtitle = findViewById(R.id.subtitle);

        adapter = new RecipeAdapter(this);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        recycler.setAdapter(adapter);

        findViewById(R.id.btn_go_pantry).setOnClickListener(v -> {
            Intent i = new Intent(this, MainActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(i);
        });
        UiUtils.setupNav(this, R.id.nav_recipes);
    }

    @Override
    protected void onResume() {
        super.onResume();
        List<Recipe> all = db.getAllRecipes();
        List<Recipe> suggested = RecipeMatcher.getSuggested(all, db.getAllPantryItems());
        adapter.setRecipes(suggested);

        boolean empty = suggested.isEmpty();
        emptyView.setVisibility(empty ? View.VISIBLE : View.GONE);
        recycler.setVisibility(empty ? View.GONE : View.VISIBLE);
        subtitle.setText(suggested.size() + " of " + all.size() + " recipes ready to cook right now");
    }

    @Override
    public void onRecipeClick(Recipe recipe) {
        Intent intent = new Intent(this, RecipeDetailActivity.class);
        intent.putExtra(RecipeDetailActivity.EXTRA_RECIPE_ID, recipe.getId());
        startActivity(intent);
    }
}
