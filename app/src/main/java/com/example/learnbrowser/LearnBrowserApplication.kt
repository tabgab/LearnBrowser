package com.example.learnbrowser

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Main application class for LearnBrowser.
 * Annotated with @HiltAndroidApp to enable dependency injection.
 */
@HiltAndroidApp
class LearnBrowserApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        // Initialize any application-wide components here
    }
}
