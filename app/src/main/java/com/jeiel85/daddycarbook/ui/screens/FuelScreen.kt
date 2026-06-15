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
import com.jeiel85.daddycarbook.data.model.FuelRecord
import com.jeiel85.daddycarbook.ui.component.*
import com.jeiel85.daddycarbook.ui.navigation.Screen
import com.jeiel85.daddycarbook.ui.theme.OrangeAccent
import com.jeiel85.daddycarbook.ui.theme.SlateBlue
import com.jeiel85.daddycarbook.ui.viewmodel.VehicleViewModel
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun FuelScreen(
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

    val records by viewModel.selectedVehicleFuelRecords.collectAsState()

    var showAddForm by remember { mutableStateOf(false) }

    // Form inputs
    var date by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var liters by remember { mutableStateOf("") }
    var mileage by remember { mutableStateOf("") }
    var memo by remember { mutableStateOf("") }

    var recordToDelete by remember { mutableStateOf<FuelRecord?>(null) }
    var amountError by remember { mutableStateOf<String?>(null) }
    var litersError by remember { mutableStateOf<String?>(null) }
    var mileageError by remember { mutableStateOf<String?>(null) }

    val calendar = Calendar.getInstance()
    var selectedDateFormatted = remember {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        sdf.format(Date())
    }

    LaunchedEffect(showAddForm, activeVehicle) {
        if (showAddForm) {
            date = selectedDateFormatted
            mileage = activeVehicle?.currentMileage?.toString() ?: ""
            amount = ""
            liters = ""
            memo = ""
        }
    }

    // DatePicker Dialog setup
    fun showDatePickerForForm() {
        DatePickerDialog(
            context,
            { _, sYear, sMonth, sDay ->
                val fm = String.format("%02d", sMonth + 1)
                val fd = String.format("%02d", sDay)
                date = "$sYear-$fm-$fd"
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    // Calculations
    val monthStr = remember { SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date()) }
    val thisMonthFuelTotal = remember(records, monthStr) {
        records.filter { it.date.startsWith(monthStr) }.sumOf { it.amount }
    }

    Scaffold(
        topBar = {
            DadsHeader(
                title = if (showAddForm) "주유기록 작성하기 ⛽" else "우리차 주유장부 📓",
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
                    Icon(imageVector = Icons.Default.LocalGasStation, contentDescription = "기록 추가", modifier = Modifier.size(36.dp))
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
                    text = "${activeVehicle.name} 주유 영 수증 기록",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Date
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "주유한 날짜",
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
                            onClick = { showDatePickerForForm() },
                            modifier = Modifier
                                .size(54.dp)
                                .background(MaterialTheme.colorScheme.primaryContainer, shape = MaterialTheme.shapes.medium)
                        ) {
                            Icon(Icons.Default.CalendarToday, contentDescription = "달력 선택", tint = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                    }
                }

                // Amount
                DadsTextField(
                    value = amount,
                    onValueChange = {
                        amount = it
                        amountError = if (it.toLongOrNull() == null) "주유비 금액은 정수 숫자만 입력 가능합니다." else null
                    },
                    label = "주유 금액 (원)*",
                    placeholder = "예: 50000",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = amountError != null,
                    errorMessage = amountError
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Liters (Double Type)
                    DadsTextField(
                        value = liters,
                        onValueChange = {
                            liters = it
                            litersError = if (it.toDoubleOrNull() == null) "소수점 숫자로 적어주세요." else null
                        },
                        label = "주유 리터 (L)",
                        placeholder = "예: 32.5",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                        isError = litersError != null,
                        errorMessage = litersError
                    )

                    // Current Mileage (Int)
                    DadsTextField(
                        value = mileage,
                        onValueChange = {
                            mileage = it
                            mileageError = if (it.toIntOrNull() == null) "주행거리는 숫자만 적어주세요." else null
                        },
                        label = "당시 주행거리 (km)*",
                        placeholder = "예: 42000",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1.2f),
                        isError = mileageError != null,
                        errorMessage = mileageError
                    )
                }

                // Gas station name or Memo
                DadsTextField(
                    value = memo,
                    onValueChange = { memo = it },
                    label = "단골 주유소명 또는 메모",
                    placeholder = "예: SK에너지 강남주유소"
                )

                Spacer(modifier = Modifier.height(18.dp))

                DadsButton(
                    text = "주유 영수증 기록 저장",
                    onClick = {
                        val amtLong = amount.toLongOrNull() ?: 0L
                        val litDouble = liters.toDoubleOrNull() ?: 0.0
                        val milInt = mileage.toIntOrNull() ?: 0

                        if (amtLong <= 0 || milInt <= 0) {
                            return@DadsButton
                        }

                        viewModel.addFuelRecord(
                            vehicleId = vehicleId,
                            date = date,
                            amount = amtLong,
                            liters = litDouble,
                            mileage = milInt,
                            memo = memo
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
                // Header Stat bar
                if (records.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .padding(20.dp)
                    ) {
                        Column {
                            Text(
                                "이번 달(${monthStr.substring(5)}월) 지출한 주유비",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                formatCost(thisMonthFuelTotal),
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Black,
                                color = OrangeAccent
                            )
                        }
                    }
                }

                if (records.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalGasStation,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(80.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "아직 주유 기록이 없습니다",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "우리차 주유 장부가 완전히 비어있습니다.\n가까운 주유소에서 주유한 다음에,\n우측 하단의 '+' 버튼을 눌러보세요!",
                            fontSize = 16.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center,
                            lineHeight = 22.sp
                        )
                    }
                } else {
                    Text(
                        text = "이전 주유 기록 (${records.size}건)",
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
                                Column(modifier = Modifier.weight(1.1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.LocalGasStation,
                                            contentDescription = null,
                                            tint = SlateBlue,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (record.memo.isNotEmpty()) record.memo else "주유소",
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        "주유일자: ${record.date}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        "기록 주행거리: ${formatMileage(record.mileage)}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    if (record.liters > 0) {
                                        val singleFormat = DecimalFormat("0.00")
                                        val pricePerLiter = record.amount / record.liters
                                        Text(
                                            "리터양: ${record.liters} L (리터당 단가 약 ${singleFormat.format(pricePerLiter)}원)",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                        )
                                    }
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = formatCost(record.amount),
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = OrangeAccent
                                    )
                                    Spacer(modifier = Modifier.height(14.dp))
                                    IconButton(onClick = { recordToDelete = record }) {
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
            title = { Text("주유 영수증 삭제", fontSize = 20.sp, fontWeight = FontWeight.Bold) },
            text = { Text("기록된 주유 장부(${formatCost(record.amount)} - ${record.date})를 목록에서 완전히 삭제하시겠습니까?", fontSize = 17.sp) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteFuelRecord(record)
                        recordToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("예, 삭제합니다", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
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
