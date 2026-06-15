package com.example.ui.screens

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.MainActivity
import com.example.data.model.Vehicle
import com.example.ui.component.DadsButton
import com.example.ui.component.DadsHeader
import com.example.ui.component.DadsTextField
import com.example.ui.theme.OrangeAccent
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun VehicleFormScreen(
    navController: NavController,
    viewModel: VehicleViewModel,
    vehicleId: Long?,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val vehicles by viewModel.allVehicles.collectAsState()
    val isEditMode = vehicleId != null
    val targetVehicle = remember(vehicles, vehicleId) {
        if (isEditMode) vehicles.find { it.id == vehicleId } else null
    }

    var name by remember { mutableStateOf("") }
    var plateNumber by remember { mutableStateOf("") }
    var manufacturer by remember { mutableStateOf("") }
    var model by remember { mutableStateOf("") }
    var year by remember { mutableStateOf("") }
    var currentMileage by remember { mutableStateOf("") }
    var insuranceExpireDate by remember { mutableStateOf("") }
    var inspectionExpireDate by remember { mutableStateOf("") }

    var nameError by remember { mutableStateOf<String?>(null) }
    var mileageError by remember { mutableStateOf<String?>(null) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    // Seed fields from edit
    LaunchedEffect(targetVehicle) {
        targetVehicle?.let { v ->
            name = v.name
            plateNumber = v.plateNumber
            manufacturer = v.manufacturer
            model = v.model
            year = v.year.toString()
            currentMileage = v.currentMileage.toString()
            insuranceExpireDate = v.insuranceExpireDate
            inspectionExpireDate = v.inspectionExpireDate
        }
    }

    // DatePicker Helpers for Dads
    val calendar = Calendar.getInstance()
    fun showDatePicker(onDateSelected: (String) -> Unit) {
        DatePickerDialog(
            context,
            { _, sYear, sMonth, sDay ->
                val formattedMonth = String.format("%02d", sMonth + 1)
                val formattedDay = String.format("%02d", sDay)
                onDateSelected("$sYear-$formattedMonth-$formattedDay")
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    Scaffold(
        topBar = {
            DadsHeader(
                title = if (isEditMode) "차량 수정/관리 ⚙️" else "새 차량 등록 🚗",
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "이전",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                },
                actions = {
                    if (isEditMode && targetVehicle != null) {
                        IconButton(onClick = { showDeleteConfirm = true }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "삭제",
                                tint = Color.Red,
                                modifier = Modifier.size(32.dp)
                            )
                        }
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
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            Text(
                text = "차량 기초 정보",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = OrangeAccent,
                modifier = Modifier.padding(bottom = 10.dp)
            )

            DadsTextField(
                value = name,
                onValueChange = {
                    name = it
                    nameError = if (it.isBlank()) "차량 별명/이름을 적어주세요." else null
                },
                label = "차량 별명 (예: 아빠 제네시스, 가족 패밀리카)*",
                placeholder = "예: 우리 쏘렌토",
                isError = nameError != null,
                errorMessage = nameError
            )

            DadsTextField(
                value = plateNumber,
                onValueChange = { plateNumber = it },
                label = "차량 번호 (예: 12가 3456)",
                placeholder = "예: 123가 4567"
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                DadsTextField(
                    value = manufacturer,
                    onValueChange = { manufacturer = it },
                    label = "제조사",
                    placeholder = "예: 기아",
                    modifier = Modifier.weight(1f)
                )

                DadsTextField(
                    value = model,
                    onValueChange = { model = it },
                    label = "모델명",
                    placeholder = "예: 쏘렌토",
                    modifier = Modifier.weight(1f)
                )
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                DadsTextField(
                    value = year,
                    onValueChange = { year = it },
                    label = "연식 (연도)",
                    placeholder = "예: 2022",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1.5f)
                )

                DadsTextField(
                    value = currentMileage,
                    onValueChange = {
                        currentMileage = it
                        mileageError = if (it.toIntOrNull() == null && it.isNotBlank()) "주행거리는 숫자만 적어주세요." else null
                    },
                    label = "아래 주행거리 (km)*",
                    placeholder = "예: 42000",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(2f),
                    isError = mileageError != null,
                    errorMessage = mileageError
                )
            }

            Spacer(modifier = Modifier.height(18.dp))
            Divider(color = MaterialTheme.colorScheme.surfaceVariant)
            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "주요 만기 기한 일정",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = OrangeAccent,
                modifier = Modifier.padding(bottom = 10.dp)
            )

            // Date pickers using read-only text fields + calendar icon buttons
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "보험 의무 가입 만기 만료일",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = insuranceExpireDate,
                        onValueChange = { insuranceExpireDate = it },
                        placeholder = { Text("YYYY-MM-DD", fontSize = 17.sp) },
                        modifier = Modifier.weight(1f),
                        textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = 19.sp),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    IconButton(
                        onClick = { showDatePicker { insuranceExpireDate = it } },
                        modifier = Modifier
                            .size(54.dp)
                            .background(MaterialTheme.colorScheme.primaryContainer, shape = MaterialTheme.shapes.medium)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = "달력 선택",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "정기 자동차 종합 검사 만료일",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = inspectionExpireDate,
                        onValueChange = { inspectionExpireDate = it },
                        placeholder = { Text("YYYY-MM-DD", fontSize = 17.sp) },
                        modifier = Modifier.weight(1f),
                        textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = 19.sp),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    IconButton(
                        onClick = { showDatePicker { inspectionExpireDate = it } },
                        modifier = Modifier
                            .size(54.dp)
                            .background(MaterialTheme.colorScheme.primaryContainer, shape = MaterialTheme.shapes.medium)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = "달력 선택",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            // Save action
            DadsButton(
                text = if (isEditMode) "차량 수정 완료 변경 저장" else "새로운 차량 우리 집차로 등록",
                onClick = {
                    if (name.isBlank()) {
                        nameError = "차량 별명/이름을 적어주세요."
                        return@DadsButton
                    }
                    val mileageInt = currentMileage.toIntOrNull() ?: 0

                    if (isEditMode && targetVehicle != null) {
                        viewModel.updateVehicle(
                            targetVehicle.copy(
                                name = name,
                                plateNumber = plateNumber,
                                manufacturer = manufacturer,
                                model = model,
                                year = year.toIntOrNull() ?: calendar.get(Calendar.YEAR),
                                currentMileage = mileageInt,
                                insuranceExpireDate = insuranceExpireDate,
                                inspectionExpireDate = inspectionExpireDate
                            )
                        )
                    } else {
                        viewModel.addVehicle(
                            name = name,
                            plateNumber = plateNumber,
                            manufacturer = manufacturer,
                            model = model,
                            year = year.toIntOrNull() ?: calendar.get(Calendar.YEAR),
                            currentMileage = mileageInt,
                            insuranceExpireDate = insuranceExpireDate,
                            inspectionExpireDate = inspectionExpireDate
                        )
                    }
                    navController.navigateUp()
                },
                icon = Icons.Default.DirectionsCar
            )
            Spacer(modifier = Modifier.height(20.dp))
        }
    }

    // Confirm Delete Dialog for older fathers (Big text & safe action buttons!)
    if (showDeleteConfirm && targetVehicle != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            icon = { Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red, modifier = Modifier.size(40.dp)) },
            title = { Text("차량 데이터를 삭제합니까?", fontSize = 22.sp, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "차량(${targetVehicle.name}) 정보를 삭제하면, 해당 차량과 관련된 모든 정비 기록, 주유 기록, 알림들이 영구 삭제됩니다. 계속 진행하시겠습니까?",
                    fontSize = 17.sp,
                    lineHeight = 24.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteVehicle(targetVehicle)
                        showDeleteConfirm = false
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("예, 영구 삭제합니다", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showDeleteConfirm = false },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("취소", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                }
            }
        )
    }
}
