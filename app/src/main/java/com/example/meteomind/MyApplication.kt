package com.example.meteomind

import android.app.Application

class MyApplication : Application() {

    private val database by lazy { LocationDatabase.getDatabase(this) }
    val repository by lazy { LocationRepository(database.locationDao()) }
}