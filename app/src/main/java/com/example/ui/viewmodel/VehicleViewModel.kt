package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.data.database.VehicleDatabase
import com.example.data.model.FuelRecord
import com.example.data.model.MaintenanceRecord
import com.example.data.model.Vehicle
import com.example.data.model.VehicleReminder
import com.example.data.repository.VehicleRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class VehicleViewModel(
    private val application: Application,
    private val repository: VehicleRepository
) : AndroidViewModel(application) {

    // --- State Flow Outputs ---
    val allVehicles: StateFlow<List<Vehicle>> = repository.allVehicles
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val selectedVehicleId = MutableStateFlow<Long?>(null)

    val selectedVehicle: StateFlow<Vehicle?> = combine(allVehicles, selectedVehicleId) { vehicles, id ->
        if (vehicles.isEmpty()) null
        else if (id != null) vehicles.find { it.id == id } ?: vehicles.firstOrNull()
        else vehicles.firstOrNull()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allMaintenanceRecords: StateFlow<List<MaintenanceRecord>> = repository.allMaintenanceRecords
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allFuelRecords: StateFlow<List<FuelRecord>> = repository.allFuelRecords
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allReminders: StateFlow<List<VehicleReminder>> = repository.allReminders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Filtered Data ---
    val selectedVehicleMaintenanceRecords: StateFlow<List<MaintenanceRecord>> = combine(allMaintenanceRecords, selectedVehicle) { records, vehicle ->
        if (vehicle == null) emptyList()
        else records.filter { it.vehicleId == vehicle.id }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val selectedVehicleFuelRecords: StateFlow<List<FuelRecord>> = combine(allFuelRecords, selectedVehicle) { records, vehicle ->
        if (vehicle == null) emptyList()
        else records.filter { it.vehicleId == vehicle.id }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val selectedVehicleReminders: StateFlow<List<VehicleReminder>> = combine(allReminders, selectedVehicle) { reminders, vehicle ->
         if (vehicle == null) emptyList()
         else reminders.filter { it.vehicleId == vehicle.id }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())


    // --- Actions ---
    fun selectVehicle(vehicleId: Long) {
        selectedVehicleId.value = vehicleId
    }

    // Vehicle CRUD
    fun addVehicle(name: String, plateNumber: String, manufacturer: String, model: String, year: Int, currentMileage: Int, insuranceExpireDate: String, inspectionExpireDate: String) {
        viewModelScope.launch {
            val id = repository.insertVehicle(
                Vehicle(
                    name = name,
                    plateNumber = plateNumber,
                    manufacturer = manufacturer,
                    model = model,
                    year = year,
                    currentMileage = currentMileage,
                    insuranceExpireDate = insuranceExpireDate,
                    inspectionExpireDate = inspectionExpireDate
                )
            )
            // Auto select newly added vehicle if none selected
            if (selectedVehicleId.value == null) {
                selectedVehicleId.value = id
            }
            // Generate standard reminders
            generateDefaultRemindersForVehicle(id, currentMileage, insuranceExpireDate, inspectionExpireDate)
        }
    }

    fun updateVehicle(vehicle: Vehicle) {
        viewModelScope.launch {
            repository.updateVehicle(vehicle)
        }
    }

    fun deleteVehicle(vehicle: Vehicle) {
        viewModelScope.launch {
            repository.deleteVehicle(vehicle)
            // Clear selected if same
            if (selectedVehicleId.value == vehicle.id) {
                selectedVehicleId.value = null
            }
        }
    }

    // Maintenance CRUD
    fun addMaintenanceRecord(vehicleId: Long, type: String, date: String, mileage: Int, cost: Long, memo: String, nextDueDate: String, nextDueMileage: Int) {
        viewModelScope.launch {
            repository.insertMaintenanceRecord(
                MaintenanceRecord(
                    vehicleId = vehicleId,
                    type = type,
                    date = date,
                    mileage = mileage,
                    cost = cost,
                    memo = memo,
                    nextDueDate = nextDueDate,
                    nextDueMileage = nextDueMileage
                )
            )

            // Update Vehicle current odometer if these records are higher
            val v = repository.getVehicleById(vehicleId)
            if (v != null && mileage > v.currentMileage) {
                repository.updateVehicle(v.copy(currentMileage = mileage))
            }

            // Create reminder for the next replacement of this maintenance item
            if (nextDueDate.isNotEmpty()) {
                repository.insertReminder(
                    VehicleReminder(
                        vehicleId = vehicleId,
                        title = "$type 교체 알람",
                        dueDate = nextDueDate,
                        dueMileage = nextDueMileage,
                        isDone = false
                    )
                )
            }
        }
    }

    fun deleteMaintenanceRecord(record: MaintenanceRecord) {
        viewModelScope.launch {
            repository.deleteMaintenanceRecord(record)
        }
    }

    // Fuel CRUD
    fun addFuelRecord(vehicleId: Long, date: String, amount: Long, liters: Double, mileage: Int, memo: String) {
        viewModelScope.launch {
            repository.insertFuelRecord(
                FuelRecord(
                    vehicleId = vehicleId,
                    date = date,
                    amount = amount,
                    liters = liters,
                    mileage = mileage,
                    memo = memo
                )
            )
            // Update Vehicle current odometer if fuel odometer is higher
            val v = repository.getVehicleById(vehicleId)
            if (v != null && mileage > v.currentMileage) {
                repository.updateVehicle(v.copy(currentMileage = mileage))
            }
        }
    }

    fun deleteFuelRecord(record: FuelRecord) {
        viewModelScope.launch {
            repository.deleteFuelRecord(record)
        }
    }

    // Reminders CRUD
    fun toggleReminderDone(reminder: VehicleReminder) {
        viewModelScope.launch {
            repository.updateReminder(reminder.copy(isDone = !reminder.isDone))
        }
    }

    fun deleteReminder(reminder: VehicleReminder) {
        viewModelScope.launch {
            repository.deleteReminder(reminder)
        }
    }

    fun addManualReminder(vehicleId: Long, title: String, dueDate: String, dueMileage: Int) {
        viewModelScope.launch {
            repository.insertReminder(
                VehicleReminder(
                    vehicleId = vehicleId,
                    title = title,
                    dueDate = dueDate,
                    dueMileage = dueMileage,
                    isDone = false
                )
            )
        }
    }


    // --- Helper to seed Default Reminders ---
    private suspend fun generateDefaultRemindersForVehicle(vehicleId: Long, mileage: Int, insExp: String, inspExp: String) {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val cal = Calendar.getInstance()

        // 1. Insurance reminder (14 days before expire)
        if (insExp.isNotEmpty()) {
            try {
                val insDate = sdf.parse(insExp)
                if (insDate != null) {
                    cal.time = insDate
                    cal.add(Calendar.DAY_OF_YEAR, -14)
                    repository.insertReminder(
                        VehicleReminder(
                            vehicleId = vehicleId,
                            title = "자동차 보험 갱신 (만기 2주 전)",
                            dueDate = sdf.format(cal.time),
                            dueMileage = 0,
                            isDone = false
                        )
                    )
                }
            } catch (e: Exception) { e.printStackTrace() }
        }

        // 2. Inspection reminder (30 days before expire)
        if (inspExp.isNotEmpty()) {
            try {
                val inspDate = sdf.parse(inspExp)
                if (inspDate != null) {
                    cal.time = inspDate
                    cal.add(Calendar.DAY_OF_YEAR, -30)
                    repository.insertReminder(
                        VehicleReminder(
                            vehicleId = vehicleId,
                            title = "정기 자동차 검사 준비",
                            dueDate = sdf.format(cal.time),
                            dueMileage = 0,
                            isDone = false
                        )
                    )
                }
            } catch (e: Exception) { e.printStackTrace() }
        }

        // 3. Engine oil (In 10000 km or 6 months)
        cal.time = Date()
        cal.add(Calendar.MONTH, 6)
        repository.insertReminder(
            VehicleReminder(
                vehicleId = vehicleId,
                title = "엔진오일 상태 점검/교체 주기",
                dueDate = sdf.format(cal.time),
                dueMileage = mileage + 10000,
                isDone = false
            )
        )

        // 4. Tire rotation/inspection (In 20000 km)
        cal.add(Calendar.MONTH, 6)
        repository.insertReminder(
            VehicleReminder(
                vehicleId = vehicleId,
                title = "타이어 마모도 확인 및 위치 교환",
                dueDate = sdf.format(cal.time),
                dueMileage = mileage + 20000,
                isDone = false
            )
        )
    }


    // --- CSV Data Export Support ---
    fun exportDataToCSV(context: Context) {
        viewModelScope.launch {
            val vehicles = allVehicles.value
            val maintenance = allMaintenanceRecords.value
            val fuel = allFuelRecords.value

            if (vehicles.isEmpty()) {
                Toast.makeText(context, "내보낼 데이터가 없습니다. 차량을 먼저 등록해주세요.", Toast.LENGTH_SHORT).show()
                return@launch
            }

            val csvContent = StringBuilder()

            // 1. Vehicles
            csvContent.append("=== 차량 목록 ===\n")
            csvContent.append("ID,차량명,등록번호,제조사,모델명,연식,현재주행거리(km),보험만기일,검사만기일\n")
            for (v in vehicles) {
                csvContent.append("${v.id},${v.name},${v.plateNumber},${v.manufacturer},${v.model},${v.year},${v.currentMileage},${v.insuranceExpireDate},${v.inspectionExpireDate}\n")
            }
            csvContent.append("\n\n")

            // 2. Maintenance
            csvContent.append("=== 정비 기록 목록 ===\n")
            csvContent.append("ID,차량ID,정비종류,정비일자,수행시주행거리(km),비용(원),다음교체기한,다음교체주행거리(km),메모\n")
            for (m in maintenance) {
                val memoClean = m.memo.replace(",", " ").replace("\n", " ")
                csvContent.append("${m.id},${m.vehicleId},${m.type},${m.date},${m.mileage},${m.cost},${m.nextDueDate},${m.nextDueMileage},${memoClean}\n")
            }
            csvContent.append("\n\n")

            // 3. Fuel
            csvContent.append("=== 주유 기록 목록 ===\n")
            csvContent.append("ID,차량ID,주유일자,주유소금액(원),리터(L),주행거리(km),메모\n")
            for (f in fuel) {
                val memoClean = f.memo.replace(",", " ").replace("\n", " ")
                csvContent.append("${f.id},${f.vehicleId},${f.date},${f.amount},${f.liters},${f.mileage},${memoClean}\n")
            }

            try {
                val fileName = "Family_Car_Record_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}.csv"
                val file = File(context.cacheDir, fileName)
                file.writeText(csvContent.toString(), charset("EUC-KR")) // Excel friendly encoding

                val authority = "${context.packageName}.fileprovider"
                val uri: Uri = FileProvider.getUriForFile(context, authority, file)

                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/csv"
                    putExtra(Intent.EXTRA_SUBJECT, "가족 차량 관리 데이터 내보내기")
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }

                val chooser = Intent.createChooser(intent, "CSV 파일 내보내기")
                chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(chooser)
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "CSV 내보내기 실패: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }


    // --- COMPANION FACTORY FOR VM ---
    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val application = checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY])
                val database = VehicleDatabase.getDatabase(application)
                val repository = VehicleRepository(database.vehicleDao())
                return VehicleViewModel(application, repository) as T
            }
        }
    }
}
