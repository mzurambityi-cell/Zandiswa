package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_properties")
data class SavedPropertyEntity(
    @PrimaryKey val propertyId: Int,
    val savedAt: Long = System.currentTimeMillis()
)
