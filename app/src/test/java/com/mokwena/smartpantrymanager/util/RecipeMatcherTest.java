package com.mokwena.smartpantrymanager.util;

import static org.junit.Assert.assertEquals;

import com.mokwena.smartpantrymanager.model.PantryItem;
import com.mokwena.smartpantrymanager.model.Recipe;
import com.mokwena.smartpantrymanager.model.RecipeIngredient;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** Unit tests for the strict-matching rule. */
public class RecipeMatcherTest {

    /** Builds a recipe from strings like "egg|3|piece". */
    private Recipe recipe(String... ingredients) {
        Recipe r = new Recipe(1, "Test recipe", "");
        List<RecipeIngredient> list = new ArrayList<>();
        for (String s : ingredients) {
            String[] p = s.split("\\|");
            list.add(new RecipeIngredient(0, 1, p[0], Double.parseDouble(p[1]), p[2]));
        }
        r.setIngredients(list);
        return r;
    }

    private PantryItem item(String name, double qty, String unit) {
        return new PantryItem(0, name, qty, unit, null);
    }

    private int suggestedCount(Recipe recipe, PantryItem... pantry) {
        return RecipeMatcher.getSuggested(Arrays.asList(recipe), Arrays.asList(pantry)).size();
    }

    @Test
    public void allIngredientsPresent_isSuggested() {
        assertEquals(1, suggestedCount(recipe("egg|3|piece", "bread|2|piece"),
                item("Eggs", 3, "piece"), item("Bread", 2, "piece")));
    }

    @Test
    public void oneIngredientMissing_isExcluded() {
        assertEquals(0, suggestedCount(
                recipe("egg|1|piece", "bread|1|piece", "butter|10|g", "salt|1|tsp", "milk|50|ml"),
                item("Egg", 1, "piece"), item("Bread", 1, "piece"),
                item("Butter", 10, "g"), item("Salt", 1, "tsp")));
    }

    @Test
    public void notEnoughQuantity_isExcluded() {
        assertEquals(0, suggestedCount(recipe("egg|3|piece"), item("Eggs", 2, "piece")));
    }

    @Test
    public void pluralNames_stillMatch() {
        assertEquals(1, suggestedCount(recipe("tomato|3|piece"), item("Tomatoes", 4, "piece")));
    }

    @Test
    public void unitDifferences_stillMatch() {
        assertEquals(1, suggestedCount(recipe("rice|200|g"), item("Rice", 1, "kg")));
    }

    @Test
    public void synonyms_stillMatch() {
        assertEquals(1, suggestedCount(recipe("pasta|200|g"), item("Spaghetti", 500, "g")));
    }

    @Test
    public void emptyPantry_suggestsNothing() {
        assertEquals(0, suggestedCount(recipe("egg|1|piece")));
    }
}