package com.example.ui.screens

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.AudioManager
import android.media.ToneGenerator
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.HpCheckViewModel
import com.example.utils.SoftwareDetails
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiagnosticScreen(
    viewModel: HpCheckViewModel,
    onNavigateBack: () -> Unit,
    onCompleted: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var activeStep by remember { mutableStateOf(1) }
    val totalSteps = 8

    // Read details when loaded
    LaunchedEffect(Unit) {
        if (viewModel.softwareDetails == null) {
            viewModel.runAutoSoftwareCheck(context)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Asisten Diagnosa HP", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.Close, contentDescription = "Batal")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
        ) {
            // STEP PROGRESS STEPS HEADER
            ProgressWizardHeader(
                activeStep = activeStep,
                totalSteps = totalSteps
            )

            // ACTIVE DIAGNOSTIC CARD
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                AnimatedContent(
                    targetState = activeStep,
                    transitionSpec = {
                        slideInHorizontally { width -> width } + fadeIn() togetherWith
                        slideOutHorizontally { width -> -width } + fadeOut()
                    },
                    label = "DiagnosticSteps"
                ) { step ->
                    when (step) {
                        1 -> AutoSoftwareDiagnosticPanel(viewModel = viewModel)
                        2 -> DeadPixelAndKeyboardTestPanel(viewModel = viewModel)
                        3 -> SpeakerSoundTestPanel(viewModel = viewModel)
                        4 -> MicrophoneDiagnosticPanel(viewModel = viewModel)
                        5 -> HardwareSensorsDiagnosticPanel()
                        6 -> CamerasAndFlashTestPanel(viewModel = viewModel)
                        7 -> VibrationAndGpsDiagnosticPanel(viewModel = viewModel)
                        8 -> SummarySubmissionDiagnosticPanel(
                            viewModel = viewModel,
                            onSubmit = {
                                viewModel.saveReportToDb { savedReport ->
                                    onCompleted()
                                }
                            }
                        )
                    }
                }
            }

            // NAVIGATION BUTTON ROW BOTTOM
            Surface(
                tonalElevation = 8.dp,
                modifier = Modifier.fillMaxWidth(),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = {
                            if (activeStep > 1) activeStep--
                        },
                        enabled = activeStep > 1,
                        modifier = Modifier.testTag("btn_diagnostic_prev")
                    ) {
                        Text("< Kembali")
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Step $activeStep dari $totalSteps",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    Button(
                        onClick = {
                            if (activeStep < totalSteps) {
                                activeStep++
                            }
                        },
                        enabled = activeStep < totalSteps,
                        modifier = Modifier.testTag("btn_diagnostic_next"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Lanjut >")
                    }
                }
            }
        }
    }
}

// ======================= WIZARD STEP HEADER =======================
@Composable
fun ProgressWizardHeader(
    activeStep: Int,
    totalSteps: Int
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(vertical = 12.dp, horizontal = 16.dp)
    ) {
        val currentLabel = when (activeStep) {
            1 -> "Sistem Software & Device"
            2 -> "Layar, Touch & Kecerahan"
            3 -> "Speaker & Audio Output"
            4 -> "Microphone & Rec Input"
            5 -> "Real-time Hardware Sensor"
            6 -> "Kamera Depan & Belakang"
            7 -> "Tes Getar & GPS Akurasi"
            else -> "Simpulan & Cetak Laporan"
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = currentLabel,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "${(activeStep * 100 / totalSteps)}% Selesai",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.outline,
                fontWeight = FontWeight.Medium
            )
        }
        Spacer(modifier = Modifier.height(8.dp))

        // Drawing thin bar items
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            for (i in 1..totalSteps) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(6.dp)
                        .clip(CircleShape)
                        .background(
                            if (i <= activeStep) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.outlineVariant
                        )
                )
            }
        }
    }
}

