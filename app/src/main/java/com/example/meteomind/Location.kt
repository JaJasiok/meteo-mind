package com.example.meteomind

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Location(
    @ColumnInfo(name = "name")
    var locationName: String,
    @ColumnInfo(name = "placeId")
    var locationPlaceId: String,
    @ColumnInfo(name = "latitude")
    var locationLat: Double,
    @ColumnInfo(name = "longitude")
    var locationLng: Double,
    @ColumnInfo(name = "dt")
    var locationDt: Long,
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
)