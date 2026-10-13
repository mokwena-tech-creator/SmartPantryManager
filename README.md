# Smart Pantry Manager

A Java Android app that helps reduce food waste. The user records the
ingredients they have at home, and the app suggests only the recipes they can
cook right now, with no shopping trip needed.

## Features
- Add, edit and delete pantry items (name, quantity, unit, optional expiry date)
- 18 pre-loaded recipes stored in the database
- Strict matching: a recipe is suggested only if every ingredient is in the
  pantry in at least the required quantity
- Handles plurals (tomato/tomatoes), unit differences (kg/g) and synonyms
  (spaghetti/pasta)
- Expiring-soon highlights and a settings screen
- Input validation and empty-state messages

## Database: SQLite
I chose SQLite (SQLiteOpenHelper) because the app only needs local, on-device
storage, it is built into Android so there is no server or account to set up,
and it works offline. Data persists after the app is closed.

## Tech
Java, Android Studio, RecyclerView with custom adapters, Intents,
SharedPreferences, Material Components.

## Setup
1. Install Android Studio and clone this repository.
2. Open the project and let Gradle sync finish.
3. Connect a phone with USB debugging, or create an emulator (API 34 works well).
4. Press Run.

## Testing
The strict-matching rule is covered by 7 JUnit unit tests (RecipeMatcherTest). They
check that a recipe is only suggested when every ingredient is present in enough
quantity, and that plurals, unit differences and synonyms still match.
## Author
Kamogelo Bokamoso, 402311533, Richfield, Mobile App Development 700