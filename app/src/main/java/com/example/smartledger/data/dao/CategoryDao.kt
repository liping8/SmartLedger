package com.example.smartledger.data.dao

import androidx.room.*
import com.example.smartledger.data.entity.Category
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories ORDER BY isDefault DESC, name ASC")
    fun getAllCategories(): Flow<List<Category>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(category: Category): Long

    @Delete
    suspend fun delete(category: Category)

    @Update
    suspend fun update(category: Category)

    @Query("SELECT COUNT(*) FROM categories")
    suspend fun count(): Int
}
