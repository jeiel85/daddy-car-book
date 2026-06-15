package com.example.ui.screens

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.data.model.VehicleReminder
import com.example.ui.component.*
import com.example.ui.navigation.Screen
import com.example.ui.theme.GoldYellow
import com.example.ui.theme.OrangeAccent
import com.example.ui.theme.SlateBlue
import com.example.ui.viewmodel.VehicleViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun RemindersScreen(
    navController: NavController,
    viewModel: VehicleViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val selectedVehicle by viewModel.selectedVehicle.collectAsState()
    val reminders by viewModel.selectedVehicleReminders.collectAsState()

    var showAddAlert by remember { mutableStateOf(false) }

    // Alert form inputs
    var title by remember { mutableStateOf("") }
    var dueDate by remember { mutableStateOf("") }
    var dueMileage by remember { mutableStateOf("") }

    val calendar = Calendar.getInstance()
    var selectedDateFormatted = remember {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        sdf.format(Date())
    }

    LaunchedEffect(showAddAlert) {
        if (showAddAlert) {
            title = ""
            dueDate = selectedDateFormatted
            dueMileage = selectedVehicle?.currentMileage?.toString() ?: ""
        }
    }

    fun showDatePicker() {
        DatePickerDialog(
            context,
            { _, sYear, sMonth, sDay ->
                val fm = String.format("%02d", sMonth + 1)
                val fd = String.format("%02d", sDay)
                dueDate = "$sYear-$fm-$fd"
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    Scaffold(
        topBar = {
            DadsHeader(
                title = "차량 알람 점검 관리 ⏰",
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "홈",
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (selectedVehicle == null) {
                // Warning empty state
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Default.DirectionsCar, "경고", modifier = Modifier.size(72.dp), tint = Color.Gray)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("먼저 차량을 등록해주세요", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(24.dp))
                    DadsButton(text = "차량 등록하러 가기", onClick = { navController.navigate(Screen.VehicleForm.route) })
                }
            } else {
                val activeVehicle = selectedVehicle!!

                if (showAddAlert) {
                    // Alert Form
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "차량 수동 자가 알림 작성",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = OrangeAccent
                        )

                        DadsTextField(
                            value = title,
                            onValueChange = { title = it },
                            label = "알림 제목*",
                            placeholder = "예: 부동액 누수 상태 확인"
                        )

                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text("교환 검사 예정 날짜*", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                OutlinedTextField(
                                    value = dueDate,
                                    onValueChange = { dueDate = it },
                                    placeholder = { Text("YYYY-MM-DD") },
                                    textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = 19.sp),
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                IconButton(
                                    onClick = { showDatePicker() },
                                    modifier = Modifier
                                        .size(54.dp)
                                        .background(MaterialTheme.colorScheme.primaryContainer, shape = MaterialTheme.shapes.medium)
                                ) {
                                    Icon(Icons.Default.CalendarToday, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                                }
                            }
                        }

                        DadsTextField(
                            value = dueMileage,
                            onValueChange = { dueMileage = it },
                            label = "교환 예정 주행거리 (km)",
                            placeholder = "예: 52000 (0 지정 시 사용 안 함)",
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedButton(
                                onClick = { showAddAlert = false },
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(58.dp)
                            ) {
                                Text("작성 취소", fontSize = 17.sp)
                            }

                            Button(
                                onClick = {
                                    if (title.isBlank()) return@Button
                                    viewModel.addManualReminder(
                                        vehicleId = activeVehicle.id,
                                        title = title,
                                        dueDate = dueDate,
                                        dueMileage = dueMileage.toIntOrNull() ?: 0
                                    )
                                    showAddAlert = false
                                },
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent),
                                modifier = Modifier
                                    .weight(1.5f)
                                    .height(58.dp)
                            ) {
                                Text("알림 저장 완료", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                } else {
                    // Alert Lists (Differentiating: Upcoming and Completed)
                    val activeReminders = reminders.filter { !it.isDone }
                    val completedReminders = reminders.filter { it.isDone }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "활성 알림 (${activeReminders.size}건)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = OrangeAccent
                        )

                        Button(
                            onClick = { showAddAlert = true },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Add, contentDescription = null, size = 18.dp, tint = SlateBlue)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("알림 추가", color = SlateBlue, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        if (activeReminders.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        "✨ 모든 알림을 완벽히 점검 완료했습니다! 기분 좋은 드라이브 되세요.",
                                        fontSize = 17.sp,
                                        color = Color.Gray,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        } else {
                            items(activeReminders) { reminder ->
                                val (days, color) = getDaysRemaining(reminder.dueDate)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(
                                            MaterialTheme.colorScheme.surface,
                                            shape = RoundedCornerShape(16.dp)
                                        )
                                        .clickable { viewModel.toggleReminderDone(reminder) }
                                        .padding(18.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = false,
                                        onCheckedChange = { viewModel.toggleReminderDone(reminder) },
                                        colors = CheckboxDefaults.colors(uncheckedColor = OrangeAccent)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = reminder.title,
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                "점검 기한: ${reminder.dueDate}",
                                                fontSize = 14.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            if (reminder.dueMileage > 0) {
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    "• 주행 기한: ${formatMileage(reminder.dueMileage)}",
                                                    fontSize = 14.sp,
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
                                            text = if (days < 0) "지남" else "D-$days",
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            color = color,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 14.sp
                                        )
                                    }
                                }
                            }
                        }

                        // Completed list section
                        if (completedReminders.isNotEmpty()) {
                            item {
                                Spacer(modifier = Modifier.height(14.dp))
                                Text(
                                    text = "완료된 항목 (${completedReminders.size}건)",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(bottom = 8.dp),
                                    color = Color.Gray
                                )
                            }

                            items(completedReminders) { reminder ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color.Gray.copy(alpha = 0.1f), shape = RoundedCornerShape(16.dp))
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = true,
                                        onCheckedChange = { viewModel.toggleReminderDone(reminder) }
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = reminder.title,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Medium,
                                            style = LocalTextStyle.current.copy(lineThrough = true),
                                            color = Color.Gray
                                        )
                                        Text(
                                            "기한: ${reminder.dueDate}",
                                            fontSize = 13.sp,
                                            color = Color.Gray
                                        )
                                    }

                                    IconButton(onClick = { viewModel.deleteReminder(reminder) }) {
                                        Icon(Icons.Default.Delete, "삭제", tint = Color.Gray.copy(alpha = 0.6f), modifier = Modifier.size(20.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
