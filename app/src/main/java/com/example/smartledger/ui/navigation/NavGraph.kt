package com.example.smartledger.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.smartledger.ui.screen.*
import com.example.smartledger.viewmodel.MainViewModel

object Routes {
    const val HOME = "home"
    const val ADD_EXPENSE = "add_expense"
    const val CAMERA = "camera"
    const val STATS = "stats"
    const val CATEGORIES = "categories"
    const val BUDGET = "budget"
    const val EDIT_EXPENSE = "edit_expense/{expenseId}"
}

@Composable
fun NavGraph(navController: NavHostController, viewModel: MainViewModel = viewModel()) {
    NavHost(navController = navController, startDestination = Routes.HOME) {
        composable(Routes.HOME) {
            HomeScreen(navController = navController, viewModel = viewModel)
        }
        composable(Routes.ADD_EXPENSE) {
            AddExpenseScreen(navController = navController, viewModel = viewModel)
        }
        composable(Routes.CAMERA) {
            CameraScreen(navController = navController, viewModel = viewModel)
        }
        composable(Routes.STATS) {
            StatsScreen(navController = navController, viewModel = viewModel)
        }
        composable(Routes.CATEGORIES) {
            CategoryScreen(navController = navController, viewModel = viewModel)
        }
        composable(Routes.BUDGET) {
            BudgetScreen(navController = navController, viewModel = viewModel)
        }
    }
}
