# LearnBrowser

LearnBrowser is an Android web browser application with translation and vocabulary learning features. It allows users to browse the web, translate web pages, and build a personalized vocabulary list by translating and saving individual words.

## Features

- **Web Browsing**: Standard web browsing capabilities with URL navigation, back/forward, and refresh.
- **Page Translation**: Translate entire web pages from their original language to a user-selected language.
- **Word Translation**: Long-press on any word to translate it and view the translation in a popup.
- **Vocabulary Lists**: Save translated words to vocabulary lists organized by source language.
- **Offline Support**: Download language packs for offline translation.
- **Export Functionality**: Export vocabulary lists as text files for use in other applications.

## Technical Details

### Architecture

The application follows the MVVM (Model-View-ViewModel) architecture pattern and uses the following components:

- **UI Layer**: Activities, Fragments, and Adapters for the user interface.
- **ViewModel Layer**: ViewModels for managing UI-related data and business logic.
- **Repository Layer**: Repositories for providing a clean API to the data sources.
- **Data Layer**: Room database for local storage and Google Translate API for translations.

### Libraries and Technologies.

- **Android Jetpack**: ViewModel, LiveData, Room, DataStore
- **Kotlin Coroutines**: For asynchronous programming
- **Dagger Hilt**: For dependency injection
- **Google Cloud Translation API**: For translation services
- **Material Design Components**: For UI elements

## Requirements

- Android 13 (API level 33) or higher
- Internet connection for web browsing and translation (unless language packs are downloaded)

## Setup

1. Clone the repository
2. Open the project in Android Studio
3. Add your Google Cloud Translation API key in the `app/build.gradle` file
4. Build and run the application

## Usage

### Browsing

- Enter a URL in the address bar and tap "Go" or press Enter
- Use the back, forward, and refresh buttons for navigation

### Translation

- Tap the "Translate Page" button to translate the entire page
- Long-press on any word to see its translation
- In the translation popup, tap "Add to Vocabulary" to save the word

### Vocabulary Management

- Tap the vocabulary button to view your saved words
- Filter vocabulary by source language
- Delete individual words or clear the entire list
- Export your vocabulary list as a text file

### Settings

- Set your preferred target language for translations
- Enable/disable automatic page translation
- Download language packs for offline use

## License

This project is licensed under the MIT License - see the LICENSE file for details.
