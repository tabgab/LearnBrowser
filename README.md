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

### Libraries and Technologies

- **Android Jetpack**: ViewModel, LiveData, Room, DataStore
- **Kotlin Coroutines**: For asynchronous programming
- **Dagger Hilt**: For dependency injection
- **Multiple Translation APIs**: Support for various translation services
- **Material Design Components**: For UI elements

## Requirements

- Android 13 (API level 33) or higher
- Internet connection for web browsing and translation (unless language packs are downloaded)

## Setup

1. Clone the repository
2. Open the project in Android Studio
3. Set up your Google Cloud Translation API key:
   - Copy `apikey.properties.template` to `apikey.properties`
   - Replace `YOUR_API_KEY_HERE` with your actual Google Cloud Translation API key
4. Build and run the application

### Supported Translation Services

LearnBrowser supports multiple translation services that you can configure in the settings:

1. **Google Cloud Translation API** (Default)
   - Professional-grade translation service with high accuracy
   - Requires an API key (paid service with free tier)
   - Supports 100+ languages

2. **LibreTranslate**
   - Open-source machine translation API
   - Can be self-hosted or used via public API
   - Free to use if self-hosted, may require API key for public instances
   - Supports custom endpoints for self-hosted instances

3. **DeepL**
   - High-quality translation service with natural-sounding results
   - Requires an API key (free tier available with 500,000 characters/month)
   - Supports 29 languages with high accuracy

4. **Lingva Translate**
   - Alternative front-end for Google Translate
   - No API key required
   - Free to use
   - Supports all languages available in Google Translate

5. **Argos Translate** (Offline)
   - Open-source offline translation library
   - No API key required
   - Requires downloading language models
   - Limited language support compared to online services

6. **Microsoft Translator**
   - Microsoft's translation service
   - Requires an API key (free tier with 2 million characters/month)
   - Supports 100+ languages

7. **Yandex.Translate**
   - Yandex's translation service
   - Requires an API key (free tier with 1 million characters/month)
   - Supports 90+ languages

### Setting Up Translation Services

#### Google Cloud Translation API

1. **Create a Google Cloud Project**:
   - Go to the [Google Cloud Console](https://console.cloud.google.com/)
   - Click on the project dropdown at the top of the page and select "New Project"
   - Enter a name for your project and click "Create"

2. **Enable the Cloud Translation API**:
   - In the Google Cloud Console, go to "APIs & Services" > "Library"
   - Search for "Cloud Translation API"
   - Click on "Cloud Translation API" and then click "Enable"

3. **Set Up Billing**:
   - The Cloud Translation API requires billing to be enabled
   - In the Google Cloud Console, go to "Billing"
   - Link a billing account to your project
   - Note: Google provides a free tier with monthly credits

4. **Create an API Key**:
   - In the Google Cloud Console, go to "APIs & Services" > "Credentials"
   - Click "Create Credentials" > "API Key"
   - Copy the generated API key

5. **Configure API Key Restrictions (Recommended)**:
   - In the Credentials page, find your API key and click "Edit"
   - Under "API restrictions", select "Restrict key"
   - Select "Cloud Translation API" from the dropdown
   - Under "Application restrictions", you can choose to restrict the key to your Android app
   - Click "Save"

6. **Add the API Key to the Project**:
   - Copy `apikey.properties.template` to `apikey.properties`
   - Replace `YOUR_API_KEY_HERE` with your actual API key
   - Note: `apikey.properties` is in `.gitignore` to prevent accidentally committing your API key

#### LibreTranslate

1. **Use Public API**:
   - No setup required for basic usage with public API
   - For higher rate limits, you may need to get an API key from a public instance
   - Visit [LibreTranslate.com](https://libretranslate.com/) for more information

2. **Self-Host (Optional)**:
   - Clone the [LibreTranslate repository](https://github.com/LibreTranslate/LibreTranslate)
   - Follow the installation instructions
   - Configure your custom endpoint in the app settings

#### DeepL

1. **Sign Up for DeepL API**:
   - Go to [DeepL API](https://www.deepl.com/pro-api)
   - Sign up for a free account
   - Verify your email address

2. **Get API Key**:
   - Log in to your DeepL account
   - Navigate to the Account section
   - Copy your Authentication Key

#### Microsoft Translator

1. **Create an Azure Account**:
   - Go to the [Azure Portal](https://portal.azure.com/)
   - Sign up for a free account

2. **Create a Translator Resource**:
   - In the Azure Portal, click "Create a resource"
   - Search for "Translator"
   - Select "Translator" and click "Create"
   - Fill in the required information and create the resource

3. **Get API Key**:
   - Once the resource is created, go to the resource page
   - Navigate to "Keys and Endpoint" under Resource Management
   - Copy one of the keys

#### Yandex.Translate

1. **Create a Yandex Cloud Account**:
   - Go to [Yandex Cloud](https://cloud.yandex.com/)
   - Sign up for an account

2. **Create a Folder and Service Account**:
   - Create a new folder in your Yandex Cloud console
   - Create a service account with the "translator" role

3. **Get API Key**:
   - In the Yandex Cloud console, go to "Service Accounts"
   - Create an API key for your service account
   - Copy the API key

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

- Choose your preferred translation service from multiple options
- Configure API keys for translation services
- Set your preferred target language for translations
- Enable/disable automatic page translation
- Download language packs for offline use

## License

This project is licensed under the MIT License - see the LICENSE file for details.
