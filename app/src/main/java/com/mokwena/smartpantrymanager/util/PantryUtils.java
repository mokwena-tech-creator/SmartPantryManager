package com.mokwena.smartpantrymanager.util;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Pure-Java helpers (no Android classes) for name/unit normalisation,
 * quantity formatting and expiry-date maths. Used by the strict matcher.
 */
public final class PantryUtils {

    private PantryUtils() { }

    public static final String PREFS = "pantry_prefs";
    public static final String KEY_EXPIRY_ALERTS = "expiry_alerts";
    public static final int NO_DATE = Integer.MAX_VALUE;

    /** Units offered in the Add/Edit spinner. */
    public static final String[] UNITS = {"g", "kg", "ml", "l", "tsp", "tbsp", "cup", "piece"};

    private static final Map<String, Double> UNIT_FACTOR = new HashMap<>();
    private static final Map<String, String> UNIT_ALIAS = new HashMap<>();
    private static final Map<String, String> SYNONYMS = new HashMap<>();

    static {
        // Conversion to a base amount (1 g is treated as roughly 1 ml)
        UNIT_FACTOR.put("g", 1.0);     UNIT_FACTOR.put("kg", 1000.0);
        UNIT_FACTOR.put("ml", 1.0);    UNIT_FACTOR.put("l", 1000.0);
        UNIT_FACTOR.put("tsp", 5.0);   UNIT_FACTOR.put("tbsp", 15.0);
        UNIT_FACTOR.put("cup", 240.0); UNIT_FACTOR.put("piece", 1.0);

        UNIT_ALIAS.put("gram", "g");     UNIT_ALIAS.put("grams", "g");
        UNIT_ALIAS.put("litre", "l");    UNIT_ALIAS.put("liter", "l");
        UNIT_ALIAS.put("pcs", "piece");  UNIT_ALIAS.put("pc", "piece");
        UNIT_ALIAS.put("pieces", "piece"); UNIT_ALIAS.put("each", "piece");

        // Different words for the same ingredient
        SYNONYMS.put("spaghetti", "pasta"); SYNONYMS.put("penne", "pasta");
        SYNONYMS.put("macaroni", "pasta");  SYNONYMS.put("yogurt", "yoghurt");
        SYNONYMS.put("chilli", "chili");    SYNONYMS.put("capsicum", "pepper");
        SYNONYMS.put("courgette", "zucchini");
    }

    /** Lower-cases, strips symbols, singularises each word and applies synonyms. */
    public static String normalizeName(String raw) {
        if (raw == null) return "";
        String cleaned = raw.toLowerCase(Locale.ROOT).replaceAll("[^a-z ]", " ").trim();
        if (cleaned.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (String word : cleaned.split("\\s+")) {
            String w = singular(word);
            if (SYNONYMS.containsKey(w)) w = SYNONYMS.get(w);
            if (sb.length() > 0) sb.append(' ');
            sb.append(w);
        }
        return sb.toString();
    }

    /** Simple English singularisation: tomatoes -> tomato, berries -> berry, eggs -> egg. */
    static String singular(String w) {
        int n = w.length();
        if (n <= 3) return w;
        if (w.endsWith("ies")) return w.substring(0, n - 3) + "y";
        if (w.endsWith("oes")) return w.substring(0, n - 2);
        if (w.endsWith("ches") || w.endsWith("shes") || w.endsWith("sses") || w.endsWith("xes"))
            return w.substring(0, n - 2);
        if (w.endsWith("ss") || w.endsWith("us")) return w;
        if (w.endsWith("s")) return w.substring(0, n - 1);
        return w;
    }

    private static String canonicalUnit(String unit) {
        String u = unit == null ? "" : unit.toLowerCase(Locale.ROOT).trim();
        return UNIT_ALIAS.containsKey(u) ? UNIT_ALIAS.get(u) : u;
    }

    /** "count" for pieces, "amount" for everything weight/volume based. */
    public static String family(String unit) {
        return canonicalUnit(unit).equals("piece") ? "count" : "amount";
    }

    /** Converts a quantity in any supported unit to its base amount (g/ml/piece). */
    public static double toBase(double qty, String unit) {
        Double f = UNIT_FACTOR.get(canonicalUnit(unit));
        return qty * (f == null ? 1.0 : f);
    }

    /** Whole days from today until yyyy-MM-dd (negative = expired). NO_DATE if blank/invalid. */
    public static int daysUntil(String date) {
        if (date == null || date.trim().isEmpty()) return NO_DATE;
        try {
            SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            f.setLenient(false);
            Date d = f.parse(date.trim());
            Calendar today = Calendar.getInstance();
            today.set(Calendar.HOUR_OF_DAY, 0); today.set(Calendar.MINUTE, 0);
            today.set(Calendar.SECOND, 0);      today.set(Calendar.MILLISECOND, 0);
            return (int) Math.round((d.getTime() - today.getTimeInMillis()) / 86400000.0);
        } catch (Exception e) {
            return NO_DATE;
        }
    }

    /** 2.0 -> "2", 0.25 -> "0.25". */
    public static String formatQty(double q) {
        return new DecimalFormat("0.##", DecimalFormatSymbols.getInstance(Locale.US)).format(q);
    }
}
