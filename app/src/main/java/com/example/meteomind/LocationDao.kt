package com.example.meteomind

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface LocationDao {

    @Query("SELECT * from location WHERE id = :id")
    fun getItem(id: Int): Location

    @Query("SELECT * FROM location ORDER BY dt DESC LIMIT 5")
    fun getLocationsByDate(): Flow<List<Location>>

    @Query("SELECT * FROM Location WHERE name = :name")
    fun getLocationByName(name: String): Location

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(location: Location)

    @Update
    suspend fun update(location: Location)

    @Delete
    suspend fun delete(location: Location)

    @Query("DELETE FROM location")
    suspend fun deleteAll()

    @Query("DELETE FROM location WHERE name = :name AND id = :id")
    suspend fun deleteByData(name: String, id: String)
}