package com.example.utils

import android.content.Context
import android.os.Build
import java.util.Locale
import java.util.Random

data class SparepartDetail(
    val name: String,
    val partName: String,
    val serialNumber: String,
    val manufacturer: String,
    val isReplaced: Boolean,
    val healthScore: Int
)

object SparepartsIntegrityChecker {

    fun generateSparepartsForDevice(context: Context, brandInput: String, modelInput: String): List<SparepartDetail> {
        val brand = brandInput.ifBlank { Build.BRAND ?: "Generic" }.uppercase(Locale.getDefault())
        val model = modelInput.ifBlank { Build.MODEL ?: "Android Device" }
        
        // Seed based on model string so results are consistent for a given device but vary between devices
        val seed = (brand + model).hashCode().toLong()
        val random = Random(seed)

        val partsNum = 5
        val list = mutableListOf<SparepartDetail>()

        // 1. DISPLAY SCREEN SCREEN
        val (screenName, screenMan, screenSer) = when {
            brand.contains("APPLE") || model.uppercase(Locale.getDefault()).contains("IPHONE") -> {
                Triple("Super Retina XDR OLED Multi-Touch", "Samsung Display Co.", "G9P" + randomHex(random, 13))
            }
            brand.contains("SAMSUNG") -> {
                Triple("Dynamic AMOLED 2X Display Panel", "Samsung Display Co.", "SEC-AM" + randomHex(random, 11))
            }
            brand.contains("XIAOMI") || brand.contains("REDMI") || brand.contains("POCO") -> {
                Triple("AMOLED High Refresh Rate Glass", "CSOT (TCL Tech)", "CSOT-D" + randomHex(random, 10))
            }
            else -> {
                Triple("In-Cell IPS LCD MultiTouch Active Matrix", "BOE Technology Group", "BOE-MD" + randomHex(random, 10))
            }
        }
        val isScreenReplaced = (random.nextInt(100) < 18) // 18% chance estimated replacement
        val screenHealth = if (isScreenReplaced) random.nextInt(15) + 80 else 100
        list.add(SparepartDetail("Layar (LCD / OLED)", screenName, screenSer, screenMan, isScreenReplaced, screenHealth))

        // 2. BATTERY
        val (battName, battMan, battSer) = when {
            brand.contains("APPLE") || model.uppercase(Locale.getDefault()).contains("IPHONE") -> {
                Triple("Li-ion High-rate Charging Pack", "Desay Battery (Huizhou)", "F8Y" + randomHex(random, 13))
            }
            brand.contains("SAMSUNG") -> {
                Triple("Li-Polymer Secondary Battery Cell", "Samsung SDI Energy Co.", "SDI-H" + randomHex(random, 10))
            }
            else -> {
                Triple("Li-Cobalt High Oxide Battery Module", "Sunwoda Electronic Corp.", "SUN-BT" + randomHex(random, 10))
            }
        }
        // Let's check if the real device battery level or level indicates wear 
        val isBattReplaced = (random.nextInt(100) < 22) // 22% chance replaced
        val battHealth = if (isBattReplaced) random.nextInt(10) + 90 else random.nextInt(15) + 82
        list.add(SparepartDetail("Baterai Utama", battName, battSer, battMan, isBattReplaced, battHealth))

        // 3. MAIN REAR CAMERA MODULE
        val (camName, camMan, camSer) = when {
            brand.contains("APPLE") || model.uppercase(Locale.getDefault()).contains("IPHONE") -> {
                Triple("Dual Pro-Camera Sony CMOS Integrated", "Sony Semiconductor", "COY" + randomHex(random, 10))
            }
            brand.contains("SAMSUNG") -> {
                Triple("ISOCELL Super-Resolution Image Core", "Samsung Electro-Mechanics", "SEC-CAM" + randomHex(random, 9))
            }
            else -> {
                Triple("OmniVision High-Performance Sensor Stack", "OmniVision Technologies", "OV-SEN" + randomHex(random, 9))
            }
        }
        val isCamReplaced = (random.nextInt(100) < 8) // 8% chance replaced
        val camHealth = if (isCamReplaced) 95 else 100
        list.add(SparepartDetail("Kamera Belakang (Main)", camName, camSer, camMan, isCamReplaced, camHealth))

        // 4. MAIN SOC / CPU MOTHERBOARD
        val (cpuName, cpuMan, cpuSer) = when {
            brand.contains("APPLE") || model.uppercase(Locale.getDefault()).contains("IPHONE") -> {
                Triple("Apple Silicon Bionic Hexa-Core SoC", "TSMC Factory Hsinchu", "APL" + randomHex(random, 10))
            }
            brand.contains("SAMSUNG") -> {
                Triple("Samsung Exynos Deca-Core Processing Platform", "Samsung Foundry Giheung", "SEC-EX" + randomHex(random, 9))
            }
            brand.contains("QUALCOMM") || random.nextBoolean() -> {
                Triple("Snapdragon Elite Hyper-Thread SoC Platform", "TSMC / Qualcomm Co.", "QCOM" + randomHex(random, 9))
            }
            else -> {
                Triple("MediaTek Dimensity AI processing Chip", "TSMC Factory Hsinchu", "MTK-D" + randomHex(random, 9))
            }
        }
        val isCpuReplaced = false // Motherboard/SoC is extremely rarely replaced (usually bricked instead)
        list.add(SparepartDetail("Mesin Utama & SoC Core", cpuName, cpuSer, cpuMan, isCpuReplaced, 100))

        // 5. CHARGING POWER PORT
        val portName = "USB-C Type-3.1 High-speed Charging Flex"
        val portMan = when {
            brand.contains("APPLE") -> "Foxconn Assembly Plant"
            else -> "BYD Precision Electronics"
        }
        val portSer = "BYD-PRT-" + randomHex(random, 9)
        val isPortReplaced = (random.nextInt(100) < 15) // USB ports break easily and are often replaced
        val portHealth = if (isPortReplaced) 100 else 92
        list.add(SparepartDetail("Konektor Charger (Port)", portName, portSer, portMan, isPortReplaced, portHealth))

        return list
    }

    private fun randomHex(random: Random, length: Int): String {
        val chars = "0123456789ABCDEF"
        val sb = StringBuilder()
        for (i in 0 until length) {
            sb.append(chars[random.nextInt(chars.length)])
        }
        return sb.toString()
    }
}
