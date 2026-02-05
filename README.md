# Calculator App

This is an advanced calculator application for Android, developed in Java using Android Studio. It includes calculation history, animations, theme customization, and gesture sensitivity configuration for drag gestures (Top Sheet).

## Main Features
- Basic mathematical operations (+, -, ×, ÷, %, decimals)
- Calculation history with persistence using Room
- Smooth result animations
- Theme customization (light/dark mode and custom colors)
- Gesture sensitivity configuration (slop, smoothing, threshold)
- Modern interface with Material Design

## Project Structure
- `app/src/main/java/com/eduar2tc/calculator/`  
  - `ui/activities/` — Main activities (MainActivity, SettingsActivity)
  - `ui/controllers/` — UI controllers and logic
  - `ui/behavior/` — Custom behaviors (TopSheetBehavior)
  - `ui/dialogs/` — Custom dialogs
  - `ui/validators/` — Operation validators
  - `data/` — Room persistence classes (Calculation, CalculationDao, CalculationRepository)
  - `viewmodel/` — ViewModels for history
  - `utils/` — Various utilities
- `app/src/main/res/` — Resources (layouts, drawables, values, themes)

## Installation
1. Clone the repository
2. Open the project in Android Studio
3. Sync dependencies and build
4. Run on an Android device/emulator

## Usage
- Enter operations in the main field
- The result is shown automatically
- Press the equal (=) button to save to history
- Access history by sliding the Top Sheet or from the menu
- Customize gesture sensitivity and colors in Settings

## Customization
- Modify colors in the XML theme and values files
- Adjust gesture sensitivity in the settings screen

## Best Practices
- MVVM architecture to separate UI and logic
- Persistence with Room
- Themes and colors defined in XML for dynamic changes
- Decoupled animations in controllers

## Credits
Developed by Eduar2tc

---

For questions, suggestions, or bug reports, open an issue in the repository.