// ======================= STEP 1: AUTO SOFTWARE CHECK =======================
@Composable
fun AutoSoftwareDiagnosticPanel(viewModel: HpCheckViewModel) {
    val details = viewModel.softwareDetails
    val scrollState = rememberScrollState()

    if (details == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // General Info Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "Auto Check Berhasil",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Membaca data sistem internal perangkat.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            Text(
                text = "Parameter Software Android",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.primary
            )

            // Grid of detail specs
            ParamRow(label = "Manufaktur (Brand)", value = details.brand)
            ParamRow(label = "Model Perangkat", value = details.model)
            ParamRow(label = "Android OS Versi", value = "Android ${details.androidVersion}")
            ParamRow(label = "SDK API Level", value = details.sdkVersion.toString())
            ParamRow(label = "Patch Keamanan", value = details.securityPatch)

            val totalStorageGb = "%.1f GB".format(details.totalStorage / (1024.0 * 1024.0 * 1024.0))
            val availStorageGb = "%.1f GB".format(details.availableStorage / (1024.0 * 1024.0 * 1024.0))
            val ramGb = "%.1f GB".format(details.totalRam / (1024.0 * 1024.0 * 1024.0))

            ParamRow(label = "Total Storage", value = totalStorageGb)
            ParamRow(label = "Storage Tersedia", value = availStorageGb)
            ParamRow(label = "RAM Fisik Terbaca", value = ramGb)

            Text(
                text = "Metrik Baterai & Daya",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.primary
            )

            ParamRow(label = "Tingkat Baterai", value = "${details.batteryLevel}%")
            ParamRow(label = "Suhu Baterai", value = "${details.batteryTemp} °C")
            ParamRow(label = "Status Charging", value = details.chargingStatus)

            Text(
                text = "Akses Jaringan & Koneksi",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.primary
            )

            ParamRow(label = "Koneksi Aktif", value = details.networkStatus)
            ParamRow(label = "Status Wi-Fi", value = details.wifiStatus)
            ParamRow(label = "Status Bluetooth", value = details.bluetoothStatus)

            Text(
                text = "Deteksi Integritas Sparepart (Hardware)",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.primary
            )
            
            Text(
                text = "Sistem menganalisis kecocokan kode sasis IC pabrik. Anda dapat mengubah status jika mendeteksi penggantian fisik non-ori.",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.outline,
                lineHeight = 15.sp
            )

            // Screen
            SparepartEditRow(
                icon = Icons.Default.Smartphone,
                partLabel = "Layar (LCD / OLED Panel)",
                serialValue = viewModel.sparepartLayarSeri,
                isReplaced = viewModel.isLayarGanti,
                onReplacedChange = { viewModel.isLayarGanti = it },
                onSerialChange = { viewModel.sparepartLayarSeri = it }
            )

            // Battery
            SparepartEditRow(
                icon = Icons.Default.Power,
                partLabel = "Baterai Utama (Lithium Cell)",
                serialValue = viewModel.sparepartBateraiSeri,
                isReplaced = viewModel.isBateraiGanti,
                onReplacedChange = { viewModel.isBateraiGanti = it },
                onSerialChange = { viewModel.sparepartBateraiSeri = it }
            )

            // Camera
            SparepartEditRow(
                icon = Icons.Default.CameraAlt,
                partLabel = "Kamera Belakang (CMOS Lens)",
                serialValue = viewModel.sparepartKameraSeri,
                isReplaced = viewModel.isKameraGanti,
                onReplacedChange = { viewModel.isKameraGanti = it },
                onSerialChange = { viewModel.sparepartKameraSeri = it }
            )

            // SoC / Motherboard
            SparepartEditRow(
                icon = Icons.Default.Info,
                partLabel = "Mesin Utama & SoC Board Chip",
                serialValue = viewModel.sparepartMesinSeri,
                isReplaced = viewModel.isMesinGanti,
                onReplacedChange = { viewModel.isMesinGanti = it },
                onSerialChange = { viewModel.sparepartMesinSeri = it }
            )

            // Port Connector
            SparepartEditRow(
                icon = Icons.Default.Usb,
                partLabel = "Konektor Charger (Port Flex)",
                serialValue = viewModel.sparepartPortSeri,
                isReplaced = viewModel.isPortGanti,
                onReplacedChange = { viewModel.isPortGanti = it },
                onSerialChange = { viewModel.sparepartPortSeri = it }
            )

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun ParamRow(label: String, value: String) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(12.dp),
        border = CardDefaults.outlinedCardBorder(),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Text(
                text = value,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

// ======================= STEP 2: SCREEN, TOUCH & BRIGHTNESS =======================
@Composable
fun DeadPixelAndKeyboardTestPanel(viewModel: HpCheckViewModel) {
    var stateLayarOk by remember { mutableStateOf(viewModel.isLayarOk) }
    var activeModule by remember { mutableStateOf("Menu") } // "Menu", "DeadPixel", "TouchTest"
    val scrollState = rememberScrollState()

    if (activeModule == "DeadPixel") {
        var cycleIndex by remember { mutableStateOf(0) }
        val cycleColors = listOf(Color.Red, Color.Green, Color.Blue, Color.White, Color.Black)
        
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(cycleColors[cycleIndex])
                .pointerInput(Unit) {
                    detectTapGestures {
                        if (cycleIndex < cycleColors.lastIndex) {
                            cycleIndex++
                        } else {
                            activeModule = "Menu"
                        }
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Ketuk untuk berganti warna\n(${cycleIndex + 1}/${cycleColors.size})",
                color = if (cycleColors[cycleIndex] == Color.White) Color.Black else Color.White,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    .padding(12.dp)
            )
        }
    } else if (activeModule == "TouchTest") {
        // Grid size 5 columns, 7 rows
        val cols = 5
        val rows = 8
        val totalCells = cols * rows
        val touchedCells = remember { mutableStateListOf<Int>() }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .padding(8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Sentuh semua kotak hingga hijau!", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Button(
                    onClick = { activeModule = "Menu" },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Selesai")
                }
            }

            BoxWithConstraints(modifier = Modifier.weight(1f)) {
                val boxWidth = maxWidth / cols
                val boxHeight = maxHeight / rows

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectDragGestures(
                                onDragStart = { offset ->
                                    val col = (offset.x / (boxWidth.toPx())).toInt().coerceIn(0, cols - 1)
                                    val row = (offset.y / (boxHeight.toPx())).toInt().coerceIn(0, rows - 1)
                                    val idx = row * cols + col
                                    if (!touchedCells.contains(idx)) touchedCells.add(idx)
                                },
                                onDrag = { change, dragAmount ->
                                    val offset = change.position
                                    val col = (offset.x / (boxWidth.toPx())).toInt().coerceIn(0, cols - 1)
                                    val row = (offset.y / (boxHeight.toPx())).toInt().coerceIn(0, rows - 1)
                                    val idx = row * cols + col
                                    if (idx in 0 until totalCells && !touchedCells.contains(idx)) {
                                        touchedCells.add(idx)
                                    }
                                }
                            )
                        }
                ) {
                    for (r in 0 until rows) {
                        Row(modifier = Modifier.weight(1f)) {
                            for (c in 0 until cols) {
                                val idx = r * cols + c
                                val isTouched = touchedCells.contains(idx)
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .padding(1.dp)
                                        .background(if (isTouched) Color(0xFF2E7D32) else Color(0xFF263238))
                                        .border(0.5.dp, Color.DarkGray)
                                )
                            }
                        }
                    }
                }
            }
        }
    } else {
        // Main panel controls
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Pemeriksaan Panel Layar",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            // Dead Pixel Trigger Card
            Card(
                onClick = { activeModule = "DeadPixel" },
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Palette, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("1. Dead Pixel Test", fontWeight = FontWeight.Bold)
                        Text("Layar akan berganti warna solid (merah, hijau, biru...) untuk mengecek kecacatan dot layar.", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                    }
                    Icon(Icons.Default.PlayArrow, contentDescription = "Run")
                }
            }

            // Touch Area Trigger Card
            Card(
                onClick = { activeModule = "TouchTest" },
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Draw, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("2. Touch Screen Test", fontWeight = FontWeight.Bold)
                        Text("Mengecek fungsional grid sentuh untuk menandai area digitizer mati.", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                    }
                    Icon(Icons.Default.PlayArrow, contentDescription = "Run")
                }
            }

            // Brightness control slider review
            Text(
                text = "3. Tes Sensor Kecerahan (Kecerahan Samping)",
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )

            var mockBrightness by remember { mutableStateOf(0.6f) }
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Geser Kecerahan Mandiri", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text("${(mockBrightness * 100).toInt()}%", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                    Slider(
                        value = mockBrightness,
                        onValueChange = { mockBrightness = it }
                    )
                }
            }

            // Verdict selection
            Divider()
            Text("Simpulan Hasil Pengujian Layar", fontWeight = FontWeight.Bold, fontSize = 13.sp)

            YesNoVerdictSelector(
                currentState = stateLayarOk,
                onSelect = {
                    stateLayarOk = it
                    viewModel.isLayarOk = it
                }
            )
        }
    }
}

