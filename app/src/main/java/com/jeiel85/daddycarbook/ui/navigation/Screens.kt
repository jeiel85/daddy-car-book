package com.jeiel85.daddycarbook.ui.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object VehicleForm : Screen("vehicle_form?vehicleId={vehicleId}") {
        fun createRoute(vehicleId: Long? = null): String {
            return if (vehicleId != null) "vehicle_form?vehicleId=$vehicleId" else "vehicle_form"
        }
    }
    object MaintenanceForm : Screen("maintenance_form?vehicleId={vehicleId}") {
        fun createRoute(vehicleId: Long): String {
            return "maintenance_form?vehicleId=$vehicleId"
        }
    }
    object FuelForm : Screen("fuel_form?vehicleId={vehicleId}") {
        fun createRoute(vehicleId: Long): String {
            return "fuel_form?vehicleId=$vehicleId"
        }
    }
    object Reminders : Screen("reminders")
    object Statistics : Screen("statistics")
    object Settings : Screen("settings")
}
