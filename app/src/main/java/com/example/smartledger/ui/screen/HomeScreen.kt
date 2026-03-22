package com.example.smartledger.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.smartledger.data.entity.Expense
import com.example.smartledger.ui.navigation.Routes
import com.example.smartledger.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController, viewModel: MainViewModel) {
    val expenses by viewModel.allExpenses.collectAsStateWithLifecycle()
    val monthTotal by viewModel.currentMonthTotal.collectAsStateWithLifecycle()
    val budget by viewModel.monthlyBudget.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("拍照记账") },
                actions = {
                    IconButton(onClick = { navController.navigate(Routes.STATS) }) {
                        Icon(Icons.Default.BarChart, "统计")
                    }
                    IconButton(onClick = { navController.navigate(Routes.CATEGORIES) }) {
                        Icon(Icons.Default.Category, "分类")
                    }
                    IconButton(onClick = { navController.navigate(Routes.BUDGET) }) {
                        Icon(Icons.Default.AccountBalanceWallet, "预算")
                    }
                }
            )
        },
        floatingActionButton = {
            Column(horizontalAlignment = Alignment.End) {
                // Camera FAB
                FloatingActionButton(
                    onClick = { navController.navigate(Routes.CAMERA) },
                    modifier = Modifier.padding(bottom = 12.dp),
                    containerColor = MaterialTheme.colorScheme.secondary
                ) {
                    Icon(Icons.Default.CameraAlt, "拍照记账")
                }
                // Manual add FAB
                FloatingActionButton(
                    onClick = { navController.navigate(Routes.ADD_EXPENSE) },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Default.Add, "手动记账")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Monthly summary card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("本月支出", color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
                    Text(
                        "¥%.2f".format(monthTotal),
                        color = Color.White,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (budget > 0) {
                        Spacer(modifier = Modifier.height(8.dp))
                        val progress = (monthTotal / budget).toFloat().coerceIn(0f, 1f)
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = if (monthTotal > budget) Color(0xFFFF5252) else Color.White,
                            trackColor = Color.White.copy(alpha = 0.3f),
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            if (monthTotal > budget) "已超支 ¥%.2f".format(monthTotal - budget)
                            else "预算剩余 ¥%.2f".format(budget - monthTotal),
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // Expense list
            if (expenses.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.ReceiptLong,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("暂无消费记录", color = Color.Gray)
                        Text("点击右下角按钮开始记账", color = Color.Gray, fontSize = 12.sp)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    items(expenses, key = { it.id }) { expense ->
                        ExpenseItem(
                            expense = expense,
                            onDelete = { viewModel.deleteExpense(expense) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ExpenseItem(expense: Expense, onDelete: () -> Unit) {
    val dateFormat = remember { SimpleDateFormat("MM/dd HH:mm", Locale.getDefault()) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("删除记录") },
            text = { Text("确定要删除这条消费记录吗？") },
            confirmButton = {
                TextButton(onClick = { onDelete(); showDeleteDialog = false }) {
                    Text("删除", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("取消") }
            }
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClickLabel = "长按删除") { },
        shape = RoundedCornerShape(12.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showDeleteDialog = true }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Category icon
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Text(expense.categoryIcon, fontSize = 22.sp)
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Info
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(expense.categoryName, fontWeight = FontWeight.Medium)
                    if (expense.isFromOcr) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)
                        ) {
                            Text(
                                "AI",
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }
                if (expense.note.isNotEmpty()) {
                    Text(expense.note, fontSize = 12.sp, color = Color.Gray)
                }
                Text(
                    dateFormat.format(Date(expense.timestamp)),
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }

            // Amount
            Text(
                "-¥%.2f".format(expense.amount),
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}
