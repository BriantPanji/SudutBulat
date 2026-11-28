package com.sudutcorp.sudutbulat

import android.app.Application
import com.sudutcorp.sudutbulat.data.AppContainer
import com.sudutcorp.sudutbulat.data.DefaultAppContainer
import org.maplibre.android.MapLibre

/**
 * Custom application class to host the app-wide [AppContainer] for manual dependency injection.
 */
class SudutBulatApplication : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        MapLibre.getInstance(this)
        container = DefaultAppContainer()
    }
}
