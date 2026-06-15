package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.VehicleDao
import com.example.data.model.FuelRecord
import com.example.data.model.MaintenanceRecord
import com.example.data.model.Vehicle
import com.example.data.model.VehicleReminder

@Database(
    entities = [
        Vehicle::class,
        MaintenanceRecord::class,
        FuelRecord::class,
        VehicleReminder::class
    ],
    version = 1,
    exportSchema = false
)
abstract class VehicleDatabase : RoomDatabase() {

    abstract fun vehicleDao(): VehicleDao

    companion object {
        @Volatile
        private var INSTANCE: VehicleDatabase? = null

        fun getDatabase(context: Context): VehicleDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    VehicleDatabase::class.java,
                    "vehicle_management_db"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
