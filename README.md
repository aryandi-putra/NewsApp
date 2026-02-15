# NewsApp

A modern Android news application built with Jetpack Compose, following Clean Architecture principles and MVVM pattern. The app fetches top headlines from NewsAPI and supports offline reading with local caching.

## Features

- **Top Headlines**: Browse latest news with pull-to-refresh and endless scroll pagination
- **Search**: Search for news articles with debounced query input
- **Bookmarks**: Save articles for offline reading
- **Offline Support**: Full offline functionality with cached data and connectivity awareness
- **Modern UI**: Built with Jetpack Compose following Material Design 3

## Tech Stack

### Architecture
- **Clean Architecture**: Separation of concerns with Domain, Data, and Presentation layers
- **MVVM**: Model-View-ViewModel pattern for UI layer
- **Repository Pattern**: Abstracted data sources (API + Local Database)
- **Dependency Injection**: Hilt for DI

### Core Libraries
- **Jetpack Compose**: Modern declarative UI toolkit
- **Room**: Local database for offline caching
- **Retrofit + OkHttp**: HTTP client for API communication
- **Paging 3**: Efficient list pagination with RemoteMediator
- **Coroutines + Flow**: Asynchronous programming
- **Navigation Component**: Type-safe navigation with Compose
- **Coil**: Image loading library

### Testing
- **JUnit 4**: Unit testing framework
- **Turbine**: Flow testing library
- **Coroutines Test**: Testing coroutines with TestDispatcher
- **Fake implementations**: For repository and connectivity testing

## Project Structure

```
app/src/main/java/com/hunter/newsapp/
├── data/
│   ├── connectivity/       # Network state monitoring
│   ├── local/             # Room database (Entities, DAOs)
│   ├── mapper/            # Data transformation (DTO <-> Entity <-> Domain)
│   ├── paging/            # PagingSource and RemoteMediator
│   ├── remote/            # Retrofit API service
│   └── repository/        # Repository implementations
├── di/                    # Hilt dependency injection modules
├── domain/
│   ├── model/             # Domain models (pure Kotlin)
│   └── repository/        # Repository interfaces
├── presentation/
│   ├── bookmark/          # Bookmark screen & ViewModel
│   ├── common/            # Shared UI components (NewsItem, etc.)
│   ├── detail/            # Article detail screen
│   ├── navigation/        # Navigation routes
│   ├── search/            # Search screen & ViewModel
│   ├── splash/            # Splash screen
│   └── top_headlines/     # Main screen & ViewModel
└── ui/theme/              # Material Design 3 theme
```

## Requirements

- **Android Studio**: Koala (2024.1.1) or later
- **JDK**: 17 or later
- **Min SDK**: 24 (Android 7.0)
- **Target SDK**: 35 (Android 15)
- **API Key**: NewsAPI key (free tier available)

## Setup

### 1. Get a NewsAPI Key

1. Go to [newsapi.org](https://newsapi.org) and create a free account
2. Copy your API key from the dashboard

### 2. Configure the API Key

Add your API key to `local.properties` file (create if doesn't exist):

```properties
NEWS_API_KEY="your_api_key_here"
```

**Note**: This file is git-ignored by default and won't be committed.

### 3. Build the Project

Open the project in Android Studio and let Gradle sync complete.

## Running the App

### From Android Studio
1. Connect a device or start an emulator
2. Click the **Run** button (▶️) or press `Shift + F10`

### From Command Line

```bash
# Install debug build on connected device
./gradlew installDebug

# Run tests
./gradlew testDebug

# Build release APK (requires signing config)
./gradlew assembleRelease
```

## Architecture Overview

### Data Flow
```
UI Layer (Compose)
    ↓
ViewModel (StateFlow/SharedFlow)
    ↓
Domain Layer (Repository Interface)
    ↓
Data Layer (RepositoryImpl)
    ↓
Remote (Retrofit) ←→ Local (Room)
```

### Offline-First Strategy
The app uses `RemoteMediator` for seamless offline experience:
- **Read**: Shows cached data immediately, refreshes in background
- **Write**: Updates local database first, syncs when online
- **Error Handling**: Network failures show cached data with Snackbar notification

## Key Features Explained

### Pull-to-Refresh
- Swipe down to refresh headlines
- Shows loading spinner, preserves cached data on error
- Displays Snackbar with retry option on network failure

### Endless Scroll
- Scroll to bottom to load more articles
- Automatic pagination with loading indicator
- Graceful error handling with retry button

### Connectivity Awareness
- Real-time network state monitoring
- Snackbar notifications when going offline/online
- Automatic retry when connectivity returns

## Testing

### Running Unit Tests
```bash
./gradlew testDebug
```

### Test Coverage
- **ViewModel Tests**: State management, user interactions, error handling
- **Repository Tests**: Data operations, offline scenarios
- **Fake Implementations**: Test doubles for external dependencies

## License

This project is for educational purposes. News data provided by [NewsAPI](https://newsapi.org).

## Troubleshooting

### Build Issues
- **Clear Gradle cache**: `./gradlew clean`
- **Invalidate caches**: File → Invalidate Caches in Android Studio
- **Check API key**: Ensure `local.properties` has valid `NEWS_API_KEY`

### Runtime Issues
- **Clear app data**: Settings → Apps → NewsApp → Storage → Clear Data
- **Check internet permission**: Verify `AndroidManifest.xml` has `INTERNET` and `ACCESS_NETWORK_STATE` permissions

## Contributing

This is a learning project. Feel free to fork and experiment!
