package com.example.ui.screens

import android.content.Context
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.HpCheckReport
import com.example.ui.viewmodel.HpCheckViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportDetailScreen(
    viewModel: HpCheckViewModel,
    onNavigateBack: () -> Unit,
    onDeleted: () -> Unit
) {
    val report = viewModel.currentReportDetail
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var showEditNotesDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    if (report == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Data Laporan Kosong")
        }
    } else {
        val scoreColor = when (report.skorKesehatan) {
            in 85..100 -> Color(0xFF2E7D32)
            in 70..84 -> Color(0xFFEF6C00)
            in 50..69 -> Color(0xFFD84315)
            else -> Color(0xFFC62828)
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Laporan Diagnosis HP", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                        }
                    },
                    actions = {
                        IconButton(onClick = { showEditNotesDialog = true }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit Catatan", tint = MaterialTheme.colorScheme.primary)
                        }
                        IconButton(onClick = { showDeleteConfirmDialog = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "Hapus Data", tint = MaterialTheme.colorScheme.error)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(innerPadding)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    
                    // PRINT DOCUMENT CARD LAYOUT (Aesthetic resembling an invoice paper sheet check)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = CardDefaults.outlinedCardBorder(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            
                            // LOGO APPLICATION BRAND HEADER
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(MaterialTheme.colorScheme.primary),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PhoneAndroid,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onPrimary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "HPCheck Pro",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 15.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                
                                Text(
                                    text = "OFFICIAL REPORT",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                            
                            Divider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 2.dp)

                            // DATE & SPEC TABLE 
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Identitas HP", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                                    Text("Brand: ${report.merk}", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    Text("Model: ${report.tipe}", fontSize = 13.sp)
                                    Text("Spec: ${report.ram} / ${report.storage}", fontSize = 13.sp)
                                    Text("IMEI: ${report.imei}", fontSize = 13.sp)
                                    Text("Serial: ${report.hardwareSerial}", fontSize = 13.sp, color = Color.Gray)
                                    Text("Option: ${report.warna}", fontSize = 13.sp)
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Tanggal Uji", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                                    val readableDate = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(report.timestamp))
                                    Text(readableDate, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text("Nilai Kondisi", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                                    Text(
                                        text = "${report.skorKesehatan}%",
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Black,
                                        color = scoreColor
                                    )
                                    Text(report.statusRekomendasi, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = scoreColor)
                                }
                            }

                            Divider(color = MaterialTheme.colorScheme.outlineVariant)

                            // VERDICT LIST OF COMPACT Hardware Checked rows
                            Text(
                                text = "RINGKASAN PARAMETER CHECKED:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )

                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                CheckResultLine("Layar Sentuh & Dead Pixel", report.isLayarOk)
                                CheckResultLine("Speaker Musik & Buzzer", report.isSpeakerOk)
                                CheckResultLine("Microphone Core", report.isMicOk)
                                CheckResultLine("Kamera Depan + Belakang", report.isKameraOk)
                                CheckResultLine("Modul Akselerometer / Sensor", report.isSensorOk)
                                CheckResultLine("Rotor Motor Getaran", report.isGetarOk)
                                CheckResultLine("Chip Lokasi Satelit GPS", report.isGpsOk)
                                CheckResultLine("Antena Wifi & Bluetooth", report.isConnectivityOk)
                            }

                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                            // SPAREPARTS SECTION
                            Text(
                                text = "ANALISIS DAFTAR SERI & SUKU CADANG:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )

                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                SparepartResultLine("Layar (Panel)", report.sparepartLayarSeri, report.isLayarGanti)
                                SparepartResultLine("Baterai Utama", report.sparepartBateraiSeri, report.isBateraiGanti)
                                SparepartResultLine("Kamera Belakang", report.sparepartKameraSeri, report.isKameraGanti)
                                SparepartResultLine("Mesin / SoC Core", report.sparepartMesinSeri, report.isMesinGanti)
                                SparepartResultLine("Port Charger", report.sparepartPortSeri, report.isPortGanti)
                            }

                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                            // COMMERCE VALUES
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Estimasi Harga Jual:", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Text(formatCurrency(report.hargaJualEstimasi), fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF2E7D32))
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            // TECHNICIAN SUMMARY COMMENT 
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.background)
                                    .padding(12.dp)
                            ) {
                                Column {
                                    Text("Catatan Tambahan Teknisi:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = report.catatanTeknisi,
                                        fontSize = 11.sp,
                                        lineHeight = 15.sp,
                                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                                    )
                                }
                            }

                            // EXPLICIT M3 MANDATORY LEGAL DISCLAIMER
                            Spacer(modifier = Modifier.height(8.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                    .padding(8.dp)
                            ) {
                                Text(
                                    text = "DISCLAIMER: Hasil pengecekan ini adalah bantuan diagnosa awal. Beberapa komponen seperti IC, riwayat servis, part original, dan kerusakan tersembunyi tetap perlu dicek manual oleh teknisi.",
                                    fontSize = 10.sp,
                                    lineHeight = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.error,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    // BOTTOM CARD SHARE ACTION
                    Button(
                        onClick = { shareTextReport(context, report) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("report_detail_btn_share"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Share, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Bagikan Laporan PDF/Kertas", fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(50.dp))
                }
            }
        }
    }

    // Edit Notes Modal Dialog
    if (showEditNotesDialog && report != null) {
        var tempNotes by remember { mutableStateOf(report.catatanTeknisi) }

        AlertDialog(
            onDismissRequest = { showEditNotesDialog = false },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateReportNotes(report.id, tempNotes)
                        showEditNotesDialog = false
                    }
                ) {
                    Text("Simpan")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditNotesDialog = false }) {
                    Text("Batal")
                }
            },
            title = { Text("Edit Catatan Teknisi") },
            text = {
                OutlinedTextField(
                    value = tempNotes,
                    onValueChange = { tempNotes = it },
                    label = { Text("Catatan Baru") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5,
                    shape = RoundedCornerShape(8.dp)
                )
            }
        )
    }

    // Delete Confirmation dialog
    if (showDeleteConfirmDialog && report != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteReport(report.id) {
                            showDeleteConfirmDialog = false
                            onDeleted()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Ya, Hapus")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Batal")
                }
            },
            title = { Text("Hapus Laporan Uji?") },
            text = { Text("Apakah Anda yakin ingin menghapus data diagnosa ${report.merk} ${report.tipe} secara permanen?") }
        )
    }
}

@Composable
fun CheckResultLine(label: String, isOk: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
        )
        Text(
            text = if (isOk) "LULUS (✓)" else "MASALAH (✗)",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = if (isOk) Color(0xFF2E7D32) else Color(0xFFC62828)
        )
    }
}

@Composable
fun SparepartResultLine(label: String, serial: String, isReplaced: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "S/N: ${serial.ifBlank { "Membaca..." }}",
                fontSize = 10.sp,
                color = Color.Gray
            )
        }
        Text(
            text = if (isReplaced) "PERNAH GANTI" else "ORIGINAL (✓)",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = if (isReplaced) Color(0xFFC62828) else Color(0xFF2E7D32)
        )
    }
}