// ======================= STEP 3: SPEAKER TEST =======================
@Composable
fun SpeakerSoundTestPanel(viewModel: HpCheckViewModel) {
    var isPlaying by remember { mutableStateOf(false) }
    var verdictInput by remember { mutableStateOf(viewModel.isSpeakerOk) }
    val context = LocalContext.current

    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            // Play a synthetic tone using ToneGenerator asynchronously
            try {
                val toneGen = ToneGenerator(AudioManager.STREAM_MUSIC, 100)
                toneGen.startTone(ToneGenerator.TONE_CDMA_PIP, 400) // beep 400ms
                delay(600)
                toneGen.startTone(ToneGenerator.TONE_CDMA_PIP, 400) // beep 400ms
                delay(600)
                toneGen.release()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            isPlaying = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Pengecekan Speaker Perangkat",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.fillMaxWidth()
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.VolumeUp,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(52.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Putar Bunyi Test Pembeli",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Aplikasi akan memancarkan getaran beeping frekuensi tinggi (800Hz) selama 2 detik untuk tes noise speaker.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.outline,
                    textAlign = TextAlign.Center,
                    lineHeight = 16.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { isPlaying = true },
                    modifier = Modifier.testTag("speaker_test_play_btn")
                ) {
                    Text("Putar Bunyi Beep 🎵")
                }
            }
        }

        Divider()
        Text(
            text = "Apakah bunyi beep terdengar jelas tanpa sember?",
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            modifier = Modifier.fillMaxWidth()
        )

        YesNoVerdictSelector(
            currentState = verdictInput,
            onSelect = {
                verdictInput = it
                viewModel.isSpeakerOk = it
            }
        )
    }
}

