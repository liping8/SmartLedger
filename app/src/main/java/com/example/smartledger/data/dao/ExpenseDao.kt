package com.example.smartledger.data.dao

import androidx.room.*
import com.example.smartledger.data.entity.Expense
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {
    @Query("SELECT * FROM expenses ORDER BY timestamp DESC")
    fun getAllExpenses(): Flow<List<Expense>>

    @Query("SELECT * FROM expenses WHERE timestamp BETWEEN :start AND :end ORDER BY timestamp DESC")
    fun getExpensesBetween(start: Long, end: Long): Flow<List<Expense>>

    @Query("SELECT SUM(amount) FROM expenses WHERE timestamp BETWEEN :start AND :end")
    fun getTotalBetween(start: Long, end: Long): Flow<Double?>

    @Query("SELECT categoryName, SUM(amount) as total FROM expenses WHERE timestamp BETWEEN :start AND :end GROUP BY categoryName")
    fun getCategoryTotalsBetween(start: Long, end: Long): Flow<List<CategoryTotal>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(expense: Expense): Long

    @Update
    suspend fun update(expense: Expense)

    @Delete
    suspend fun delete(expense: Expense)
}

data class CategoryTotal(
    val categoryName: String,
    val total: Double
)
