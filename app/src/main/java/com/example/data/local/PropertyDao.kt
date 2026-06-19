package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PropertyDao {
    @Query("SELECT propertyId FROM saved_properties ORDER BY savedAt DESC")
    fun getSavedPropertyIds(): Flow<List<Int>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavedProperty(savedProperty: SavedPropertyEntity)

    @Query("DELETE FROM saved_properties WHERE propertyId = :propertyId")
    suspend fun removeSavedProperty(propertyId: Int)

    @Query("SELECT EXISTS(SELECT 1 FROM saved_properties WHERE propertyId = :propertyId)")
    suspend fun isPropertySaved(propertyId: Int): Boolean
}
