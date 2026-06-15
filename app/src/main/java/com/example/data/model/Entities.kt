package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "vehicles")
data class Vehicle(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val plateNumber: String,
    val manufacturer: String,
    val model: String,
    val year: Int,
    val currentMileage: Int,
    val insuranceExpireDate: String, // YYYY-MM-DD
    val inspectionExpireDate: String, // YYYY-MM-DD
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "maintenance_records")
data class MaintenanceRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val vehicleId: Long,
    val type: String, // "엔진오일", "타이어", "배터리", "브레이크", "와이퍼", "에어컨 필터", "기타 정비"
    val date: String, // YYYY-MM-DD
    val mileage: Int,
    val cost: Long,
    val memo: String,
    val nextDueDate: String, // YYYY-MM-DD
    val nextDueMileage: Int
)

@Entity(tableName = "fuel_records")
data class FuelRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val vehicleId: Long,
    val date: String, // YYYY-MM-DD
    val amount: Long,
    val liters: Double,
    val mileage: Int,
    val memo: String
)

@Entity(tableName = "vehicle_reminders")
data class VehicleReminder(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val vehicleId: Long,
    val title: String,
    val dueDate: String, // YYYY-MM-DD
    val dueMileage: Int,
    val isDone: Boolean = false
)
