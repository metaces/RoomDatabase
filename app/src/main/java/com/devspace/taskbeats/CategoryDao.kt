package com.devspace.taskbeats

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface CategoryDao {
    @Query("Select * from categoryentity")
    fun getAll(): List<CategoryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg categoryEntities: CategoryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(categoryEntities: CategoryEntity)

    @Delete
    fun delete(categoryEntities: CategoryEntity)
}