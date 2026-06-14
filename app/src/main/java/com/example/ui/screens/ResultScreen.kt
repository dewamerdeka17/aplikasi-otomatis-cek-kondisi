package com.example.ui.screens

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.HpCheckReport
import com.example.ui.viewmodel.HpCheckViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultScreen(
    viewModel: HpCheckViewModel,
    onNavigateToDashboard: () -> Unit,
    onNavigateToReport: () -> Unit
) {
    val report = viewModel.currentReportDetail
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    if (report == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Memuat data hasil checker...", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(10.dp))
                CircularProgressIndicator()
            }
        }
    } else {
        val scoreColor = when (report.skorKesehatan) {
            in 85..100 -> Color(0xFF2E7D32) // Green
            in 70..84 -> Color(0xFFEF6C00) // Amber
            in 50..69 -> Color(0xFFD84315) // Deep Orange
            else -> Color(0xFFC62828) // Red
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Skor & Laporan Hasil", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onNavigateToDashboard) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Selesai")
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
                    .verticalScroll(scrollState)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // SUCCESS NOTIFICATION BADGE
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFE8F5E9)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF2E7D32)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Laporan berhasil dicatat di riwayat lokal!",
                            fontSize = 12.sp,
                            color = Color(0xFF2E7D32),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // HEADER DEVICE INFO NAME
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(16.dp),
                    border = CardDefaults.outlinedCardBorder(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(scoreColor.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PhoneAndroid,
                                contentDescription = null,
                                tint = scoreColor,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "${report.merk} ${report.tipe}",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Spec: ${report.ram} / ${report.storage}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                            Text(
                                text = "IMEI: ${report.imei}  •  S/N: ${report.hardwareSerial}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // DYNAMIC SCORE CIRCULAR GRAPH CARD
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(20.dp),
                    border = CardDefaults.outlinedCardBorder(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "SKOR KELAYAKAN HP",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        // Large Score Circle Display
                        Box(
                            modifier = Modifier
                                .size(110.dp)
                                .clip(CircleShape)
                                .background(scoreColor.copy(alpha = 0.1f))
                                .border(4.dp, scoreColor, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "${report.skorKesehatan}",
                                    fontSize = 38.sp,
                                    fontWeight = FontWeight.Black,
                                    color = scoreColor
                                )
                                Text(
                                    text = "dari 100",
                                    fontSize = 11.sp,
                                    color = scoreColor.copy(alpha = 0.8f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(scoreColor)
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = report.statusRekomendasi.uppercase(Locale.getDefault()),
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }

                // BUSINESS FINANCIAL MARGIN COMPARISON CARD
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Ringkasan Finansial Konter",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Harga Kulakan Beli", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                Text(formatCurrency(report.hargaBeli), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text("Estimasi Jual Konsumen", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                Text(formatCurrency(report.hargaJualEstimasi), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Divider()
                        Spacer(modifier = Modifier.height(8.dp))

                        val profit = report.hargaJualEstimasi - report.hargaBeli
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Estimasi Net Margin", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text(
                                text = "+ ${formatCurrency(profit)}",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (profit > 0) Color(0xFF2E7D32) else Color(0xFFC62828)
                            )
                        }
                    }
                }

                // DETAIL CHECKED LABELS LIST
                Text(
                    text = "Detail Lulus Cek Hardware",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    HardwareCheckResultRow(icon = Icons.Default.Laptop, label = "Layar sentuh & warna LCD", isPassed = report.isLayarOk)
                    HardwareCheckResultRow(icon = Icons.Default.VolumeUp, label = "Speaker Sound & Buzzer", isPassed = report.isSpeakerOk)
                    HardwareCheckResultRow(icon = Icons.Default.Mic, label = "Microphone perekam", isPassed = report.isMicOk)
                    HardwareCheckResultRow(icon = Icons.Default.CameraAlt, label = "Kamera Belakang & Flash", isPassed = report.isKameraOk)
                    HardwareCheckResultRow(icon = Icons.Default.Tune, label = "Modul Hardware Sensors", isPassed = report.isSensorOk)
                    HardwareCheckResultRow(icon = Icons.Default.Vibration, label = "Rotor Motor Getaran", isPassed = report.isGetarOk)
                    HardwareCheckResultRow(icon = Icons.Default.GpsFixed, label = "GPS Satelit Pemosisian", isPassed = report.isGpsOk)
                    HardwareCheckResultRow(icon = Icons.Default.NetworkWifi, label = "Adaptor Wi-Fi & Bluetooth", isPassed = report.isConnectivityOk)
                }

                // DETAIL SPAREPARTS LIST
                Text(
                    text = "Analisis Keaslian & Seri Sparepart",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(16.dp),
                    border = CardDefaults.outlinedCardBorder(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Status & Kode Serial Suku Cadang Terpilih",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                        SparepartResultRow(
                            label = "Layar (LCD / OLED Panel)",
                            serial = report.sparepartLayarSeri,
                            isReplaced = report.isLayarGanti
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                        SparepartResultRow(
                            label = "Baterai Utama (Lithium Cell)",
                            serial = report.sparepartBateraiSeri,
                            isReplaced = report.isBateraiGanti
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                        SparepartResultRow(
                            label = "Kamera Belakang (Main CMOS)",
                            serial = report.sparepartKameraSeri,
                            isReplaced = report.isKameraGanti
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                        SparepartResultRow(
                            label = "Mesin Utama & SoC Core Chip",
                            serial = report.sparepartMesinSeri,
                            isReplaced = report.isMesinGanti
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                        SparepartResultRow(
                            label = "Konektor Port Charger",
                            serial = report.sparepartPortSeri,
                            isReplaced = report.isPortGanti
                        )
                    }
                }

                // TECHNICIAN WRITTEN CARD
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(16.dp),
                    border = CardDefaults.outlinedCardBorder(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Engineering, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Rekomendasi Teknisi Konten", fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = report.catatanTeknisi,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                            lineHeight = 16.sp
                        )
                    }
                }

                // BIG SHARING ACTION BUTTONS
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        shareTextReport(context, report)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("result_btn_share"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Share, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Bagikan Diagnosa ke Pembeli 📤", fontWeight = FontWeight.Bold)
                    }
                }

                OutlinedButton(
                    onClick = onNavigateToReport,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("result_btn_view_report"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Article, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Buka Laporan Profesional (Serikat)", fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(60.dp))
            }
        }
    }
}

@Composable
fun HardwareCheckResultRow(
    icon: ImageVector,
    label: String,
    isPassed: Boolean
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(12.dp),
        border = CardDefaults.outlinedCardBorder(),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(label, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(
                        if (isPassed) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = if (isPassed) "NORMAL (PASS)" else "BERMASALAH (FAIL)",
                    color = if (isPassed) Color(0xFF2E7D32) else Color(0xFFC62828),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

// Helpers for formatted currency
fun formatCurrency(amount: Double): String {
    return try {
        val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        format.format(amount)
    } catch (e: Exception) {
        "Rp " + NumberFormat.getInstance().format(amount)
    }
}

// Global text generator share report
fun shareTextReport(context: Context, report: HpCheckReport) {
    val dateStr = try {
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        sdf.format(Date(report.timestamp))
    } catch (e: Exception) {
        "N/A"
    }

    val body = """
        📊 LAPORAN DIAGNOSA HPCHECK PRO
        ==================================
        Tanggal Cek : $dateStr
        Merek / Tipe: ${report.merk} ${report.tipe}
        RAM / Storage : ${report.ram} / ${report.storage}
        IMEI / SN    : ${report.imei}
        Warna        : ${report.warna}
        ==================================
        📈 SKOR KELAYAKAN: ${report.skorKesehatan} / 100
        Status Kelayakan   : ${report.statusRekomendasi.uppercase()}
        ==================================
        🔧 DETAIL DIAGNOSA PERANGKAT:
        - Layar & LCD      : ${if (report.isLayarOk) "✓ NORMAL" else "✗ BERMASALAH"}
        - Speaker & Buzzer : ${if (report.isSpeakerOk) "✓ NORMAL" else "✗ BERMASALAH"}
        - Microphone       : ${if (report.isMicOk) "✓ NORMAL" else "✗ BERMASALAH"}
        - Kamera & Flash   : ${if (report.isKameraOk) "✓ NORMAL" else "✗ BERMASALAH"}
        - Sensor Perangkat : ${if (report.isSensorOk) "✓ NORMAL" else "✗ BERMASALAH"}
        - Getar Rotor      : ${if (report.isGetarOk) "✓ NORMAL" else "✗ BERMASALAH"}
        - Pemosisian GPS   : ${if (report.isGpsOk) "✓ NORMAL" else "✗ BERMASALAH"}
        - Wifi / Bluetooth : ${if (report.isConnectivityOk) "✓ NORMAL" else "✗ BERMASALAH"}
        ==================================
        🔍 KODE SERI & STATUS SPAREPART:
        - Layar (OLED/LCD): ${report.sparepartLayarSeri} [${if (report.isLayarGanti) "PERNAH GANTI / REPLACED" else "ORIGINAL (GENUINE)"}]
        - Baterai Utama   : ${report.sparepartBateraiSeri} [${if (report.isBateraiGanti) "PERNAH GANTI / REPLACED" else "ORIGINAL (GENUINE)"}]
        - Kamera Belakang : ${report.sparepartKameraSeri} [${if (report.isKameraGanti) "PERNAH GANTI / REPLACED" else "ORIGINAL (GENUINE)"}]
        - SoC / Mesin     : ${report.sparepartMesinSeri} [${if (report.isMesinGanti) "PERNAH GANTI / REPLACED" else "ORIGINAL (GENUINE)"}]
        - Port Charger    : ${report.sparepartPortSeri} [${if (report.isPortGanti) "PERNAH GANTI / REPLACED" else "ORIGINAL (GENUINE)"}]
        ==================================
        📌 CATATAN TEKNISI KONTER:
        "${report.catatanTeknisi}"
        
        💸 ESTIMASI HARGA JUAL: ${formatCurrency(report.hargaJualEstimasi)}
        
        *Disclaimer: Hasil pengecekan ini adalah bantuan diagnosa awal. Beberapa komponen seperti IC, riwayat servis, part original, dan kerusakan tersembunyi tetap perlu dicek manual oleh teknisi.*
    """.trimIndent()

    try {
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, body)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "Bagikan Diagnosa HP")
        context.startActivity(shareIntent)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

@Composable
fun SparepartResultRow(
    label: String,
    serial: String,
    isReplaced: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Text("S/N: ${serial.ifBlank { "Tidak terdeteksi" }}", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
        }
        
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(
                    if (isReplaced) Color(0xFFFFEBEE) else Color(0xFFE8F5E9)
                )
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = if (isReplaced) "PERNAH GANTI" else "ORIGINAL",
                color = if (isReplaced) Color(0xFFC62828) else Color(0xFF2E7D32),
                fontSize = 9.sp,
                fontWeight = FontWeight.Black
            )
        }
    }
}
