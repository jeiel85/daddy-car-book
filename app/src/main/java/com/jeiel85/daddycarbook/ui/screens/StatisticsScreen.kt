package com.jeiel85.daddycarbook.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.jeiel85.daddycarbook.ui.component.*
import com.jeiel85.daddycarbook.ui.theme.GoldYellow
import com.jeiel85.daddycarbook.ui.theme.OrangeAccent
import com.jeiel85.daddycarbook.ui.theme.SlateBlue
import com.jeiel85.daddycarbook.ui.viewmodel.VehicleViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun StatisticsScreen(
    navController: NavController,
    viewModel: VehicleViewModel,
    modifier: Modifier = Modifier
) {
    val selectedVehicle by viewModel.selectedVehicle.collectAsState()
    val maintenanceRecords by viewModel.selectedVehicleMaintenanceRecords.collectAsState()
    val fuelRecords by viewModel.selectedVehicleFuelRecords.collectAsState()

    // Aggregate and filter past 6 months
    val statsList = remember(maintenanceRecords, fuelRecords) {
        val sdf = SimpleDateFormat("yyyy-MM", Locale.getDefault())
        val cal = Calendar.getInstance()
        val list = mutableListOf<MonthStat>()

        // Generate past 6 months
        for (i in 0 until 6) {
            val monthStr = sdf.format(cal.time)
            
            val mCost = maintenanceRecords.filter { it.date.startsWith(monthStr) }.sumOf { it.cost }
            val fCost = fuelRecords.filter { it.date.startsWith(monthStr) }.sumOf { it.amount }
            
            list.add(MonthStat(monthStr, mCost, fCost))
            cal.add(Calendar.MONTH, -1)
        }
        list.reverse()
        list
    }

    // Maximum cost among months for relative scaling on bar graph
    val maxCost = remember(statsList) {
        val max = statsList.maxOfOrNull { it.mCost + it.fCost } ?: 0L
        if (max == 0L) 100000L else max
    }

    // Category aggregations for all time
    val categoryStats = remember(maintenanceRecords) {
        val groups = maintenanceRecords.groupBy { it.type }
        groups.map { (type, records) ->
            CategoryStat(type, records.sumOf { it.cost })
        }.sortedByDescending { it.totalCost }
    }

    val totalMAllTime = remember(maintenanceRecords) { maintenanceRecords.sumOf { it.cost } }
    val totalFAllTime = remember(fuelRecords) { fuelRecords.sumOf { it.amount } }

    Scaffold(
        topBar = {
            DadsHeader(
                title = "차량 지출 소비 분석 📊",
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "이전",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        if (selectedVehicle == null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Default.BarChart, "통계", modifier = Modifier.size(72.dp), tint = Color.Gray)
                Spacer(modifier = Modifier.height(16.dp))
                Text("차량 등록 후 확인 가능가능합니다.", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
        } else {
            val vehicle = selectedVehicle!!
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "${vehicle.name} 누적 유지 비용",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Cumulative cost totals card
                item {
                    DadsCard {
                        Text("총 누적 소비 금액", fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            formatCost(totalMAllTime + totalFAllTime),
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black,
                            color = OrangeAccent
                        )

                        Spacer(modifier = Modifier.height(14.dp))
                        Divider(color = MaterialTheme.colorScheme.surfaceVariant)
                        Spacer(modifier = Modifier.height(14.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Build, "정비", tint = OrangeAccent, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("누적 정비비", fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Text(formatCost(totalMAllTime), fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.LocalGasStation, "주유", tint = SlateBlue, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("누적 주유비", fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Text(formatCost(totalFAllTime), fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Recent 6 Months bar graph using native Compose overlays
                item {
                    Text(
                        "최근 6개월 소비 흐름",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 10.dp)
                    )
                }

                item {
                    DadsCard {
                        if (totalMAllTime == 0L && totalFAllTime == 0L) {
                            Text(
                                "통계 그래프를 그릴 정비/주유 데이터가 없습니다. 주유소 가시거나 차량 정비하신 후에 입력해 보세요!",
                                fontSize = 16.sp,
                                color = Color.Gray,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        } else {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(240.dp)
                                    .padding(vertical = 10.dp),
                                verticalArrangement = Arrangement.SpaceEvenly
                            ) {
                                statsList.forEach { stat ->
                                    val total = stat.mCost + stat.fCost
                                    val relativeWeight = (total.toFloat() / maxCost.toFloat()).coerceIn(0.01f, 1f)

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Month label: 2026-06 -> 06월
                                        Text(
                                            text = "${stat.month.substring(5)}월",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            modifier = Modifier.width(48.dp)
                                        )

                                        // Relative horizontal bar representation with gradients
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(24.dp)
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxHeight()
                                                    .fillMaxWidth(relativeWeight)
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(
                                                        if (stat.mCost > stat.fCost) OrangeAccent else SlateBlue
                                                    )
                                            ) {}
                                        }

                                        Spacer(modifier = Modifier.width(10.dp))

                                        Text(
                                            text = if (total > 0) formatCost(total) else "0원",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            textAlign = TextAlign.End,
                                            modifier = Modifier.width(90.dp)
                                        )
                                    }
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 10.dp)) {
                                    Box(modifier = Modifier.size(10.dp).background(OrangeAccent, RoundedCornerShape(2.dp)))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("정비비 우세", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 10.dp)) {
                                    Box(modifier = Modifier.size(10.dp).background(SlateBlue, RoundedCornerShape(2.dp)))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("주유비 우세", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }

                // Category Breakdowns
                item {
                    Text(
                        "정비 유형별 지출",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 10.dp)
                    )
                }

                if (categoryStats.isEmpty()) {
                    item {
                        DadsCard {
                            Text("정비 교체 내역이 없습니다.", color = Color.Gray, fontSize = 15.sp)
                        }
                    }
                } else {
                    items(categoryStats) { stat ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(14.dp))
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .background(OrangeAccent, RoundedCornerShape(3.dp))
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(stat.type, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                            }
                            Text(formatCost(stat.totalCost), fontSize = 18.sp, fontWeight = FontWeight.Black)
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }
}

data class MonthStat(val month: String, val mCost: Long, val fCost: Long)
data class CategoryStat(val type: String, val totalCost: Long)
