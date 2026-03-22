package com.example.smartledger.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.smartledger.data.entity.Category
import com.example.smartledger.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseScreen(
    navController: NavController,
    viewModel: MainViewModel,
    initialAmount: String = "",
    initialCategory: String = "",
    initialNote: String = "",
    isFromOcr: Boolean = false
) {
    val categories by viewModel.allCategories.collectAsStateWithLifecycle()
    var amount by remember { mutableStateOf(initialAmount) }
    var note by remember { mutableStateOf(initialNote) }
    var selectedCategory by remember { mutableStateOf<Category?>(null) }

    // Auto-select category from OCR
    LaunchedEffect(initialCategory, categories) {
        if (initialCategory.isNotEmpty() && selectedCategory == null) {
            selectedCategory = categories.find { it.name == initialCategory }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isFromOcr) "确认记账" else "手动记账") },
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
            // Amount input
            OutlinedTextField(
                value = amount,
                onValueChange = { newValue ->
                    if (newValue.isEmpty() || newValue.matches(Regex("""^\d*\.?\d{0,2}$"""))) {
                        amount = newValue
                    }
                },
                label = { Text("金额") },
                prefix = { Text("¥ ") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Category selection
            Text("选择分类", style = MaterialTheme.typography.titleSmall)
            Spacer(modifier = Modifier.height(8.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                modifier = Modifier.heightIn(max = 240.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { category ->
                    val isSelected = selectedCategory?.id == category.id
                    Card(
                        modifier = Modifier
                            .clickable { selectedCategory = category },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected)
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                            else MaterialTheme.colorScheme.surface
                        ),
                        border = if (isSelected) CardDefaults.outlinedCardBorder() else null
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(category.icon, fontSize = 24.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                category.name,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Note input
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("备注（可选）") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 2,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.weight(1f))

            // Save button
            Button(
                onClick = {
                    val amountValue = amount.toDoubleOrNull()
                    if (amountValue != null && amountValue > 0 && selectedCategory != null) {
                        viewModel.addExpense(
                            amount = amountValue,
                            categoryName = selectedCategory!!.name,
                            categoryIcon = selectedCategory!!.icon,
                            note = note,
                            isFromOcr = isFromOcr
                        )
                        navController.popBackStack()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                enabled = amount.toDoubleOrNull() != null && selectedCategory != null,
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("保存", fontSize = 16.sp)
            }
        }
    }
}
