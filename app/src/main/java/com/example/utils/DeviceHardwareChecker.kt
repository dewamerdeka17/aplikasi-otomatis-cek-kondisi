package com.example.utils

import android.app.ActivityManager
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import java.io.File

data class SoftwareDetails(
    val brand: String,
    val model: String,
    val androidVersion: String,
    val sdkVersion: Int,
    val securityPatch: String,
    val totalStorage: Long,      // in Bytes
    val availableStorage: Long,  // in Bytes
    val totalRam: Long,          // in Bytes
    val batteryLevel: Int,       // 0 - 100
    val batteryTemp: Double,     // in Celsius
    val chargingStatus: String,
    val networkStatus: String,
    val wifiStatus: String,
    val bluetoothStatus: String
)

object DeviceHardwareChecker {

    fun getSoftwareDetails(context: Context): SoftwareDetails {
        val brand = Build.BRAND ?: "Tidak tersedia"
        val model = Build.MODEL ?: "Tidak tersedia"
        val androidVersion = Build.VERSION.RELEASE ?: "Tidak tersedia"
        val sdkVersion = Build.VERSION.SDK_INT
        val securityPatch = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Build.VERSION.SECURITY_PATCH ?: "Tidak tersedia"
        } else {
            "Tidak tersedia (API < 23)"
        }

        // Storage calculations
        var totalStorage: Long = 0
        var availableStorage: Long = 0
        try {
            val path = Environment.getDataDirectory()
            val stat = StatFs(path.path)
            val blockSize = stat.blockSizeLong
            val totalBlocks = stat.blockCountLong
            val availableBlocks = stat.availableBlocksLong
            totalStorage = totalBlocks * blockSize
            availableStorage = availableBlocks * blockSize
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // RAM Calculation
        var totalRam: Long = 0
        try {
            val actManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
            val memInfo = ActivityManager.MemoryInfo()
            actManager?.getMemoryInfo(memInfo)
            totalRam = memInfo.totalMem
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Battery level, temp and charging status
        var batteryLevel = -1
        var batteryTemp = 0.0
        var chargingStatus = "Tidak diketahui"
        try {
            val intentFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            val batteryStatus = context.registerReceiver(null, intentFilter)
            if (batteryStatus != null) {
                val level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                val scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                if (level != -1 && scale != -1) {
                    batteryLevel = (level.toFloat() / scale.toFloat() * 100).toInt()
                }

                val temp = batteryStatus.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0)
                batteryTemp = temp / 10.0

                val status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
                chargingStatus = when (status) {
                    BatteryManager.BATTERY_STATUS_CHARGING -> "Mengisi Daya"
                    BatteryManager.BATTERY_STATUS_DISCHARGING -> "Melepas Daya"
                    BatteryManager.BATTERY_STATUS_FULL -> "Penuh"
                    BatteryManager.BATTERY_STATUS_NOT_CHARGING -> "Tidak Mengisi Daya"
                    else -> "Tidak diketahui"
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Network Status
        var networkStatus = "Terputus"
        try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            if (cm != null) {
                val activeNetwork = cm.activeNetwork
                val capabilities = cm.getNetworkCapabilities(activeNetwork)
                if (capabilities != null) {
                    when {
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> networkStatus = "Wi-Fi Terkoneksi"
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> networkStatus = "Seluler Terkoneksi"
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> networkStatus = "Ethernet Terkoneksi"
                        else -> networkStatus = "Terhubung"
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Wi-Fi Status
        var wifiStatus = "Mati"
        try {
            val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
            if (wifiManager != null) {
                wifiStatus = if (wifiManager.isWifiEnabled) "Aktif" else "Mati"
            }
        } catch (e: Exception) {
            wifiStatus = "Izin/Fitur Tidak Tersedia"
        }

        // Bluetooth Status
        var bluetoothStatus = "Mati"
        try {
            val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
            val bluetoothAdapter = bluetoothManager?.adapter
            if (bluetoothAdapter != null) {
                bluetoothStatus = if (bluetoothAdapter.isEnabled) "Aktif" else "Mati"
            } else {
                bluetoothStatus = "Tidak Didukung"
            }
        } catch (e: Exception) {
            bluetoothStatus = "Tidak Tersedia"
        }

        return SoftwareDetails(
            brand = brand,
            model = model,
            androidVersion = androidVersion,
            sdkVersion = sdkVersion,
            securityPatch = securityPatch,
            totalStorage = totalStorage,
            availableStorage = availableStorage,
            totalRam = totalRam,
            batteryLevel = batteryLevel,
            batteryTemp = batteryTemp,
            chargingStatus = chargingStatus,
            networkStatus = networkStatus,
            wifiStatus = wifiStatus,
            bluetoothStatus = bluetoothStatus
        )
    }

    // Safe trigger for vibration
    fun triggerVibration(context: Context) {
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }

            if (vibrator != null && vibrator.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(500)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