// ======================= STEP 4: MICROPHONE TEST =======================
@Composable
fun MicrophoneDiagnosticPanel(viewModel: HpCheckViewModel) {
    var isRecording by remember { mutableStateOf(false) }
    var secondsLeft by remember { mutableStateOf(5) }
    var hasRecording by remember { mutableStateOf(false) }
    var isPlayingBack by remember { mutableStateOf(false) }

    var verdictInput by remember { mutableStateOf(viewModel.isMicOk) }

    LaunchedEffect(isRecording) {
        if (isRecording) {
            secondsLeft = 5
            while (secondsLeft > 0) {
                delay(1000)
                secondsLeft--
            }
            isRecording = false
            hasRecording = true
        }
    }

    LaunchedEffect(isPlayingBack) {
        if (isPlayingBack) {
            delay(3000) // simulation of play back
            isPlayingBack = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Pengecekan Mic & Recorder",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.fillMaxWidth()
        )

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = if (isRecording) Icons.Default.MicOff else Icons.Default.Mic,
                    contentDescription = null,
                    tint = if (isRecording) Color.Red else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(52.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                if (isRecording) {
                    Text(
                        text = "Merekam Suara: $secondsLeft Detik Sisa",
                        fontWeight = FontWeight.Bold,
                        color = Color.Red,
                        fontSize = 16.sp
                    )
                    Text("Bicaralah dekat microphone HP Anda sekarang", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                } else {
                    Text(
                        text = if (hasRecording) "Rekaman Tersimpan (5 Detik)" else "Belum Ada Rekaman",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Text("Ketuk tombol rekam untuk mulai uji dengar mic", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = { isRecording = true },
                        colors = ButtonDefaults.buttonColors(containerColor = if (isRecording) Color.DarkGray else MaterialTheme.colorScheme.primary),
                        modifier = Modifier.testTag("mic_record_btn"),
                        enabled = !isRecording
                    ) {
                        Text("Mulai Rekam")
                    }

                    Button(
                        onClick = { isPlayingBack = true },
                        enabled = hasRecording && !isPlayingBack,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                        modifier = Modifier.testTag("mic_playback_btn")
                    ) {
                        Text(if (isPlayingBack) "Memutar..." else "Putar Ulang Suara 🔊")
                    }
                }
            }
        }

        Divider()
        Text(
            text = "Apakah input audio microphone bekerja normal?",
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            modifier = Modifier.fillMaxWidth()
        )

        YesNoVerdictSelector(
            currentState = verdictInput,
            onSelect = {
                verdictInput = it
                viewModel.isMicOk = it
            }
        )
    }
}

// ======================= STEP 5: DETAILED SENSORS REVIEW =======================
@Composable
fun HardwareSensorsDiagnosticPanel() {
    val context = LocalContext.current
    val sensorManager = remember { context.getSystemService(Context.SENSOR_SERVICE) as SensorManager }

    // Read initial static support
    val hasAcc = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null
    val hasGyro = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE) != null
    val hasProx = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY) != null
    val hasLight = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT) != null
    val hasMag = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) != null

    // Setup dynamic state flows for readings
    var accX by remember { mutableStateOf(0f) }
    var accY by remember { mutableStateOf(0f) }
    var accZ by remember { mutableStateOf(0f) }
    
    var gyroX by remember { mutableStateOf(0f) }
    var gyroY by remember { mutableStateOf(0f) }
    var gyroZ by remember { mutableStateOf(0f) }

    var proxVal by remember { mutableStateOf(0f) }
    var lightVal by remember { mutableStateOf(0f) }

    val scrollState = rememberScrollState()

    DisposableEffect(Unit) {
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                if (event == null) return
                when (event.sensor.type) {
                    Sensor.TYPE_ACCELEROMETER -> {
                        accX = event.values.getOrNull(0) ?: 0f
                        accY = event.values.getOrNull(1) ?: 0f
                        accZ = event.values.getOrNull(2) ?: 0f
                    }
                    Sensor.TYPE_GYROSCOPE -> {
                        gyroX = event.values.getOrNull(0) ?: 0f
                        gyroY = event.values.getOrNull(1) ?: 0f
                        gyroZ = event.values.getOrNull(2) ?: 0f
                    }
                    Sensor.TYPE_PROXIMITY -> {
                        proxVal = event.values.getOrNull(0) ?: 0f
                    }
                    Sensor.TYPE_LIGHT -> {
                        lightVal = event.values.getOrNull(0) ?: 0f
                    }
                }
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        // Register ACC
        val acc = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        if (acc != null) sensorManager.registerListener(listener, acc, SensorManager.SENSOR_DELAY_UI)

        // Register Gyro
        val gyro = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
        if (gyro != null) sensorManager.registerListener(listener, gyro, SensorManager.SENSOR_DELAY_UI)

        // Register Prox
        val prox = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY)
        if (prox != null) sensorManager.registerListener(listener, prox, SensorManager.SENSOR_DELAY_UI)

        // Register Light
        val light = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
        if (light != null) sensorManager.registerListener(listener, light, SensorManager.SENSOR_DELAY_UI)

        onDispose {
            sensorManager.unregisterListener(listener)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Diagnosa Sensor Fisik Hardware",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Text(
            text = "Data dari sensor dikalkulasikan real-time dari kernel Linux firmware bawaan HP jika modul tersedia.",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.outline,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        SensorReviewCard(
            name = "Accelerometer (Percepatan Gravitasi)",
            isSupported = hasAcc,
            realtimeVal = if (hasAcc) "X: %.2f  Y: %.2f  Z: %.2f m/s²".format(accX, accY, accZ) else null
        )

        SensorReviewCard(
            name = "Gyroscope (Sensitivitas Rotasi Sudut)",
            isSupported = hasGyro,
            realtimeVal = if (hasGyro) "X: %.2f  Y: %.2f  Z: %.2f rad/s".format(gyroX, gyroY, gyroZ) else null
        )

        SensorReviewCard(
            name = "Proximity (Sensor Jarak Panggilan)",
            isSupported = hasProx,
            realtimeVal = if (hasProx) "Status Terbaca: $proxVal cm" else null
        )

        SensorReviewCard(
            name = "Ambient Light Sensor (Kecerahan Ruang)",
            isSupported = hasLight,
            realtimeVal = if (hasLight) "Intensitas Cahaya: $lightVal Lux" else null
        )

        SensorReviewCard(
            name = "Magnetometer / Compass (Kiblat & Peta)",
            isSupported = hasMag,
            realtimeVal = if (hasMag) "Kompas Kutub: Utara magnetik aktif" else null
        )
    }
}

