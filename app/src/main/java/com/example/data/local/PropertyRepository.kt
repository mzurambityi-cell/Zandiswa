package com.example.data.local

import kotlinx.coroutines.flow.Flow

class PropertyRepository(private val propertyDao: PropertyDao) {
    val savedPropertyIds: Flow<List<Int>> = propertyDao.getSavedPropertyIds()

    suspend fun saveProperty(propertyId: Int) {
        propertyDao.insertSavedProperty(SavedPropertyEntity(propertyId))
    }

    suspend fun removeProperty(propertyId: Int) {
        propertyDao.removeSavedProperty(propertyId)
    }

    suspend fun isSaved(propertyId: Int): Boolean {
        return propertyDao.isPropertySaved(propertyId)
    }
}
