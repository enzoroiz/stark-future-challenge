# Stark Future App

A small Android telemetry dashboard built with Kotlin and Jetpack Compose. The app demonstrates a clean Android architecture with reactive state handling, dependency injection, and testable business logic.

## Key architecture and library choices

- MVVM: ViewModel owns UI state and keeps screen logic separate from rendering.
- Jetpack Compose: modern declarative UI with reusable components and state-driven rendering.
- Hilt: dependency injection keeps the project easier to test and extend.
- Clean Architecture: separation between `data`, `domain`, and `presentation` layers.
- Repository pattern: centralizes data access, mapping, and error handling.
- Coroutines + Flow + StateFlow: handles loading, success, empty, and error states.
- Coil: loads the bike image efficiently in Compose.
- Unit tests: verifies repository and ViewModel logic for success, empty, error, and retry scenarios.

## Usage

The app includes scenario toggles to switch between states:
They can be selected from the filter chips in the UI and trigger the ViewModel to reload telemetry accordingly.

- Success: renders valid telemetry data
- Error: simulates a failed telemetry request, allowing retry (always goes back to success on retry)
- Empty: displays the no-data state

## Trade-offs and things I would improve with more time

- More dedicated UI polish: add linear gauges for battery, motor health, and range; better card grouping for readability.
- Better Empty and Error states: stronger messaging, adding images / animations, and clearer recovery actions.
- UI testing: add Compose UI tests to validate state rendering and interactions.
