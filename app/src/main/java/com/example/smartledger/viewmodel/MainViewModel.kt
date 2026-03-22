package com.example.smartledger.viewmodel

import android.app.Application
import android.graphics.Bitmap
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartledger.data.AppDatabase
import com.example.smartledger.data.dao.CategoryTotal
import com.example.smartledger.data.entity.Category
import com.example.smartledger.data.entity.Expense
import com.example.smartledger.ocr.OcrResult
import com.example.smartledger.ocr.ReceiptRecognizer
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*

val Application.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val expenseDao = db.expenseDao()
    private val categoryDao = db.categoryDao()
    private val dataStore = application.dataStore

    companion object {
        val BUDGET_KEY = doublePreferencesKey("monthly_budget")
    }

    // All expenses
    val allExpenses: StateFlow<List<Expense>> = expenseDao.getAllExpenses()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All categories
    val allCategories: StateFlow<List<Category>> = categoryDao.getAllCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Monthly budget
    val monthlyBudget: StateFlow<Double> = dataStore.data
        .map { prefs -> prefs[BUDGET_KEY] ?: 0.0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // Current month total
    val currentMonthTotal: StateFlow<Double> = run {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val start = cal.timeInMillis

        cal.add(Calendar.MONTH, 1)
        val end = cal.timeInMillis

        expenseDao.getTotalBetween(start, end)
            .map { it ?: 0.0 }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)
    }

    // Weekly stats for chart
    private val _weeklyStats = MutableStateFlow<List<CategoryTotal>>(emptyList())
    val weeklyStats: StateFlow<List<CategoryTotal>> = _weeklyStats

    // OCR state
    private val _ocrResult = MutableStateFlow<OcrResult?>(null)
    val ocrResult: StateFlow<OcrResult?> = _ocrResult

    private val _ocrLoading = MutableStateFlow(false)
    val ocrLoading: StateFlow<Boolean> = _ocrLoading

    init {
        loadWeeklyStats()
    }

    fun addExpense(amount: Double, categoryName: String, categoryIcon: String, note: String, isFromOcr: Boolean = false) {
        viewModelScope.launch {
            expenseDao.insert(
                Expense(
                    amount = amount,
                    categoryName = categoryName,
                    categoryIcon = categoryIcon,
                    note = note,
                    isFromOcr = isFromOcr
                )
            )
            loadWeeklyStats()
        }
    }

    fun updateExpense(expense: Expense) {
        viewModelScope.launch {
            expenseDao.update(expense)
            loadWeeklyStats()
        }
    }

    fun deleteExpense(expense: Expense) {
        viewModelScope.launch {
            expenseDao.delete(expense)
            loadWeeklyStats()
        }
    }

    fun addCategory(name: String, icon: String) {
        viewModelScope.launch {
            categoryDao.insert(Category(name = name, icon = icon))
        }
    }

    fun deleteCategory(category: Category) {
        viewModelScope.launch {
            categoryDao.delete(category)
        }
    }

    fun setBudget(amount: Double) {
        viewModelScope.launch {
            dataStore.edit { prefs ->
                prefs[BUDGET_KEY] = amount
            }
        }
    }

    fun recognizeReceipt(bitmap: Bitmap) {
        viewModelScope.launch {
            _ocrLoading.value = true
            try {
                val result = ReceiptRecognizer.recognize(bitmap)
                _ocrResult.value = result
            } catch (e: Exception) {
                _ocrResult.value = OcrResult(null, "其他", "识别失败: ${e.message}")
            } finally {
                _ocrLoading.value = false
            }
        }
    }

    fun clearOcrResult() {
        _ocrResult.value = null
    }

    private fun loadWeeklyStats() {
        viewModelScope.launch {
            val cal = Calendar.getInstance()
            cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            val weekStart = cal.timeInMillis

            cal.add(Calendar.WEEK_OF_YEAR, 1)
            val weekEnd = cal.timeInMillis

            expenseDao.getCategoryTotalsBetween(weekStart, weekEnd)
                .collect { _weeklyStats.value = it }
        }
    }

    fun getWeeklyExpenses(): Flow<List<Expense>> {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val weekStart = cal.timeInMillis

        cal.add(Calendar.WEEK_OF_YEAR, 1)
        val weekEnd = cal.timeInMillis

        return expenseDao.getExpensesBetween(weekStart, weekEnd)
    }
}