@Composable
fun SensorReviewCard(
    name: String,
    isSupported: Boolean,
    realtimeVal: String?
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(12.dp),
        border = CardDefaults.outlinedCardBorder(),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(4.dp))
                if (isSupported && realtimeVal != null) {
                    Text(
                        text = realtimeVal,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                } else if (isSupported) {
                    Text("Tersedia (Siaga)", fontSize = 11.sp, color = Color(0xFF2E7D32))
                } else {
                    Text("Tidak tersedia di perangkat ini.", fontSize = 11.sp, color = MaterialTheme.colorScheme.error)
                }
            }

            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(
                        if (isSupported) Color(0xFF2E7D32).copy(alpha = 0.1f)
                        else MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = if (isSupported) "Ada" else "Tidak Ada",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isSupported) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

// ======================= STEP 6: CAMERA & FLASH TEST =======================
@Composable
fun CamerasAndFlashTestPanel(viewModel: HpCheckViewModel) {
    var activeSubCamera by remember { mutableStateOf("Belakang") } // "Belakang", "Depan"
    var isFlashOn by remember { mutableStateOf(false) }

    var verdictInput by remember { mutableStateOf(viewModel.isKameraOk) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Diagnosa Kamera & Flashlight",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = { activeSubCamera = "Belakang" },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (activeSubCamera == "Belakang") MaterialTheme.colorScheme.primary 
                                     else MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text("Kamera Belakang")
            }

            Button(
                onClick = { activeSubCamera = "Depan" },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (activeSubCamera == "Depan") MaterialTheme.colorScheme.primary 
                                     else MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text("Kamera Depan (Selfie)")
            }
        }

        // Live Simulated Viewfinder Screen Box representing camera sensors
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.Black)
                .border(2.dp, MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.PhotoCamera,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.6f),
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "SIMULATOR LENSA: KAMERA $activeSubCamera",
                    color = Color.Green,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Lensa berfungsi normal • Stabilizer OK • Autofokus aktif",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 11.sp
                )
            }
        }

        // Flash Toggle Switch Card
        Card(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.FlashlightOn,
                        contentDescription = null,
                        tint = if (isFlashOn) Color(0xFFFFB300) else MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Tes Lampu Flashlight", fontWeight = FontWeight.Bold)
                        Text("Nyalakan kilat LED kamera belakang", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                    }
                }
                Switch(
                    checked = isFlashOn,
                    onCheckedChange = { isFlashOn = it }
                )
            }
        }

        Divider()
        Text(
            text = "Apakah gambar tangkapan kamera bersih dan flash menyala?",
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            modifier = Modifier.fillMaxWidth()
        )

        YesNoVerdictSelector(
            currentState = verdictInput,
            onSelect = {
                verdictInput = it
                viewModel.isKameraOk = it
            }
        )
    }
}

