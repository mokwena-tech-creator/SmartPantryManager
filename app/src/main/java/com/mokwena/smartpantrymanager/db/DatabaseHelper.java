
package com.mokwena.smartpantrymanager.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.mokwena.smartpantrymanager.model.PantryItem;
import com.mokwena.smartpantrymanager.model.Recipe;
import com.mokwena.smartpantrymanager.model.RecipeIngredient;

import java.util.ArrayList;
import java.util.List;

/**
 * Local SQLite database: pantry_items (CRUD), recipes and recipe_ingredients
 * (seeded on first run). Data is stored in a file on the device, so it
 * persists after the app is closed and reopened.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "smart_pantry.db";
    private static final int DB_VERSION = 1;

    private static final String T_PANTRY = "pantry_items";
    private static final String T_RECIPES = "recipes";
    private static final String T_ING = "recipe_ingredients";

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    /** Runs every time the database opens: switches on foreign-key checking. */
    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    /** Runs once, when the database file is first created: builds the tables and loads the recipes. */
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + T_PANTRY + " (id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "name TEXT NOT NULL, quantity REAL NOT NULL, unit TEXT NOT NULL, expiry_date TEXT)");
        db.execSQL("CREATE TABLE " + T_RECIPES + " (id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "name TEXT NOT NULL, steps TEXT NOT NULL)");
        db.execSQL("CREATE TABLE " + T_ING + " (id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "recipe_id INTEGER NOT NULL, ingredient_name TEXT NOT NULL, "
                + "quantity REAL NOT NULL, unit TEXT NOT NULL, "
                + "FOREIGN KEY(recipe_id) REFERENCES " + T_RECIPES + "(id) ON DELETE CASCADE)");
        seedRecipes(db);
    }

    /** Runs when DB_VERSION goes up: drops every table and rebuilds it (this also clears the pantry). */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + T_ING);
        db.execSQL("DROP TABLE IF EXISTS " + T_RECIPES);
        db.execSQL("DROP TABLE IF EXISTS " + T_PANTRY);
        onCreate(db);
    }

    // ------------------------------------------------------------------ PANTRY CRUD

    /** CREATE: adds a new ingredient row and returns its new id. */
    public long insertPantryItem(PantryItem item) {
        return getWritableDatabase().insert(T_PANTRY, null, toValues(item));
    }

    /** READ: returns every pantry item, sorted by name, ready for the RecyclerView. */
    public List<PantryItem> getAllPantryItems() {
        List<PantryItem> list = new ArrayList<>();
        try (Cursor c = getReadableDatabase().query(T_PANTRY, null, null, null,
                null, null, "name COLLATE NOCASE ASC")) {
            while (c.moveToNext()) list.add(fromCursor(c));
        }
        return list;
    }

    /** READ: returns the single pantry item with this id, or null if it does not exist. */
    public PantryItem getPantryItem(long id) {
        try (Cursor c = getReadableDatabase().query(T_PANTRY, null, "id = ?",
                new String[]{String.valueOf(id)}, null, null, null)) {
            return c.moveToFirst() ? fromCursor(c) : null;
        }
    }

    /** UPDATE: overwrites the row that has the same id as the item. */
    public int updatePantryItem(PantryItem item) {
        return getWritableDatabase().update(T_PANTRY, toValues(item), "id = ?",
                new String[]{String.valueOf(item.getId())});
    }

    /** DELETE: removes the row with this id from the pantry. */
    public int deletePantryItem(long id) {
        return getWritableDatabase().delete(T_PANTRY, "id = ?", new String[]{String.valueOf(id)});
    }

    /** Removes every pantry item (used by "Clear pantry" in Settings). Recipes are not affected. */
    public void deleteAllPantryItems() {
        getWritableDatabase().delete(T_PANTRY, null, null);
    }

    /** Packs a PantryItem into a ContentValues bag (column name and value pairs) for insert/update. */
    private ContentValues toValues(PantryItem item) {
        ContentValues cv = new ContentValues();
        cv.put("name", item.getName());
        cv.put("quantity", item.getQuantity());
        cv.put("unit", item.getUnit());
        cv.put("expiry_date", item.getExpiryDate());
        return cv;
    }

    /** Turns the row the cursor is currently on back into a PantryItem object. */
    private PantryItem fromCursor(Cursor c) {
        return new PantryItem(
                c.getLong(c.getColumnIndexOrThrow("id")),
                c.getString(c.getColumnIndexOrThrow("name")),
                c.getDouble(c.getColumnIndexOrThrow("quantity")),
                c.getString(c.getColumnIndexOrThrow("unit")),
                c.getString(c.getColumnIndexOrThrow("expiry_date")));
    }

    // ------------------------------------------------------------------ RECIPES

    /** Loads every recipe together with its ingredient list, used by the strict matcher. */
    public List<Recipe> getAllRecipes() {
        List<Recipe> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor c = db.query(T_RECIPES, null, null, null, null, null, "name ASC")) {
            while (c.moveToNext()) list.add(recipeFromCursor(db, c));
        }
        return list;
    }

    /** Loads one recipe, with its ingredients, for the Recipe Detail screen (null if not found). */
    public Recipe getRecipe(long id) {
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor c = db.query(T_RECIPES, null, "id = ?",
                new String[]{String.valueOf(id)}, null, null, null)) {
            return c.moveToFirst() ? recipeFromCursor(db, c) : null;
        }
    }

    /** Builds a Recipe from the current row, then fetches its ingredient rows using the recipe_id link. */
    private Recipe recipeFromCursor(SQLiteDatabase db, Cursor c) {
        Recipe r = new Recipe(
                c.getLong(c.getColumnIndexOrThrow("id")),
                c.getString(c.getColumnIndexOrThrow("name")),
                c.getString(c.getColumnIndexOrThrow("steps")));
        List<RecipeIngredient> ings = new ArrayList<>();
        try (Cursor ic = db.query(T_ING, null, "recipe_id = ?",
                new String[]{String.valueOf(r.getId())}, null, null, "id ASC")) {
            while (ic.moveToNext()) {
                ings.add(new RecipeIngredient(
                        ic.getLong(ic.getColumnIndexOrThrow("id")), r.getId(),
                        ic.getString(ic.getColumnIndexOrThrow("ingredient_name")),
                        ic.getDouble(ic.getColumnIndexOrThrow("quantity")),
                        ic.getString(ic.getColumnIndexOrThrow("unit"))));
            }
        }
        r.setIngredients(ings);
        return r;
    }

    // ------------------------------------------------------------------ SEED DATA

    /**
     * Inserts one recipe, then one row per ingredient linked by recipe_id.
     * Each ingredient string is "name|quantity|unit".
     */
    private void addRecipe(SQLiteDatabase db, String name, String steps, String... ingredients) {
        ContentValues rv = new ContentValues();
        rv.put("name", name);
        rv.put("steps", steps);
        long recipeId = db.insert(T_RECIPES, null, rv);
        for (String s : ingredients) {
            String[] p = s.split("\\|");
            ContentValues cv = new ContentValues();
            cv.put("recipe_id", recipeId);
            cv.put("ingredient_name", p[0]);
            cv.put("quantity", Double.parseDouble(p[1]));
            cv.put("unit", p[2]);
            db.insert(T_ING, null, cv);
        }
    }

    /** Pre-loads the 18 recipes the first time the database is created. */
    private void seedRecipes(SQLiteDatabase db) {
        addRecipe(db, "Scrambled Eggs on Toast",
                "1. Toast the bread.\n2. Beat the eggs with the salt.\n3. Melt the butter in a pan on low heat.\n4. Stir the eggs gently until just set.\n5. Pile onto the toast and serve.",
                "egg|3|piece", "bread|2|piece", "butter|10|g", "salt|1|tsp");
        addRecipe(db, "Tomato Pasta",
                "1. Boil the pasta in salted water until al dente.\n2. Fry chopped onion and garlic in olive oil.\n3. Add chopped tomatoes and simmer for 15 minutes.\n4. Toss the drained pasta through the sauce.",
                "pasta|200|g", "tomato|3|piece", "onion|1|piece", "garlic|2|piece", "olive oil|2|tbsp", "salt|1|tsp");
        addRecipe(db, "Egg Fried Rice",
                "1. Cook the rice and let it cool.\n2. Fry the chopped onion in oil.\n3. Push aside, scramble the eggs in the pan.\n4. Add the rice and soy sauce and stir-fry for 5 minutes.",
                "rice|200|g", "egg|2|piece", "onion|1|piece", "soy sauce|2|tbsp", "oil|1|tbsp");
        addRecipe(db, "Fluffy Pancakes",
                "1. Whisk flour, sugar, egg and milk into a smooth batter.\n2. Melt a little butter in a pan.\n3. Pour in small ladles of batter.\n4. Flip when bubbles appear and cook until golden.",
                "flour|150|g", "milk|200|ml", "egg|1|piece", "sugar|1|tbsp", "butter|20|g");
        addRecipe(db, "Cheese Omelette",
                "1. Beat the eggs with the salt.\n2. Melt the butter in a non-stick pan.\n3. Pour in the eggs and cook until nearly set.\n4. Add the cheese, fold over and serve.",
                "egg|3|piece", "cheese|50|g", "butter|10|g", "salt|1|tsp");
        addRecipe(db, "Peanut Butter Banana Toast",
                "1. Toast the bread.\n2. Spread with peanut butter.\n3. Slice the banana on top and serve.",
                "bread|2|piece", "peanut butter|2|tbsp", "banana|1|piece");
        addRecipe(db, "Creamy Mashed Potato",
                "1. Peel and cube the potatoes.\n2. Boil for 15-20 minutes until soft.\n3. Drain and mash with butter, milk and salt.",
                "potato|500|g", "butter|30|g", "milk|50|ml", "salt|1|tsp");
        addRecipe(db, "Chicken Rice Bowl",
                "1. Cut the chicken into pieces and season with salt.\n2. Fry with chopped onion in oil until cooked through.\n3. Cook the rice.\n4. Serve the chicken over the rice.",
                "chicken|300|g", "rice|200|g", "onion|1|piece", "oil|1|tbsp", "salt|1|tsp");
        addRecipe(db, "Tuna Mayo Sandwich",
                "1. Drain the tuna and mix with mayonnaise.\n2. Spread on a slice of bread.\n3. Top with the second slice and cut in half.",
                "tuna|1|piece", "mayonnaise|2|tbsp", "bread|2|piece");
        addRecipe(db, "Simple Vegetable Soup",
                "1. Chop the carrots, potatoes and onion.\n2. Simmer everything in the stock for 25 minutes.\n3. Season with salt and blend if you like it smooth.",
                "carrot|2|piece", "potato|2|piece", "onion|1|piece", "stock|500|ml", "salt|1|tsp");
        addRecipe(db, "Garlic Bread",
                "1. Mash the butter with crushed garlic.\n2. Spread over the bread slices.\n3. Bake at 200 C for 8-10 minutes until crisp.",
                "bread|4|piece", "butter|40|g", "garlic|2|piece");
        addRecipe(db, "Beans on Toast",
                "1. Warm the beans in a small pot.\n2. Toast and butter the bread.\n3. Spoon the beans over the toast.",
                "beans|200|g", "bread|2|piece", "butter|10|g");
        addRecipe(db, "Banana Honey Oatmeal",
                "1. Simmer the oats in milk for 5 minutes, stirring.\n2. Pour into a bowl.\n3. Top with sliced banana and a drizzle of honey.",
                "oats|60|g", "milk|250|ml", "banana|1|piece", "honey|1|tbsp");
        addRecipe(db, "Cheese Toastie",
                "1. Butter the outside of both bread slices.\n2. Put the cheese between them.\n3. Toast in a pan on medium heat until golden on both sides.",
                "bread|2|piece", "cheese|40|g", "butter|10|g");
        addRecipe(db, "Spaghetti Aglio e Olio",
                "1. Boil the pasta in salted water.\n2. Gently warm sliced garlic and chili flakes in olive oil.\n3. Toss the drained pasta in the oil and serve.",
                "spaghetti|200|g", "garlic|4|piece", "olive oil|4|tbsp", "chili flakes|1|tsp", "salt|1|tsp");
        addRecipe(db, "Yoghurt Fruit Bowl",
                "1. Spoon the yoghurt into a bowl.\n2. Add sliced banana and oats.\n3. Finish with a drizzle of honey.",
                "yoghurt|200|g", "banana|1|piece", "honey|1|tbsp", "oats|30|g");
        addRecipe(db, "Mac and Cheese",
                "1. Cook the macaroni and drain.\n2. Melt butter, stir in flour, then slowly whisk in the milk.\n3. Stir in the cheese until melted.\n4. Combine with the macaroni.",
                "macaroni|200|g", "cheese|100|g", "milk|200|ml", "butter|20|g", "flour|2|tbsp");
        addRecipe(db, "French Toast",
                "1. Whisk the eggs, milk and sugar.\n2. Dip each bread slice in the mixture.\n3. Fry in butter until golden on both sides.",
                "bread|4|piece", "egg|2|piece", "milk|100|ml", "butter|10|g", "sugar|1|tbsp");
    }
}

