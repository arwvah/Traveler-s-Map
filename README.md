# Traveler's Map (MVP)

Premium **Uzbekistan-only** travel map for Android.

## Stack

- Kotlin · Jetpack Compose · Material 3
- Google Maps SDK (maps-compose)
- Room · Hilt · Coroutines/Flow · Navigation Compose
- Clean architecture (scalable country datasets)

## Open in Android Studio

1. **File → Open** → this folder
2. Wait for Gradle sync
3. Add a Maps key in `local.properties`:

```properties
MAPS_API_KEY=YOUR_KEY_HERE
```

Or set `MAPS_API_KEY` in `gradle.properties`.

4. Run on an emulator/device with Google Play services

## Features (MVP)

- Fullscreen map centered on Uzbekistan
- Large gold tourist pins (Compose custom) + intelligent clustering (zoom out → cluster bubbles, tap to expand)
- Search (city / landmark / category), place preview bottom sheet + full place page
- Favorites stored in Room (offline, no login)
- Route modes (walk / drive / cycle + transit placeholder)
- Mock AI travel planner (swap-ready `AiTravelPlanner` interface)
- Budget planner
- Settings: dark mode default, EN / UZ / RU preference, about, privacy
- Offline seed catalog of famous Uzbekistan sites

## Architecture

```
app/
  core/          theme, design system
  data/          Room, repositories, seed data
  domain/        models, use cases, AI interface
  ui/features/   map, search, place, favorites, ai, budget, settings, routes
  navigation/
  di/
```

Adding another country later = new dataset seed + country config, not a rewrite.

## Note

No login/accounts — opens directly to the map. All MVP data is local.
