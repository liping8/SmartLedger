package com.example.smartledger.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.smartledger.data.dao.CategoryDao
import com.example.smartledger.data.dao.ExpenseDao
import com.example.smartledger.data.entity.Category
import com.example.smartledger.data.entity.Expense
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [Expense::class, Category::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun expenseDao(): ExpenseDao
    abstract fun categoryDao(): CategoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "smart_ledger_db"
                )
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            INSTANCE?.let { database ->
                                CoroutineScope(Dispatchers.IO).launch {
                                    val dao = database.categoryDao()
                                    val defaults = listOf(
                                        Category(name = "餐饮", icon = "🍜", isDefault = true),
                                        Category(name = "交通", icon = "🚗", isDefault = true),
                                        Category(name = "购物", icon = "🛒", isDefault = true),
                                        Category(name = "住房", icon = "🏠", isDefault = true),
                                        Category(name = "娱乐", icon = "🎮", isDefault = true),
                                        Category(name = "医疗", icon = "💊", isDefault = true),
                                        Category(name = "教育", icon = "📚", isDefault = true),
                                        Category(name = "通讯", icon = "📱", isDefault = true),
                                        Category(name = "其他", icon = "📦", isDefault = true),
                                    )
                                    defaults.forEach { dao.insert(it) }
                                }
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
