package com.mokwena.smartpantrymanager;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.mokwena.smartpantrymanager.db.DatabaseHelper;
import com.mokwena.smartpantrymanager.model.Recipe;
import com.mokwena.smartpantrymanager.model.RecipeIngredient;
import com.mokwena.smartpantrymanager.util.PantryUtils;
import com.mokwena.smartpantrymanager.util.UiUtils;

/** Shows the full ingredient list and method for the recipe passed in the Intent. */
public class RecipeDetailActivity extends AppCompatActivity {

    public static final String EXTRA_RECIPE_ID = "recipe_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);
        UiUtils.applyInsets(findViewById(R.id.root), findViewById(R.id.header));
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        long id = getIntent().getLongExtra(EXTRA_RECIPE_ID, -1);
        Recipe recipe = new DatabaseHelper(this).getRecipe(id);
        if (recipe == null) {
            finish();
            return;
        }

        ((TextView) findViewById(R.id.tv_title)).setText(recipe.getName());
        ((TextView) findViewById(R.id.subtitle)).setText(
                recipe.getIngredients().size() + " ingredients  \u2022  all in your pantry");

        StringBuilder sb = new StringBuilder();
        for (RecipeIngredient ing : recipe.getIngredients()) {
            String qty = PantryUtils.formatQty(ing.getQuantity());
            String line = ing.getUnit().equals("piece")
                    ? qty + " \u00D7 " + ing.getName()
                    : qty + " " + ing.getUnit() + "  " + ing.getName();
            sb.append("\u2022  ").append(line).append("\n");
        }
        ((TextView) findViewById(R.id.tv_ingredients)).setText(sb.toString().trim());
        ((TextView) findViewById(R.id.tv_steps)).setText(recipe.getSteps());
    }
}
