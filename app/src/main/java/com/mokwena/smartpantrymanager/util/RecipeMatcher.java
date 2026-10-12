package com.mokwena.smartpantrymanager.util;

import com.mokwena.smartpantrymanager.model.PantryItem;
import com.mokwena.smartpantrymanager.model.Recipe;
import com.mokwena.smartpantrymanager.model.RecipeIngredient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * THE STRICT-MATCHING RULE: a recipe is suggested only if EVERY ingredient
 * is in the pantry in at least the required quantity. One missing or short
 * ingredient excludes the whole recipe.
 */
public final class RecipeMatcher {

    private RecipeMatcher() { }

    /** Returns only the recipes that can be cooked right now. */
    public static List<Recipe> getSuggested(List<Recipe> recipes, List<PantryItem> pantry) {
        Map<String, Double> stock = buildStock(pantry);
        List<Recipe> result = new ArrayList<>();
        for (Recipe r : recipes) {
            if (canMake(r, stock)) result.add(r);
        }
        return result;
    }

    /** Totals the pantry per normalised ingredient name and unit family, in base units. */
    static Map<String, Double> buildStock(List<PantryItem> pantry) {
        Map<String, Double> stock = new HashMap<>();
        for (PantryItem p : pantry) {
            String key = key(p.getName(), p.getUnit());
            double qty = PantryUtils.toBase(p.getQuantity(), p.getUnit());
            Double current = stock.get(key);
            stock.put(key, (current == null ? 0 : current) + qty);
        }
        return stock;
    }

    static boolean canMake(Recipe recipe, Map<String, Double> stock) {
        if (recipe.getIngredients().isEmpty()) return false;
        for (RecipeIngredient need : recipe.getIngredients()) {
            Double have = stock.get(key(need.getName(), need.getUnit()));
            double required = PantryUtils.toBase(need.getQuantity(), need.getUnit());
            if (have == null || have + 1e-9 < required) {
                return false;   // missing or not enough: reject the whole recipe
            }
        }
        return true;
    }

    private static String key(String name, String unit) {
        return PantryUtils.normalizeName(name) + "|" + PantryUtils.family(unit);
    }
}
