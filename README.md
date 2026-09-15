# Stark Future App

An Android telemetry dashboard built in Kotlin using Jetpack Compose and a clean, testable architecture. This project was designed to demonstrate scalable Android development patterns and best practices commonly used in production mobile apps.

## Overview

The app displays bike telemetry data in a structured dashboard, including:
- battery status and estimated range
- motor power and temperature
- ride configuration and session metrics
- diagnostic warnings and status feedback

The UI is built with Compose and Material 3, while the application logic follows a clean architecture to keep responsibilities separated and the codebase maintainable.

## Architecture and Best Practices

### MVVM (Model-View-ViewModel)
The presentation layer is built around MVVM, which separates user interface logic from the screen rendering.

- ViewModels own and expose UI state to the composable screen
- state is observed through Flow and StateFlow
- user interactions are handled through explicit ViewModel methods
- the UI remains predictable, easier to test, and simpler to evolve over time

### Jetpack Compose
The interface is implemented using Jetpack Compose.

- reusable composable components
- state-driven rendering with `collectAsState()`
- easier maintenance and faster iteration

### Hilt for Dependency Injection
Hilt is used to manage dependency injection across the app.

- repositories are injected into ViewModels
- manual object wiring is reduced
- dependencies are more centralized and easier to maintain
- the app is easier to test by injecting mock implementations

### Clean Architecture
The project follows a layered structure inspired by Clean Architecture.

- `data` layer: data sources, DTOs, mapping logic, repository implementations, mock scenarios
- `domain` layer: business models and repository contracts
- `presentation` layer: ViewModels and Compose screens

This separation aims to improve:
- maintainability
- decoupling between UI code and data sources
- easier testing of business behavior
- flexibility to replace or extend data providers later

### Repository Pattern and Data Handling
The repository layer acts as the boundary between the data source and the rest of the application.

- repository interfaces define a clear contract
- concrete implementations handle validation and error mapping
- DTOs are transformed into domain models through mapper classes
result states represent success, empty, and error scenarios

### State Handling and Unidirectional UI Flow
The app uses a Flow-based state model to manage UI behavior.

- loading, success, empty, and error states are well defined
- the screen responds to state changes
- the single source of truth remains in the ViewModel
- interactions such as retrying and changing a scenario are handled in a predictable way

### Coil for Image Loading
Coil is used to loading the bike image inside the Compose UI.

- native compatibility with Jetpack Compose
- efficient image loading and caching

### Unit Testing
The project includes unit tests covering both ViewModel and repository logic for regression prevention.

Examples covered:
- successful data loading
- empty response handling
- error propagation
- retry behavior
- scenario-based state changes

### Error Handling and Resilience
The app is designed to handle failures instead of crashing the UI.

- network and unexpected exceptions are converted into explicit result states
- empty data is treated as a valid business case rather than a runtime failure
- retry flows are supported to recover from transient issues
- failures are visible and understandable from the screen state

## Project Structure

```text
app/
├── data/
│   ├── mapper/
│   ├── mock/
│   ├── remote/
│   └── repository/
├── di/
├── domain/
│   ├── model/
│   └── repository/
├── presentation/
│   └── telemetry/
├── ui/
└── MainActivity.kt
```

## Tech Stack

- Kotlin
- Jetpack Compose
- Material 3
- MVVM
- Hilt
- Coroutines + Flow
- StateFlow
- Clean Architecture
- Repository Pattern
- Coil
- JUnit
