package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.HIJRI_MONTHS
import com.example.data.model.HijriCalendarDay
import com.example.data.model.HijriDate
import com.example.data.model.ISLAMIC_EVENTS_LIST
import com.example.data.model.IslamicEvent
import com.example.ui.theme.AmiriFontFamily
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.ForestDark
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldLight
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun HijriCalendarScreen(
    todayHijri: HijriDate,
    currentCalendar: Calendar,
    daysGrid: List<HijriCalendarDay>,
    adjustmentDays: Int,
    onNavigateMonth: (Int) -> Unit,
    onResetCurrentMonth: () -> Unit,
    onUpdateAdjustment: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAdjustmentDialog by remember { mutableStateOf(false) }
    var selectedDayDetail by remember { mutableStateOf<HijriCalendarDay?>(null) }

    val monthYearFormat = SimpleDateFormat("MMMM yyyy", Locale("id", "ID"))
    val currentMonthYearString = monthYearFormat.format(currentCalendar.time)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("hijri_calendar_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Today's Hijri Hero Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = ForestDark),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(listOf(ForestDark, EmeraldPrimary))
                        )
                        .padding(20.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "HARI INI DALAM PENANGGALAN HIJRIAH",
                            color = GoldLight,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "${todayHijri.day} ${todayHijri.monthName} ${todayHijri.year} H",
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "${todayHijri.monthNameArabic} ${todayHijri.year}",
                            fontFamily = AmiriFontFamily,
                            fontSize = 24.sp,
                            color = GoldAccent
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "${todayHijri.dayName}, ${todayHijri.gregorianFormatted}",
                                color = Color.White,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }

        // Calendar Month Navigator & Adjustment
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { onNavigateMonth(-1) }) {
                            Icon(imageVector = Icons.Filled.ChevronLeft, contentDescription = "Bulan Sebelumnya", tint = EmeraldPrimary)
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = currentMonthYearString,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            if (adjustmentDays != 0) {
                                Text(
                                    text = "Koreksi: ${if (adjustmentDays > 0) "+$adjustmentDays" else "$adjustmentDays"} Hari",
                                    fontSize = 11.sp,
                                    color = GoldAccent,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        IconButton(onClick = { onNavigateMonth(1) }) {
                            Icon(imageVector = Icons.Filled.ChevronRight, contentDescription = "Bulan Selanjutnya", tint = EmeraldPrimary)
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = onResetCurrentMonth,
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                        ) {
                            Icon(imageVector = Icons.Filled.Today, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Bulan Ini", fontSize = 11.sp)
                        }

                        TextButton(
                            onClick = { showAdjustmentDialog = true },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Icon(imageVector = Icons.Filled.Tune, contentDescription = null, modifier = Modifier.size(14.dp), tint = EmeraldPrimary)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Koreksi Hilal", fontSize = 11.sp, color = EmeraldPrimary)
                        }
                    }
                }
            }
        }

        // Calendar Month Grid View
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    // Day of Week Header Row (Sen, Sel, Rab, Kam, Jum, Sab, Ahd)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        listOf("Sen", "Sel", "Rab", "Kam", "Jum", "Sab", "Ahd").forEach { dayLabel ->
                            Text(
                                text = dayLabel,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (dayLabel == "Ahd") MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // 7-Column Grid of Calendar Days
                    val rows = daysGrid.chunked(7)
                    rows.forEach { week ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            week.forEach { dayItem ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(2.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CalendarDayCell(
                                        day = dayItem,
                                        onClick = { selectedDayDetail = dayItem }
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Legend Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        LegendItem(color = EmeraldPrimary, label = "Hari Ini")
                        LegendItem(color = GoldAccent, label = "Ayyamul Bidh (13-15)")
                        LegendItem(color = Color(0xFFB42318), label = "Hari Besar")
                    }
                }
            }
        }

        // Important Islamic Events Section
        item {
            Text(
                text = "Hari Penting & Peristiwa Islam",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        items(ISLAMIC_EVENTS_LIST) { event ->
            IslamicEventCard(event = event)
        }
    }

    // Day Details Dialog
    selectedDayDetail?.let { day ->
        val monthName = HIJRI_MONTHS.getOrNull(day.hijriMonth - 1)?.first ?: "Bulan Hijriah"
        AlertDialog(
            onDismissRequest = { selectedDayDetail = null },
            title = {
                Text("${day.hijriDay} $monthName ${day.hijriYear} H")
            },
            text = {
                Column {
                    Text(
                        text = "Tanggal Masehi: ${day.gregorianDay}/${day.gregorianMonth}/${day.gregorianYear}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    if (day.isAyyamulBidh) {
                        Surface(
                            color = GoldLight.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Text(
                                text = "✨ Puasa Sunnah Ayyamul Bidh (Hari ke-${day.hijriDay})",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = ForestDark,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    if (day.event != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = day.event.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldPrimary
                        )
                        Text(
                            text = day.event.arabicTitle,
                            fontFamily = AmiriFontFamily,
                            fontSize = 18.sp,
                            color = GoldAccent
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = day.event.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else if (!day.isAyyamulBidh) {
                        Text(
                            text = "Tidak ada peringatan khusus pada hari ini.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { selectedDayDetail = null },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                ) {
                    Text("Tutup")
                }
            }
        )
    }

    // Hijri Adjustment Dialog
    if (showAdjustmentDialog) {
        var tempAdj by remember { mutableStateOf(adjustmentDays.toFloat()) }
        AlertDialog(
            onDismissRequest = { showAdjustmentDialog = false },
            title = { Text("Koreksi Penanggalan Hijriah") },
            text = {
                Column {
                    Text(
                        text = "Penetapan tanggal Hijriah dapat berbeda 1 hari menurut keputusan Sidang Isbat / Rukyatul Hilal Kemenag RI. Anda dapat menggeser hari di sini:",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Penyesuaian: ${tempAdj.toInt()} Hari",
                        fontWeight = FontWeight.Bold,
                        color = EmeraldPrimary,
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Slider(
                        value = tempAdj,
                        onValueChange = { tempAdj = it },
                        valueRange = -2f..2f,
                        steps = 3,
                        colors = SliderDefaults.colors(
                            thumbColor = EmeraldPrimary,
                            activeTrackColor = EmeraldPrimary
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onUpdateAdjustment(tempAdj.toInt())
                        showAdjustmentDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                ) {
                    Text("Simpan")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAdjustmentDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }
}

@Composable
private fun CalendarDayCell(
    day: HijriCalendarDay,
    onClick: () -> Unit
) {
    val backgroundColor = when {
        day.isToday -> EmeraldPrimary
        day.isAyyamulBidh && day.isCurrentMonth -> GoldLight.copy(alpha = 0.5f)
        else -> Color.Transparent
    }

    val textColor = when {
        day.isToday -> Color.White
        !day.isCurrentMonth -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
        else -> MaterialTheme.colorScheme.onSurface
    }

    val borderColor = when {
        day.event != null && day.isCurrentMonth -> Color(0xFFB42318)
        day.isAyyamulBidh && day.isCurrentMonth -> GoldAccent
        else -> Color.Transparent
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.85f)
            .background(backgroundColor, RoundedCornerShape(8.dp))
            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Gregorian Day number
        Text(
            text = "${day.gregorianDay}",
            fontSize = 12.sp,
            fontWeight = if (day.isToday || day.event != null) FontWeight.Bold else FontWeight.Medium,
            color = textColor
        )

        // Hijri Day number underneath
        Text(
            text = "${day.hijriDay}",
            fontSize = 9.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (day.isToday) GoldLight else GoldAccent
        )

        // Indicator dot for events
        if (day.event != null && day.isCurrentMonth) {
            Box(
                modifier = Modifier
                    .size(4.dp)
                    .background(Color(0xFFB42318), CircleShape)
            )
        }
    }
}

@Composable
private fun IslamicEventCard(event: IslamicEvent) {
    val monthName = HIJRI_MONTHS.getOrNull(event.hijriMonth - 1)?.first ?: "Bulan Hijriah"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(
                        if (event.isMajorHoliday) EmeraldPrimary.copy(alpha = 0.12f) else GoldLight.copy(alpha = 0.4f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (event.isMajorHoliday) Icons.Filled.Star else Icons.Filled.Event,
                    contentDescription = null,
                    tint = if (event.isMajorHoliday) EmeraldPrimary else ForestDark,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = event.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${event.hijriDay} $monthName",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = EmeraldPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = event.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            Text(
                text = event.arabicTitle,
                fontFamily = AmiriFontFamily,
                fontSize = 18.sp,
                color = GoldAccent
            )
        }
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(color, CircleShape)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
