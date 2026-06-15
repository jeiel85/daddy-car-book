package com.jeiel85.daddycarbook.ui.screens

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import com.jeiel85.daddycarbook.data.model.MaintenanceRecord
import com.jeiel85.daddycarbook.ui.component.*
import com.jeiel85.daddycarbook.ui.navigation.Screen
import com.jeiel85.daddycarbook.ui.theme.GoldYellow
import com.jeiel85.daddycarbook.ui.theme.OrangeAccent
import com.jeiel85.daddycarbook.ui.theme.SlateBlue
import com.jeiel85.daddycarbook.ui.viewmodel.VehicleViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun MaintenanceScreen(
    navController: NavController,
    viewModel: VehicleViewModel,
    vehicleId: Long,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val vehicles by viewModel.allVehicles.collectAsState()
    val activeVehicle = remember(vehicles, vehicleId) {
        vehicles.find { it.id == vehicleId }
    }

    val records by viewModel.selectedVehicleMaintenanceRecords.collectAsState()

    var showAddForm by remember { mutableStateOf(false) }

    // Form inputs
    val categories = listOf("엔진오일", "타이어", "배터리", "브레이크 패드", "와이퍼", "에어컨 필터", "기타 정비")
    var selectedCategory by remember { mutableStateOf(categories.first()) }
    var customCategory by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var mileage by remember { mutableStateOf("") }
    var cost by remember { mutableStateOf("") }
    var memo by remember { mutableStateOf("") }
    
    // Schedule intervals
    var nextDueDate by remember { mutableStateOf("") }
    var nextDueMileage by remember { mutableStateOf("") }

    var recordToDelete by remember { mutableStateOf<MaintenanceRecord?>(null) }
    var costError by remember { mutableStateOf<String?>(null) }
    var mileageError by remember { mutableStateOf<String?>(null) }

    // Seed defaults
    val calendar = Calendar.getInstance()
    var selectedDateFormatted = remember {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        sdf.format(Date())
    }

    fun updateIntervals(cat: String, currentMil: Int) {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val cal = Calendar.getInstance()
        when (cat) {
            "엔진오일" -> {
                cal.add(Calendar.MONTH, 6)
                nextDueDate = sdf.format(cal.time)
                nextDueMileage = (currentMil + 10000).toString()
            }
            "타이어" -> {
                cal.add(Calendar.MONTH, 24)
                nextDueDate = sdf.format(cal.time)
                nextDueMileage = (currentMil + 40000).toString()
            }
            "배터리" -> {
                cal.add(Calendar.MONTH, 36)
                nextDueDate = sdf.format(cal.time)
                nextDueMileage = (currentMil + 60000).toString()
            }
            "브레이크 패드" -> {
                cal.add(Calendar.MONTH, 18)
                nextDueDate = sdf.format(cal.time)
                nextDueMileage = (currentMil + 40000).toString()
            }
            "와이퍼" -> {
                cal.add(Calendar.MONTH, 6)
                nextDueDate = sdf.format(cal.time)
                nextDueMileage = "0"
            }
            "에어컨 필터" -> {
                cal.add(Calendar.MONTH, 6)
                nextDueDate = sdf.format(cal.time)
                nextDueMileage = (currentMil + 12000).toString()
            }
            else -> {
                nextDueDate = ""
                nextDueMileage = ""
            }
        }
    }

    LaunchedEffect(showAddForm, activeVehicle) {
        if (showAddForm) {
            date = selectedDateFormatted
            mileage = activeVehicle?.currentMileage?.toString() ?: ""
            cost = ""
            memo = ""
            // Auto schedule next cycles
            updateIntervals(selectedCategory, activeVehicle?.currentMileage ?: 0)
        }
    }

    // DatePicker Dialog setup
    fun showDatePickerForForm(isNext: Boolean) {
        DatePickerDialog(
            context,
            { _, sYear, sMonth, sDay ->
                val fm = String.format("%02d", sMonth + 1)
                val fd = String.format("%02d", sDay)
                if (isNext) {
                    nextDueDate = "$sYear-$fm-$fd"
                } else {
                    date = "$sYear-$fm-$fd"
                }
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    Scaffold(
        topBar = {
            DadsHeader(
                title = if (showAddForm) "정비 기록하기 🛠️" else "정비 내역 노트 📓",
                navigationIcon = {
                    IconButton(onClick = {
                        if (showAddForm) showAddForm = false else navController.navigateUp()
                    }) {
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
        floatingActionButton = {
            if (!showAddForm && activeVehicle != null) {
                LargeFloatingActionButton(
                    onClick = { showAddForm = true },
                    containerColor = OrangeAccent,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Icon(imageVector = Icons.Default.Build, contentDescription = "기록 추가", modifier = Modifier.size(36.dp))
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        if (showAddForm && activeVehicle != null) {
            // Form Screen
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "${activeVehicle.name} 정비 기록 작성",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Selectable standard items check categories (Horizontal wrapped style)
                Text(
                    text = "정비 항목 선택",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Fast row category select
                    Column {
                        val row1 = categories.take(4)
                        val row2 = categories.drop(4)
                        
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(bottom = 6.dp)) {
                            row1.forEach { cat ->
                                val isSelected = selectedCategory == cat
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { 
                                        selectedCategory = cat
                                        updateIntervals(cat, mileage.toIntOrNull() ?: activeVehicle.currentMileage)
                                    },
                                    label = { Text(cat, fontSize = 15.sp, fontWeight = FontWeight.Bold) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = OrangeAccent,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            row2.forEach { cat ->
                                val isSelected = selectedCategory == cat
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { 
                                        selectedCategory = cat
                                        updateIntervals(cat, mileage.toIntOrNull() ?: activeVehicle.currentMileage)
                                    },
                                    label = { Text(cat, fontSize = 15.sp, fontWeight = FontWeight.Bold) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = OrangeAccent,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }
                    }
                }

                if (selectedCategory == "기타 정비") {
                    DadsTextField(
                        value = customCategory,
                        onValueChange = { customCategory = it },
                        label = "기타 정비 이름 입력*",
                        placeholder = "예: 부동액 누수 수리"
                    )
                }

                // Date
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "정비 일자",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = date,
                            onValueChange = { date = it },
                            placeholder = { Text("YYYY-MM-DD") },
                            modifier = Modifier.weight(1f),
                            textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = 19.sp),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        IconButton(
                            onClick = { showDatePickerForForm(false) },
                            modifier = Modifier
                                .size(54.dp)
                                .background(MaterialTheme.colorScheme.primaryContainer, shape = MaterialTheme.shapes.medium)
                        ) {
                            Icon(Icons.Default.CalendarToday, contentDescription = "달력 선택", tint = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                    }
                }

                // Mileage at maintenance
                DadsTextField(
                    value = mileage,
                    onValueChange = {
                        mileage = it
                        mileageError = if (it.toIntOrNull() == null) "주행거리는 숫자만 적어주세요." else null
                    },
                    label = "정비 당시 주행거리 (km)*",
                    placeholder = "예: 42000",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = mileageError != null,
                    errorMessage = mileageError
                )

                // Cost
                DadsTextField(
                    value = cost,
                    onValueChange = {
                        cost = it
                        costError = if (it.toLongOrNull() == null) "금액은 숫자만 정수로 적어주세요." else null
                    },
                    label = "정비 지출 비용 (원)*",
                    placeholder = "예: 75000",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = costError != null,
                    errorMessage = costError
                )

                // Next Due settings for automatic alert triggering!
                Text(
                    text = "다음 정비 주기 설정 (알람 알림 예정)",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = OrangeAccent,
                    modifier = Modifier.padding(top = 10.dp)
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("다음 정비 기한일", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = nextDueDate,
                                onValueChange = { nextDueDate = it },
                                placeholder = { Text("YYYY-MM-DD") },
                                textStyle = MaterialTheme.typography.bodyMedium,
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            IconButton(onClick = { showDatePickerForForm(true) }, modifier = Modifier.size(44.dp)) {
                                Icon(Icons.Default.CalendarToday, contentDescription = null, tint = OrangeAccent)
                            }
                        }
                    }

                    Column(modifier = Modifier.weight(1.1f)) {
                        Text("다음 정비 주행거리(km)", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                        OutlinedTextField(
                            value = nextDueMileage,
                            onValueChange = { nextDueMileage = it },
                            placeholder = { Text("예: 52000") },
                            textStyle = MaterialTheme.typography.bodyMedium,
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // Memo
                DadsTextField(
                    value = memo,
                    onValueChange = { memo = it },
                    label = "간단한 정비 메모",
                    placeholder = "예: 현대 블루핸즈 대현점에서 순정 오일 교환 필터 포함"
                )

                Spacer(modifier = Modifier.height(18.dp))

                DadsButton(
                    text = "정비 내역 저장하기 💾",
                    onClick = {
                        val mInt = mileage.toIntOrNull() ?: 0
                        val cLong = cost.toLongOrNull() ?: 0L
                        val finalCategory = if (selectedCategory == "기타 정비") customCategory else selectedCategory
                        
                        if (finalCategory.isBlank()) {
                            return@DadsButton
                        }

                        viewModel.addMaintenanceRecord(
                            vehicleId = vehicleId,
                            type = finalCategory,
                            date = date,
                            mileage = mInt,
                            cost = cLong,
                            memo = memo,
                            nextDueDate = nextDueDate,
                            nextDueMileage = nextDueMileage.toIntOrNull() ?: 0
                        )
                        showAddForm = false
                    },
                    icon = Icons.Default.Check
                )
                Spacer(modifier = Modifier.height(30.dp))
            }
        } else {
            // History list
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (records.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Assignment,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(80.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "정비 기록 노트가 비어있습니다",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "아직 등록된 교체 정비 이력이 없습니다.\n정비소 다녀오신 후에 우측 하단 '+'버튼을\n눌러 첫 기록을 모아보세요!",
                            fontSize = 16.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center,
                            lineHeight = 22.sp
                        )
                    }
                } else {
                    Text(
                        text = "이전 정비 일지 (${records.size}건)",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp),
                        color = SlateBlue
                    )

                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(records) { record ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        MaterialTheme.colorScheme.surface,
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                    .padding(20.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.BuildCircle,
                                            contentDescription = null,
                                            tint = OrangeAccent,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = record.type,
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        "정비일자: ${record.date}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        "주행거리: ${formatMileage(record.mileage)}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    if (record.memo.isNotEmpty()) {
                                        Text(
                                            "메모: ${record.memo}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                        )
                                    }
                                    if (record.nextDueDate.isNotEmpty()) {
                                        Text(
                                            "다음주기: ${record.nextDueDate} (약 +${formatMileage(record.nextDueMileage - record.mileage)})",
                                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                            color = SlateBlue
                                        )
                                    }
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = formatCost(record.cost),
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = OrangeAccent
                                    )
                                    Spacer(modifier = Modifier.height(14.dp))
                                    IconButton(
                                        onClick = { recordToDelete = record }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "삭제",
                                            tint = Color.Gray.copy(alpha = 0.7f),
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Delete record confirmation
    recordToDelete?.let { record ->
        AlertDialog(
            onDismissRequest = { recordToDelete = null },
            title = { Text("정비 이력 영구 삭제", fontSize = 20.sp, fontWeight = FontWeight.Bold) },
            text = { Text("기록된 정비 내역(${record.type} - ${formatCost(record.cost)})를 목록에서 영구 삭제하시겠습니까?", fontSize = 17.sp) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteMaintenanceRecord(record)
                        recordToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("삭제", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { recordToDelete = null }) {
                    Text("취소", fontSize = 16.sp)
                }
            }
        )
    }
}
