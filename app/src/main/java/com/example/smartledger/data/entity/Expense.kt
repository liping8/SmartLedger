package com.example.smartledger.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expenses")
data class Expense(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amount: Double,
    val categoryName: String,
    val categoryIcon: String = "📦",
    val note: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val isFromOcr: Boolean = false
)