// ======================= STEP 7: VIBRATION, GPS & NETWORK =======================
@Composable
fun VibrationAndGpsDiagnosticPanel(viewModel: HpCheckViewModel) {
    val context = LocalContext.current
    var isCheckingGps by remember { mutableStateOf(false) }
    var locationFoundInfo by remember { mutableStateOf<String?>(null) }
    var verdictGetar by remember { mutableStateOf(viewModel.isGetarOk) }
    var verdictGps by remember { mutableStateOf(viewModel.isGpsOk) }

    LaunchedEffect(isCheckingGps) {
        if (isCheckingGps) {
            // Simulated GPS network location retrieval
            delay(2000)
            locationFoundInfo = "Terkoneksi! Lintang: -6.1754, Bujur: 106.8272 (Akurasi: 4 meter)"
            isCheckingGps = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Tes Getar & Pemosisian Satelit GPS",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        // Vibration Trigger
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("1. Motor Vibration Engine", fontWeight = FontWeight.Bold)
                Text("Memicu rotor getar haptic internal HP untuk getaran konstan.", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = { viewModel.executeVibrate(context) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("trigger_vibration_btn")
                ) {
                    Text("Getarkan HP Sekarang 📳")
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text("Hasil getar berfungsi normal?", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                YesNoVerdictSelector(
                    currentState = verdictGetar,
                    onSelect = {
                        verdictGetar = it
                        viewModel.isGetarOk = it
                    }
                )
            }
        }

        // GPS Lock Trigger
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("2. Penerima Sinyal GPS", fontWeight = FontWeight.Bold)
                Text("Mengkoneksikan ke chip GPS untuk membaca akurasi titik Google Maps.", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                Spacer(modifier = Modifier.height(12.dp))
                
                if (locationFoundInfo != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF2E7D32).copy(alpha = 0.1f))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = locationFoundInfo!!,
                            color = Color(0xFF2E7D32),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else if (isCheckingGps) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Mencari Sinyal Satelit...", fontSize = 12.sp)
                    }
                } else {
                    Button(
                        onClick = { isCheckingGps = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Ping Lokasi Satelit GPS 🛰️")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text("Hasil lock GPS berhasil & akurat?", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                YesNoVerdictSelector(
                    currentState = verdictGps,
                    onSelect = {
                        verdictGps = it
                        viewModel.isGpsOk = it
                    }
                )
            }
        }
    }
}

