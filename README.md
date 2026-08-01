# Traveler's Map

Premium **Uzbekistan** travel map for Android (Kotlin · Jetpack Compose · Material 3 · MVVM).

## Stack

- Google Maps SDK (maps-compose) + marker clustering
- Room · Hilt · Coroutines/Flow · Navigation Compose · Coil · OkHttp
- Open-Meteo weather · Google Directions (optional) · Groq Llama AI

## Open in Android Studio

1. **File → Open** → this folder
2. Wait for Gradle sync
3. Add keys in `local.properties`:

```properties
MAPS_API_KEY=YOUR_GOOGLE_MAPS_KEY
GROQ_API_KEY=YOUR_GROQ_KEY
```

See `local.properties.example`.

4. Run on an emulator/device with Google Play services

**Note:** Catalog lives in `app/src/main/assets/uzbekistan_attractions.json` (v3+, 650+ places). Room auto-reseeds when the asset `version` is newer than the last import, or when place count is under 200. After pulling catalog fixes, just relaunch the app (clear data only if pins look stale).

## Features

- Fullscreen map with gold tourist pins + clustering
- Live GPS (permission), blue location dot, **My Location** FAB
- Search by name / city / region / category across 650+ attractions
- Place pages: hero image carousel, gallery, history, meta, favorites
- **Navigate**: live route from GPS, distance/time/mode, map polyline, **Start Navigation** (Google Maps)
- **Weather** card per place (Open-Meteo, short Room cache)
- **AI planner** via Groq `llama-3.3-70b-versatile` (chat history + streaming; falls back if no key)
- Favorites, budget planner, settings (dark mode, EN/UZ/RU preference)
- Attractions stored in `app/src/main/assets/uzbekistan_attractions.json` (not hardcoded)

## Architecture

```
app/src/main/java/com/travelersmap/
  data/       Room, remote APIs, seed loader, repositories, Groq AI
  domain/     models, repository interfaces, AiTravelPlanner
  ui/         map, place, route, AI, favorites, budget, settings
  di/         Hilt modules
  navigation/
```

## Regenerating the catalog

```bash
python scripts/generate_attractions.py
```

Writes `app/src/main/assets/uzbekistan_attractions.json`.
