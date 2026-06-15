package com.jeiel85.daddycarbook.data.dao

import androidx.room.*
import com.jeiel85.daddycarbook.data.model.FuelRecord
import com.jeiel85.daddycarbook.data.model.MaintenanceRecord
import com.jeiel85.daddycarbook.data.model.Vehicle
import com.jeiel85.daddycarbook.data.model.VehicleReminder
import kotlinx.coroutines.flow.Flow

@Dao
interface VehicleDao {

    // --- Vehicle Queries ---
    @Query("SELECT * FROM vehicles ORDER BY id DESC")
    fun getAllVehicles(): Flow<List<Vehicle>>

    @Query("SELECT * FROM vehicles WHERE id = :vehicleId LIMIT 1")
    suspend fun getVehicleById(vehicleId: Long): Vehicle?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVehicle(vehicle: Vehicle): Long

    @Update
    suspend fun updateVehicle(vehicle: Vehicle)

    @Delete
    suspend fun deleteVehicle(vehicle: Vehicle)


    // --- Maintenance Queries ---
    @Query("SELECT * FROM maintenance_records ORDER BY date DESC")
    fun getAllMaintenanceRecords(): Flow<List<MaintenanceRecord>>

    @Query("SELECT * FROM maintenance_records WHERE vehicleId = :vehicleId ORDER BY date DESC")
    fun getMaintenanceRecordsForVehicle(vehicleId: Long): Flow<List<MaintenanceRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMaintenanceRecord(record: MaintenanceRecord): Long

    @Delete
    suspend fun deleteMaintenanceRecord(record: MaintenanceRecord)


    // --- Fuel Queries ---
    @Query("SELECT * FROM fuel_records ORDER BY date DESC")
    fun getAllFuelRecords(): Flow<List<FuelRecord>>

    @Query("SELECT * FROM fuel_records WHERE vehicleId = :vehicleId ORDER BY date DESC")
    fun getFuelRecordsForVehicle(vehicleId: Long): Flow<List<FuelRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFuelRecord(record: FuelRecord): Long

    @Delete
    suspend fun deleteFuelRecord(record: FuelRecord)


    // --- Reminder Queries ---
    @Query("SELECT * FROM vehicle_reminders ORDER BY dueDate ASC")
    fun getAllReminders(): Flow<List<VehicleReminder>>

    @Query("SELECT * FROM vehicle_reminders WHERE vehicleId = :vehicleId ORDER BY dueDate ASC")
    fun getRemindersForVehicle(vehicleId: Long): Flow<List<VehicleReminder>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: VehicleReminder): Long

    @Update
    suspend fun updateReminder(reminder: VehicleReminder)

    @Delete
    suspend fun deleteReminder(reminder: VehicleReminder)
}
