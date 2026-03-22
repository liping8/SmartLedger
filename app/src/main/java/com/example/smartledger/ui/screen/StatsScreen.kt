package com.example.smartledger.ui.screen

import android.graphics.Color as AndroidColor
import android.view.ViewGroup
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.smartledger.viewmodel.MainViewModel
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(navController: NavController, viewModel: MainViewModel) {
    val weeklyStats by viewModel.weeklyStats.collectAsStateWithLifecycle()
    val totalAmount = weeklyStats.sumOf { it.total }

    val chartColors = listOf(
        AndroidColor.parseColor("#2196F3"),
        AndroidColor.parseColor("#4CAF50"),
        AndroidColor.parseColor("#FF9800"),
        AndroidColor.parseColor("#E91E63"),
        AndroidColor.parseColor("#9C27B0"),
        AndroidColor.parseColor("#00BCD4"),
        AndroidColor.parseColor("#FF5722"),
        AndroidColor.parseColor("#795548"),
        AndroidColor.parseColor("#607D8B"),
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("本周统计") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "返回")
                    }
                }
            )
        }
    ) { padding ->
        if (weeklyStats.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("本周暂无消费记录", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
            ) {
                // Total card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text("本周总支出", color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
                            Text(
                                "¥%.2f".format(totalAmount),
                                color = Color.White,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Pie chart
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        AndroidView(
                            factory = { context ->
                                PieChart(context).apply {
                                    layoutParams = ViewGroup.LayoutParams(
                                        ViewGroup.LayoutParams.MATCH_PARENT,
                                        ViewGroup.LayoutParams.MATCH_PARENT
                                    )
                                    setUsePercentValues(true)
                                    description.isEnabled = false
                                    isDrawHoleEnabled = true
                                    setHoleColor(AndroidColor.TRANSPARENT)
                                    holeRadius = 50f
                                    setDrawEntryLabels(true)
                                    setEntryLabelColor(AndroidColor.BLACK)
                                    setEntryLabelTextSize(11f)
                                    legend.isEnabled = false
                                    setExtraOffsets(20f, 10f, 20f, 10f)
                                }
                            },
                            update = { chart ->
                                val entries = weeklyStats.map { stat ->
                                    PieEntry(stat.total.toFloat(), stat.categoryName)
                                }
                                val dataSet = PieDataSet(entries, "").apply {
                                    colors = chartColors.take(entries.size)
                                    valueTextSize = 12f
                                    valueTextColor = AndroidColor.WHITE
                                    valueFormatter = PercentFormatter(chart)
                                    sliceSpace = 2f
                                }
                                chart.data = PieData(dataSet)
                                chart.invalidate()
                            },
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Category breakdown list
                item {
                    Text("分类明细", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                }

                items(weeklyStats.sortedByDescending { it.total }) { stat ->
                    val percentage = if (totalAmount > 0) (stat.total / totalAmount * 100) else 0.0
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(stat.categoryName, modifier = Modifier.weight(1f))
                            Text(
                                "%.1f%%".format(percentage),
                                color = Color.Gray,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(end = 12.dp)
                            )
                            Text(
                                "¥%.2f".format(stat.total),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
