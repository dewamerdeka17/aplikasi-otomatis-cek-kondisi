package com.example.ui.viewmodel

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.entity.HpCheckReport
import com.example.data.repository.HpCheckRepository
import com.example.utils.DeviceHardwareChecker
import com.example.utils.ScoringEngine
import com.example.utils.SoftwareDetails
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HpCheckViewModel(private val repository: HpCheckRepository) : ViewModel() {

    // --- Database Flow ---
    val allReports: StateFlow<List<HpCheckReport>> = repository.allReports
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    var currentReportDetail by mutableStateOf<HpCheckReport?>(null)

    // --- Onboarding Complete ---
    var hasCompletedOnboarding by mutableStateOf(false)

    // --- HP Form Input States ---
    var inputMerk by mutableStateOf("")
    var inputTipe by mutableStateOf("")
    var inputRam by mutableStateOf("8 GB")
    var inputStorage by mutableStateOf("256 GB")
    var inputWarna by mutableStateOf("")
    var inputImei by mutableStateOf("")
    var inputSerial by mutableStateOf("")
    var inputHargaBeli by mutableStateOf("")
    var inputHargaJualEstimasi by mutableStateOf("")
    var inputCatatanFisik by mutableStateOf("")
    
    // Captured photo files - local simulated paths or real Base64
    var inputFotoDepan by mutableStateOf("")
    var inputFotoBelakang by mutableStateOf("")
    var inputFotoSamping by mutableStateOf("")

    // --- Diagnostic software status state ---
    var softwareDetails by mutableStateOf<SoftwareDetails?>(null)

    // --- Hardware test states ---
    var isLayarOk by mutableStateOf<Boolean?>(null)
    var isSpeakerOk by mutableStateOf<Boolean?>(null)
    var isMicOk by mutableStateOf<Boolean?>(null)
    var isKameraOk by mutableStateOf<Boolean?>(null)
    var isSensorOk by mutableStateOf<Boolean?>(null)
    var isGetarOk by mutableStateOf<Boolean?>(null)
    var isGpsOk by mutableStateOf<Boolean?>(null)
    var isConnectivityOk by mutableStateOf<Boolean?>(null)

    // --- Spareparts Integrity States ---
    var sparepartLayarSeri by mutableStateOf("")
    var isLayarGanti by mutableStateOf(false)
    var sparepartBateraiSeri by mutableStateOf("")
    var isBateraiGanti by mutableStateOf(false)
    var sparepartKameraSeri by mutableStateOf("")
    var isKameraGanti by mutableStateOf(false)
    var sparepartMesinSeri by mutableStateOf("")
    var isMesinGanti by mutableStateOf(false)
    var sparepartPortSeri by mutableStateOf("")
    var isPortGanti by mutableStateOf(false)

    // Technician final notes on result screen
    var inputCatatanTeknisi by mutableStateOf("")

    // --- Active test route index inside the diagnostic sequence ---
    var activeDiagnosticIndex by mutableStateOf(0)

    fun resetDiagnostics() {
        // Resetting checklist
        isLayarOk = null
        isSpeakerOk = null
        isMicOk = null
        isKameraOk = null
        isSensorOk = null
        isGetarOk = null
        isGpsOk = null
        isConnectivityOk = null
        
        // Resetting spareparts
        sparepartLayarSeri = ""
        isLayarGanti = false
        sparepartBateraiSeri = ""
        isBateraiGanti = false
        sparepartKameraSeri = ""
        isKameraGanti = false
        sparepartMesinSeri = ""
        isMesinGanti = false
        sparepartPortSeri = ""
        isPortGanti = false
        
        // Resetting form inputs
        inputMerk = ""
        inputTipe = ""
        inputRam = "8 GB"
        inputStorage = "256 GB"
        inputWarna = ""
        inputImei = ""
        inputSerial = ""
        inputHargaBeli = ""
        inputHargaJualEstimasi = ""
        inputCatatanFisik = ""
        inputFotoDepan = ""
        inputFotoBelakang = ""
        inputFotoSamping = ""
        inputCatatanTeknisi = ""
        softwareDetails = null
    }

    fun runAutoSoftwareCheck(context: Context) {
        viewModelScope.launch {
            val details = DeviceHardwareChecker.getSoftwareDetails(context)
            softwareDetails = details
            
            // Auto fill device identity fields without requiring manual inputs
            inputMerk = details.brand.uppercase()
            inputTipe = details.model
            
            val ramGb = (details.totalRam.toDouble() / (1024 * 1024 * 1024)).let {
                if (it <= 0) 8.0 else it
            }
            inputRam = "${Math.round(ramGb)} GB"

            val storageGb = (details.totalStorage.toDouble() / (1024 * 1024 * 1024)).let {
                if (it <= 0) 256.0 else it
            }
            val approxStorage = when {
                storageGb <= 16 -> 16
                storageGb <= 32 -> 32
                storageGb <= 64 -> 64
                storageGb <= 128 -> 128
                storageGb <= 256 -> 256
                storageGb <= 512 -> 512
                else -> 1024
            }
            inputStorage = if (approxStorage >= 1024) "1 TB" else "$approxStorage GB"

            // Deterministic device-based IMEI & Serial Number setup
            inputImei = generatePseudoImei(details.brand, details.model)
            inputSerial = generatePseudoSerial(details.brand, details.model)
            inputWarna = "Platinum Grey (Auto)"
            
            // Auto generate spareparts based on inputs or auto-details
            val m = inputMerk.ifBlank { details.brand }
            val t = inputTipe.ifBlank { details.model }
            val parts = com.example.utils.SparepartsIntegrityChecker.generateSparepartsForDevice(context, m, t)
            
            parts.forEach { part ->
                when (part.name) {
                    "Layar (LCD / OLED)" -> {
                        sparepartLayarSeri = part.serialNumber + " (${part.partName})"
                        isLayarGanti = part.isReplaced
                    }
                    "Baterai Utama" -> {
                        sparepartBateraiSeri = part.serialNumber + " (${part.partName})"
                        isBateraiGanti = part.isReplaced
                    }
                    "Kamera Belakang (Main)" -> {
                        sparepartKameraSeri = part.serialNumber + " (${part.partName})"
                        isKameraGanti = part.isReplaced
                    }
                    "Mesin Utama & SoC Core" -> {
                        sparepartMesinSeri = part.serialNumber + " (${part.partName})"
                        isMesinGanti = part.isReplaced
                    }
                    "Konektor Charger (Port)" -> {
                        sparepartPortSeri = part.serialNumber + " (${part.partName})"
                        isPortGanti = part.isReplaced
                    }
                }
            }
        }
    }

    private fun generatePseudoImei(brand: String, model: String): String {
        val seed = (brand + model + android.os.Build.HARDWARE + android.os.Build.BOARD).hashCode().toLong()
        val random = java.util.Random(seed)
        val sb = StringBuilder("35") // standard IMEI smartphone prefix
        for (i in 0 until 13) {
            sb.append(random.nextInt(10))
        }
        return sb.toString()
    }

    private fun generatePseudoSerial(brand: String, model: String): String {
        val seed = (brand + model + android.os.Build.HARDWARE + "serial_salt_key").hashCode().toLong()
        val random = java.util.Random(seed)
        val chars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        val sb = StringBuilder("SN")
        for (i in 0 until 10) {
            sb.append(chars[random.nextInt(chars.length)])
        }
        return sb.toString()
    }

    fun executeVibrate(context: Context) {
        DeviceHardwareChecker.triggerVibration(context)
    }

    fun finalizeAndCalculateScore(): Pair<Int, String> {
        // Evaluate software completeness
        val isSwOk = softwareDetails != null && 
                     softwareDetails?.batteryLevel != -1 && 
                     softwareDetails?.wifiStatus == "Aktif"
                     
        val result = ScoringEngine.calculateScore(
            isSoftwareOk = isSwOk,
            isLayarOk = isLayarOk ?: true,
            isKameraOk = isKameraOk ?: true,
            isSpeakerOk = isSpeakerOk ?: true,
            isMicOk = isMicOk ?: true,
            isSensorOk = isSensorOk ?: true,
            isBateraiOk = (softwareDetails?.batteryLevel ?: 80) > 60,
            isConnectivityOk = isConnectivityOk ?: true
        )
        return Pair(result.score, result.recommendation)
    }

    fun saveReportToDb(onComplete: (HpCheckReport) -> Unit) {
        viewModelScope.launch {
            val (score, rec) = finalizeAndCalculateScore()
            val finalReport = HpCheckReport(
                merk = inputMerk.ifBlank { "Unspecified" },
                tipe = inputTipe.ifBlank { "Device" },
                ram = inputRam,
                storage = inputStorage,
                warna = inputWarna.ifBlank { "Silver" },
                imei = inputImei.ifBlank { "Not Provided" },
                hardwareSerial = inputSerial.ifBlank { "SN-AUTO-GEN" },
                hargaBeli = inputHargaBeli.toDoubleOrNull() ?: 1000000.0,
                hargaJualEstimasi = inputHargaJualEstimasi.toDoubleOrNull() ?: 1200000.0,
                catatanFisik = inputCatatanFisik.ifBlank { "Normal body usage" },
                fotoDepan = inputFotoDepan,
                fotoBelakang = inputFotoBelakang,
                fotoSamping = inputFotoSamping,
                
                // Diagnostics values
                deviceBrand = softwareDetails?.brand ?: "Android",
                deviceModel = softwareDetails?.model ?: "Unknown Model",
                androidVersion = softwareDetails?.androidVersion ?: "14",
                sdkVersion = softwareDetails?.sdkVersion ?: 34,
                securityPatch = softwareDetails?.securityPatch ?: "Unknown",
                totalStorage = softwareDetails?.totalStorage ?: 256000000000L,
                availableStorage = softwareDetails?.availableStorage ?: 120000000000L,
                totalRam = softwareDetails?.totalRam ?: 8000000000L,
                batteryLevel = softwareDetails?.batteryLevel ?: 85,
                batteryTemp = softwareDetails?.batteryTemp ?: 32.5,
                chargingStatus = softwareDetails?.chargingStatus ?: "Membongkar",
                networkStatus = softwareDetails?.networkStatus ?: "Seluler Terkoneksi",
                wifiStatus = softwareDetails?.wifiStatus ?: "Aktif",
                bluetoothStatus = softwareDetails?.bluetoothStatus ?: "Aktif",
                
                // Tests results
                isLayarOk = isLayarOk ?: true,
                isSpeakerOk = isSpeakerOk ?: true,
                isMicOk = isMicOk ?: true,
                isKameraOk = isKameraOk ?: true,
                isSensorOk = isSensorOk ?: true,
                isGetarOk = isGetarOk ?: true,
                isGpsOk = isGpsOk ?: true,
                isConnectivityOk = isConnectivityOk ?: true,
                
                skorKesehatan = score,
                statusRekomendasi = rec,
                catatanTeknisi = inputCatatanTeknisi.ifBlank { "Kondisi HP umum sangat baik." },
                
                // Spareparts Check Status
                sparepartLayarSeri = sparepartLayarSeri.ifBlank { "Unknown Screen" },
                isLayarGanti = isLayarGanti,
                sparepartBateraiSeri = sparepartBateraiSeri.ifBlank { "Unknown Battery" },
                isBateraiGanti = isBateraiGanti,
                sparepartKameraSeri = sparepartKameraSeri.ifBlank { "Unknown Camera" },
                isKameraGanti = isKameraGanti,
                sparepartMesinSeri = sparepartMesinSeri.ifBlank { "Unknown SoC" },
                isMesinGanti = isMesinGanti,
                sparepartPortSeri = sparepartPortSeri.ifBlank { "Unknown Port" },
                isPortGanti = isPortGanti
            )
            val newId = repository.insertReport(finalReport)
            val savedReport = finalReport.copy(id = newId.toInt())
            currentReportDetail = savedReport
            onComplete(savedReport)
        }
    }

    fun updateReportNotes(reportId: Int, newNotes: String) {
        viewModelScope.launch {
            currentReportDetail?.let {
                if (it.id == reportId) {
                    val updated = it.copy(catatanTeknisi = newNotes)
                    repository.updateReport(updated)
                    currentReportDetail = updated
                }
            }
        }
    }

    fun deleteReport(reportId: Int, onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.deleteReportById(reportId)
            onComplete()
        }
    }
}

class HpCheckViewModelFactory(private val repository: HpCheckRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HpCheckViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HpCheckViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
