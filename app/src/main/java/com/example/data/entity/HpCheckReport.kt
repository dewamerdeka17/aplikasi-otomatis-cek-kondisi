package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "hp_check_reports")
data class HpCheckReport(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val merk: String,
    val tipe: String,
    val ram: String,
    val storage: String,
    val warna: String,
    val imei: String,
    val hardwareSerial: String = "SN-AUTO-GEN",
    val hargaBeli: Double,
    val hargaJualEstimasi: Double,
    val catatanFisik: String,
    val fotoDepan: String,
    val fotoBelakang: String,
    val fotoSamping: String,
    
    // Auto Check Software
    val deviceBrand: String,
    val deviceModel: String,
    val androidVersion: String,
    val sdkVersion: Int,
    val securityPatch: String,
    val totalStorage: Long,
    val availableStorage: Long,
    val totalRam: Long,
    val batteryLevel: Int,
    val batteryTemp: Double,
    val chargingStatus: String,
    val networkStatus: String,
    val wifiStatus: String,
    val bluetoothStatus: String,
    
    // Hardware Test Status (Boolean - true if OK, false if not OK)
    val isLayarOk: Boolean,
    val isSpeakerOk: Boolean,
    val isMicOk: Boolean,
    val isKameraOk: Boolean,
    val isSensorOk: Boolean,
    val isGetarOk: Boolean,
    val isGpsOk: Boolean,
    val isConnectivityOk: Boolean,
    
    // Scoring & Recommendations
    val skorKesehatan: Int,
    val statusRekomendasi: String,
    val catatanTeknisi: String,
    
    // Spareparts Check Status (New Columns V2)
    val sparepartLayarSeri: String = "SMC-OLED-M11-DISP",
    val isLayarGanti: Boolean = false,
    val sparepartBateraiSeri: String = "ATL-4200MAH-BATT6",
    val isBateraiGanti: Boolean = false,
    val sparepartKameraSeri: String = "SONY-IMX890-WIDE",
    val isKameraGanti: Boolean = false,
    val sparepartMesinSeri: String = "SEC-EXYNOS2400-SOC",
    val isMesinGanti: Boolean = false,
    val sparepartPortSeri: String = "USB-C-3.2-GEN2",
    val isPortGanti: Boolean = false,
    
    val timestamp: Long = System.currentTimeMillis()
) : Serializable
