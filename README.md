# Steven Workout Timer

A specialized Android workout timer app built with Jetpack Compose, designed for athletes with support for EMOM (Every Minute On the Minute) and Climbing/Hangboard training modes.

## Features

### Timer Modes

**EMOM Mode (Weightlifting/CrossFit)**
- Start exercises at the beginning of each minute
- Configurable workout duration (2-120 minutes)
- Audio countdown before each minute ends

**Climbing Mode (Hangboard Training)**
- Alternating hold and rest phases
- Customizable hold duration (1-60 seconds)
- Customizable rest duration (1-60 seconds)
- Configurable repetition count (1-50 reps)

### Core Features

- **Background Execution** - Timer continues running when app is minimized or screen is off
- **Audio Notifications** - Choose between beep tones or voice countdown
- **Initial Countdown** - Optional countdown before workout begins (0-30 seconds)
- **Fullscreen Mode** - Large display optimized for gym viewing
- **Persistent Notifications** - Real-time progress in notification bar with quick controls
- **Save & Manage Timers** - Create, edit, and delete custom workout presets
- **Multiple Themes** - System, Light, Dark, and Glassmorphic themes

## Screenshots

*Coming soon*

## Tech Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose with Material3
- **Architecture**: MVVM with Repository pattern
- **Local Storage**: Room Database
- **Navigation**: Compose Navigation
- **Background Processing**: Foreground Service with Wake Lock
- **Audio**: ToneGenerator + Android TTS

## Requirements

- Android 8.0 (API 26) or higher
- Android Studio Hedgehog or newer
- JDK 17

## Building the Project

```bash
# Clone the repository
git clone https://github.com/yourusername/StevenWorkoutTimer.git
cd StevenWorkoutTimer

# Build debug APK
./gradlew assembleDebug

# Install on connected device
./gradlew installDebug

# Build release APK
./gradlew assembleRelease
```

## Project Structure

```
app/src/main/java/com/steven/workouttimer/
├── MainActivity.kt              # App entry point
├── WorkoutTimerApp.kt           # Application class & DI container
├── service/
│   └── TimerService.kt          # Foreground service for background timer
├── data/
│   ├── db/                      # Room database (Entity, DAO, Database)
│   ├── repository/              # Data access abstraction
│   └── preferences/             # Theme preferences
├── ui/
│   ├── screens/
│   │   ├── home/                # Timer list & running timer banner
│   │   ├── create/              # Timer creation/editing form
│   │   ├── timer/               # Active timer display
│   │   └── fullscreen/          # Fullscreen timer view
│   ├── components/              # Reusable UI components
│   ├── theme/                   # Material3 theming
│   └── navigation/              # Navigation graph
├── audio/
│   └── AudioNotificationManager.kt  # Audio playback
└── util/
    └── TimeUtils.kt             # Time formatting utilities
```

## Architecture

The app follows MVVM architecture with clean separation of concerns:

- **UI Layer**: Jetpack Compose screens with ViewModels managing UI state via StateFlow
- **Data Layer**: Room database with Repository pattern for data access
- **Service Layer**: Foreground service maintains timer state and handles background execution
- **DI**: Manual dependency injection via AppContainer

## Permissions

The app requires the following permissions:

| Permission | Purpose |
|------------|---------|
| `FOREGROUND_SERVICE` | Run timer in background |
| `FOREGROUND_SERVICE_MEDIA_PLAYBACK` | Audio countdown notifications |
| `POST_NOTIFICATIONS` | Display timer progress notification |
| `VIBRATE` | Haptic feedback |
| `WAKE_LOCK` | Prevent device sleep during workout |

## License

MIT License - See [LICENSE](LICENSE) for details.
