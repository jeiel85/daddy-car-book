package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.MainActivity
import com.example.data.model.Vehicle
import com.example.ui.component.*
import com.example.ui.navigation.Screen
import com.example.ui.theme.GoldYellow
import com.example.ui.theme.NavyDark
import com.example.ui.theme.OrangeAccent
import com.example.ui.theme.SlateBlue
import com.example.ui.viewmodel.VehicleViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: VehicleViewModel,
    modifier: Modifier = Modifier
) {
    val vehicles by viewModel.allVehicles.collectAsState()
    val selectedVehicle by viewModel.selectedVehicle.collectAsState()
    val maintenanceRecords by viewModel.selectedVehicleMaintenanceRecords.collectAsState()
    val fuelRecords by viewModel.selectedVehicleFuelRecords.collectAsState()
    val reminders by viewModel.selectedVehicleReminders.collectAsState()

    var showVehicleMenu by remember { mutableStateOf(false) }

    // Aggregate monthly costs (Current Month)
    val currentMonthStr = remember {
        SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date())
    }

    val currentMonthMaintenanceCost = remember(maintenanceRecords, currentMonthStr) {
        maintenanceRecords.filter { it.date.startsWith(currentMonthStr) }.sumOf { it.cost }
    }

    val currentMonthFuelCost = remember(fuelRecords, currentMonthStr) {
        fuelRecords.filter { it.date.startsWith(currentMonthStr) }.sumOf { it.amount }
    }

    Scaffold(
        topBar = {
            DadsHeader(
                title = stringResource(id = com.example.R.string.app_name),
                actions = {
                    IconButton(onClick = { navController.navigate(Screen.Reminders.route) }) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "알람",
                            tint = OrangeAccent,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    IconButton(onClick = { navController.navigate(Screen.Settings.route) }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "설정",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            )
        },
        bottomBar = {
            // Main Navigation Dock for older dads
            Surface(
                tonalElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .navigationBarsPadding()
                        .padding(vertical = 12.dp, horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clickable { /* Already on Home */ }
                            .padding(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = "홈",
                            tint = OrangeAccent,
                            modifier = Modifier.size(30.dp)
                        )
                        Text("메인 홈", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = OrangeAccent)
                    }
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clickable { navController.navigate(Screen.Reminders.route) }
                            .padding(8.dp)
                    ) {
                        val activeRemindersCount = reminders.count { !it.isDone }
                        BadgedBox(
                            badge = {
                                if (activeRemindersCount > 0) {
                                    Badge(containerColor = Color.Red) {
                                        Text(activeRemindersCount.toString(), color = Color.White, fontSize = 12.sp)
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "알람",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(30.dp)
                            )
                        }
                        Text("할 일/알림", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clickable { navController.navigate(Screen.Statistics.route) }
                            .padding(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Analytics,
                            contentDescription = "통계",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(30.dp)
                        )
                        Text("소비통계", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Dropdown to switch vehicles (1-2 vehicles target)
            if (vehicles.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .background(
                                    MaterialTheme.colorScheme.surface,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable { showVehicleMenu = true }
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                                .align(Alignment.CenterStart),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.DirectionsCar,
                                contentDescription = "차량전환",
                                tint = SlateBlue,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = selectedVehicle?.name ?: "차량 선택",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        DropdownMenu(
                            expanded = showVehicleMenu,
                            onDismissRequest = { showVehicleMenu = false },
                            modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                        ) {
                            vehicles.forEach { vehicle ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            "${vehicle.name} (${vehicle.plateNumber})",
                                            fontSize = 17.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    },
                                    onClick = {
                                        viewModel.selectVehicle(vehicle.id)
                                        showVehicleMenu = false
                                    }
                                )
                            }
                            Divider()
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        "+ 새 차량 추가",
                                        color = OrangeAccent,
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                },
                                onClick = {
                                    navController.navigate(Screen.VehicleForm.createRoute())
                                    showVehicleMenu = false
                                }
                            )
                        }
                    }
                }
            }

            // If empty, show beautiful starter empty state
            if (vehicles.isEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(40.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .background(
                                    MaterialTheme.colorScheme.surfaceVariant,
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.DirectionsCar,
                                contentDescription = null,
                                tint = OrangeAccent,
                                modifier = Modifier.size(60.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "아직 등록된 차량이 없습니다",
                            style = MaterialTheme.typography.titleLarge.copy(fontSize = 22.sp),
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "우리 가족의 차량을 등록하고\n놓치기 쉬운 주요 정비, 보험 만기를\n꼼꼼하게 챙김 받으세요!",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontSize = 17.sp,
                                lineHeight = 24.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(32.dp))
                        DadsButton(
                            text = "첫 차량 등록하기 🚗",
                            onClick = { navController.navigate(Screen.VehicleForm.createRoute()) },
                            icon = Icons.Default.Add
                        )
                    }
                }
            } else {
                // Display vehicle card & upcoming alerts
                selectedVehicle?.let { vehicle ->
                    // 1. Interactive dashboard-style Vehicle Card
                    item {
                        DadsCard(
                            backgroundColor = MaterialTheme.colorScheme.surface,
                            onClick = { navController.navigate(Screen.VehicleForm.createRoute(vehicle.id)) }
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "${vehicle.manufacturer} ${vehicle.model}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = vehicle.name,
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontSize = 26.sp,
                                            fontWeight = FontWeight.ExtraBold
                                        ),
                                        color = OrangeAccent
                                    )
                                    Text(
                                        text = vehicle.plateNumber,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                Card(
                                    colors = CardDefaults.cardColors(containerColor = SlateBlue),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text(
                                        text = "${vehicle.year}년식",
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(18.dp))
                            Divider(color = MaterialTheme.colorScheme.surfaceVariant)
                            Spacer(modifier = Modifier.height(18.dp))

                            // Crucial Odometer Reading and dynamic expiration dates
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        "현재 주행거리",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.Speed,
                                            contentDescription = null,
                                            tint = SlateBlue,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = formatMileage(vehicle.currentMileage),
                                            style = MaterialTheme.typography.titleLarge.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 22.sp
                                            )
                                        )
                                    }
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    val (insDays, insColor) = getDaysRemaining(vehicle.insuranceExpireDate)
                                    val (inspDays, inspColor) = getDaysRemaining(vehicle.inspectionExpireDate)

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("보험 만기: ", fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(
                                            text = if (insDays < 0) "만기일 지남" else "D-$insDays",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            color = insColor
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("정기 검사: ", fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(
                                            text = if (inspDays < 0) "기한 지남" else "D-$inspDays",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            color = inspColor
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 2. Fast Input Entry Quick Buttons
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = { navController.navigate(Screen.MaintenanceForm.createRoute(vehicle.id)) },
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                                contentPadding = PaddingValues(16.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 64.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Build, contentDescription = "정비", tint = OrangeAccent)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("정비 기록", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                }
                            }

                            Button(
                                onClick = { navController.navigate(Screen.FuelForm.createRoute(vehicle.id)) },
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                                contentPadding = PaddingValues(16.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 64.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.LocalGasStation, contentDescription = "주유", tint = SlateBlue)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("주유 기록", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSecondaryContainer)
                                }
                            }
                        }
                    }

                    // 3. Priorities & Reminders Checklist - "곧 해야 할 것"
                    item {
                        Text(
                            text = "곧 해야 할 알람",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(top = 10.dp, bottom = 4.dp),
                            color = OrangeAccent
                        )
                    }

                    val pendingReminders = reminders.filter { !it.isDone }.take(3)
                    if (pendingReminders.isEmpty()) {
                        item {
                            DadsCard {
                                Text(
                                    "✨ 다가오는 정비나 만기 알람이 없습니다. 평온합니다!",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    } else {
                        items(pendingReminders) { reminder ->
                            val (days, color) = getDaysRemaining(reminder.dueDate)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        MaterialTheme.colorScheme.surface,
                                        shape = RoundedCornerShape(14.dp)
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = color.copy(alpha = 0.5f),
                                        shape = RoundedCornerShape(14.dp)
                                    )
                                    .clickable { viewModel.toggleReminderDone(reminder) }
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (days <= 7) Icons.Default.Warning else Icons.Default.CalendarToday,
                                    contentDescription = null,
                                    tint = color,
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = reminder.title,
                                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            "기한: ${reminder.dueDate}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        if (reminder.dueMileage > 0) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                "• 약 ${formatMileage(reminder.dueMileage)}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.15f)),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = if (days < 0) "초과" else "D-$days",
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        color = color,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 15.sp
                                    )
                                }
                            }
                        }
                    }

                    // 4. This Month's Expenses summary
                    item {
                        Text(
                            text = "이번 달 차량 유지비 요약",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(top = 10.dp, bottom = 4.dp)
                        )
                    }

                    item {
                        DadsCard(
                            backgroundColor = MaterialTheme.colorScheme.surface
                        ) {
                            Text(
                                text = "${currentMonthStr.substring(5)}월 총 소비 지출",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = formatCost(currentMonthMaintenanceCost + currentMonthFuelCost),
                                style = MaterialTheme.typography.displayMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    color = OrangeAccent
                                )
                            )

                            Spacer(modifier = Modifier.height(14.dp))
                            Divider(color = MaterialTheme.colorScheme.surfaceVariant)
                            Spacer(modifier = Modifier.height(14.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(12.dp)
                                            .background(OrangeAccent, CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("정비 기한비: ", fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(formatCost(currentMonthMaintenanceCost), fontSize = 17.sp, fontWeight = FontWeight.Bold)
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(12.dp)
                                            .background(SlateBlue, CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("주유 정비비: ", fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(formatCost(currentMonthFuelCost), fontSize = 17.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}
