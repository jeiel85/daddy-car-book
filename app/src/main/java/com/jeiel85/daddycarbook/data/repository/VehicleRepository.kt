package com.jeiel85.daddycarbook.data.repository

import com.jeiel85.daddycarbook.data.dao.VehicleDao
import com.jeiel85.daddycarbook.data.model.FuelRecord
import com.jeiel85.daddycarbook.data.model.MaintenanceRecord
import com.jeiel85.daddycarbook.data.model.Vehicle
import com.jeiel85.daddycarbook.data.model.VehicleReminder
import kotlinx.coroutines.flow.Flow

class VehicleRepository(private val vehicleDao: VehicleDao) {

    val allVehicles: Flow<List<Vehicle>> = vehicleDao.getAllVehicles()
    val allMaintenanceRecords: Flow<List<MaintenanceRecord>> = vehicleDao.getAllMaintenanceRecords()
    val allFuelRecords: Flow<List<FuelRecord>> = vehicleDao.getAllFuelRecords()
    val allReminders: Flow<List<VehicleReminder>> = vehicleDao.getAllReminders()

    suspend fun getVehicleById(id: Long): Vehicle? {
        return vehicleDao.getVehicleById(id)
    }

    suspend fun insertVehicle(vehicle: Vehicle): Long {
        return vehicleDao.insertVehicle(vehicle)
    }

    suspend fun updateVehicle(vehicle: Vehicle) {
        vehicleDao.updateVehicle(vehicle)
    }

    suspend fun deleteVehicle(vehicle: Vehicle) {
        vehicleDao.deleteVehicle(vehicle)
    }

    fun getMaintenanceRecordsForVehicle(vehicleId: Long): Flow<List<MaintenanceRecord>> {
        return vehicleDao.getMaintenanceRecordsForVehicle(vehicleId)
    }

    suspend fun insertMaintenanceRecord(record: MaintenanceRecord): Long {
        return vehicleDao.insertMaintenanceRecord(record)
    }

    suspend fun deleteMaintenanceRecord(record: MaintenanceRecord) {
        vehicleDao.deleteMaintenanceRecord(record)
    }

    fun getFuelRecordsForVehicle(vehicleId: Long): Flow<List<FuelRecord>> {
        return vehicleDao.getFuelRecordsForVehicle(vehicleId)
    }

    suspend fun insertFuelRecord(record: FuelRecord): Long {
        return vehicleDao.insertFuelRecord(record)
    }

    suspend fun deleteFuelRecord(record: FuelRecord) {
        vehicleDao.deleteFuelRecord(record)
    }

    fun getRemindersForVehicle(vehicleId: Long): Flow<List<VehicleReminder>> {
        return vehicleDao.getRemindersForVehicle(vehicleId)
    }

    suspend fun insertReminder(reminder: VehicleReminder): Long {
        return vehicleDao.insertReminder(reminder)
    }

    suspend fun updateReminder(reminder: VehicleReminder) {
        vehicleDao.updateReminder(reminder)
    }

    suspend fun deleteReminder(reminder: VehicleReminder) {
        vehicleDao.deleteReminder(reminder)
    }
}