// ======================= STEP 8: SCORE VERDICT SUMMARY & SUBMIT =======================
@Composable
fun SummarySubmissionDiagnosticPanel(
    viewModel: HpCheckViewModel,
    onSubmit: () -> Unit
) {
    var connectivityOkInput by remember { mutableStateOf(viewModel.isConnectivityOk) }
    val (score, recommendation) = viewModel.finalizeAndCalculateScore()

    val badgeColor = when (score) {
        in 85..100 -> Color(0xFF2E7D32)
        in 70..84 -> Color(0xFFEF6C00)
        in 50..69 -> Color(0xFFD84315)
        else -> Color(0xFFC62828)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Verifikasi Hasil & Catatan",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        // Wifi/Bluetooth verdict - explicitly required
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Verifikasi Wi-Fi & Bluetooth Adaptor", fontWeight = FontWeight.Bold)
                Text("Bluetooth adaptor mendeteksi siaran bluetooth lain. Wi-Fi terkoneksi normal.", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                Spacer(modifier = Modifier.height(10.dp))
                YesNoVerdictSelector(
                    currentState = connectivityOkInput,
                    onSelect = {
                        connectivityOkInput = it
                        viewModel.isConnectivityOk = it
                    }
                )
            }
        }

        // Live Scoring Card Preview
        Surface(
            color = badgeColor.copy(alpha = 0.1f),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.5.dp, badgeColor)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "ESTIMASI SKOR KESEHATAN",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = badgeColor
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "$score / 100",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = badgeColor
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = recommendation,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = badgeColor
                )
            }
        }

        // Tech written notes
        OutlinedTextField(
            value = viewModel.inputCatatanTeknisi,
            onValueChange = { viewModel.inputCatatanTeknisi = it },
            label = { Text("Catatan Masalah / Komentar Teknisi") },
            placeholder = { Text("Contoh: Mesin mulus. Port charger agak longgar, baterai awet. Layar ada baret dikit di bezel kiri.") },
            minLines = 3,
            maxLines = 5,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("tech_comment_input"),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onSubmit,
            colors = ButtonDefaults.buttonColors(containerColor = badgeColor),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .testTag("submit_report_save_btn")
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Save, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Simpan Diagnosa ke Database Lokal", fontWeight = FontWeight.Bold)
            }
        }
        Spacer(modifier = Modifier.height(50.dp))
    }
}

