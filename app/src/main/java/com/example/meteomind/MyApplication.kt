package com.example.meteomind

import android.app.Application
import com.google.android.material.color.DynamicColors

class MyApplication : Application() {

    private val database by lazy { LocationDatabase.getDatabase(this) }
    val repository by lazy { LocationRepository(database.locationDao()) }
    override fun onCreate() {
        super.onCreate()
//        DynamicColors.applyToActivitiesIfAvailable(this)
    }
}