package com.example.meteomind

import androidx.annotation.WorkerThread
import kotlinx.coroutines.flow.Flow

class LocationRepository(private val locationDao: LocationDao) {

    val locations: Flow<List<Location>> = locationDao.getLocationsByDate()

    @WorkerThread
    suspend fun insertLocation(location: Location) {
        locationDao.insert(location)
    }

    @WorkerThread
    suspend fun updateLocation(location: Location) {
        locationDao.update(location)
    }

    @WorkerThread
    suspend fun deleteLocation(location: Location) {
        locationDao.delete(location)
    }
}