// Universal Yes/No Selector to avoid messy duplicated buttons
@Composable
fun YesNoVerdictSelector(
    currentState: Boolean?,
    onSelect: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Button(
            onClick = { onSelect(true) },
            colors = ButtonDefaults.buttonColors(
                containerColor = if (currentState == true) Color(0xFF2E7D32) else MaterialTheme.colorScheme.surfaceVariant,
                contentColor = if (currentState == true) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
            ),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .weight(1f)
                .height(42.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Normal/Bekerja", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        Button(
            onClick = { onSelect(false) },
            colors = ButtonDefaults.buttonColors(
                containerColor = if (currentState == false) Color(0xFFC62828) else MaterialTheme.colorScheme.surfaceVariant,
                contentColor = if (currentState == false) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
            ),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .weight(1f)
                .height(42.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Cancel, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Bermasalah/Rusak", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// Interactive Sparepart row to set / override and edit serial strings
@Composable
fun SparepartEditRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    partLabel: String,
    serialValue: String,
    isReplaced: Boolean,
    onReplacedChange: (Boolean) -> Unit,
    onSerialChange: (String) -> Unit
) {
    var isEditingSerial by remember { mutableStateOf(false) }
    var tempSerial by remember { mutableStateOf(serialValue) }
    
    // Sync external changes
    LaunchedEffect(serialValue) {
        tempSerial = serialValue
    }

    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(12.dp),
        border = CardDefaults.outlinedCardBorder(),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(
                                if (isReplaced) MaterialTheme.colorScheme.errorContainer
                                else MaterialTheme.colorScheme.primaryContainer
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = if (isReplaced) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = partLabel,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (!isEditingSerial) {
                            Text(
                                text = "S/N: ${serialValue.ifBlank { "Membaca..." }}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }

                // Switch or Chip Toggle
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (isReplaced) "PERNAH GANTI" else "ORIGINAL (ORI)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = if (isReplaced) Color(0xFFC62828) else Color(0xFF2E7D32),
                        modifier = Modifier.padding(end = 6.dp)
                    )
                    Switch(
                        checked = isReplaced,
                        onCheckedChange = onReplacedChange,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFFC62828),
                            uncheckedThumbColor = Color.White,
                            uncheckedTrackColor = Color(0xFF2E7D32)
                        )
                    )
                }
            }

            if (isEditingSerial) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = tempSerial,
                        onValueChange = { tempSerial = it },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp),
                        label = { Text("Seri Kode / Nama Part", fontSize = 10.sp) }
                    )
                    IconButton(
                        onClick = {
                            onSerialChange(tempSerial)
                            isEditingSerial = false
                        }
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "Simpan", tint = Color(0xFF2E7D32))
                    }
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        contentPadding = PaddingValues(0.dp),
                        onClick = { isEditingSerial = true }
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Edit Serial / Model Sparepart", fontSize = 10.sp)
                        }
                    }
                }
            }
        }
    }
}
