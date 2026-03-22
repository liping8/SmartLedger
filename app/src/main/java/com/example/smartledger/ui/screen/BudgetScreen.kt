package com.example.smartledger.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.smartledger.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetScreen(navController: NavController, viewModel: MainViewModel) {
    val currentBudget by viewModel.monthlyBudget.collectAsStateWithLifecycle()
    val monthTotal by viewModel.currentMonthTotal.collectAsStateWithLifecycle()
    var budgetInput by remember { mutableStateOf("") }
    var saved by remember { mutableStateOf(false) }

    LaunchedEffect(currentBudget) {
        if (currentBudget > 0) {
            budgetInput = currentBudget.toLong().toString()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("预算设置") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "返回")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // Current status card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (currentBudget > 0 && monthTotal > currentBudget)
                        MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
                    else MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                )
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("本月消费", fontSize = 14.sp, color = Color.Gray)
                    Text(
                        "¥%.2f".format(monthTotal),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (currentBudget > 0) {
                        Spacer(modifier = Modifier.height(8.dp))
                        val remaining = currentBudget - monthTotal
                        Text(
                            if (remaining >= 0) "预算剩余: ¥%.2f".format(remaining)
                            else "已超支: ¥%.2f".format(-remaining),
                            fontSize = 14.sp,
                            color = if (remaining >= 0) MaterialTheme.colorScheme.secondary
                            else MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        @Suppress("DEPRECATION")
                        LinearProgressIndicator(
                            progress = (monthTotal / currentBudget).toFloat().coerceIn(0f, 1f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp),
                            color = if (remaining < 0) MaterialTheme.colorScheme.error
                            else MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Budget input
            Text("设置月度预算", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = budgetInput,
                onValueChange = { newValue ->
                    if (newValue.isEmpty() || newValue.matches(Regex("""^\d*\.?\d{0,2}$"""))) {
                        budgetInput = newValue
                        saved = false
                    }
                },
                label = { Text("月度预算金额") },
                prefix = { Text("¥ ") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Quick amount buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("1000", "2000", "3000", "5000").forEach { amount ->
                    AssistChip(
                        onClick = { budgetInput = amount; saved = false },
                        label = { Text("¥$amount") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    val value = budgetInput.toDoubleOrNull()
                    if (value != null && value > 0) {
                        viewModel.setBudget(value)
                        saved = true
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                enabled = budgetInput.toDoubleOrNull() != null && !saved,
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Check, null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (saved) "已保存" else "保存预算", fontSize = 16.sp)
            }

            if (saved) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "预算已设置，超支时首页会提醒",
                    color = MaterialTheme.colorScheme.secondary,
                    fontSize = 13.sp,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}
