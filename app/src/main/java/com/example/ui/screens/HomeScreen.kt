package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Mosque
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.config.AppConfig
import com.example.data.local.LastReadPosition
import com.example.data.local.ReadingHistoryEntity
import com.example.data.local.UserSettings
import com.example.data.model.DailyPrayerSchedule
import com.example.data.model.HijriDate
import com.example.service.PrayerTimesCalculator
import com.example.ui.components.GoldenMedallion
import com.example.ui.theme.AmiriFontFamily
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.ForestDark
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldLight
import com.example.ui.viewmodel.Screen

@Composable
fun HomeScreen(
    lastRead: LastReadPosition,
    settings: UserSettings,
    history: List<ReadingHistoryEntity>,
    prayerSchedule: DailyPrayerSchedule?,
    todayHijri: HijriDate,
    onNavigate: (Screen) -> Unit,
    onIncrementAyatProgress: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 760.dp)
                .testTag("home_screen"),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // Hero Header with Brand & Logo
        item {
            HeroHeaderSection(
                lastRead = lastRead,
                todayHijri = todayHijri,
                onContinueReading = {
                    onNavigate(Screen.Reader(lastRead.surahNumber, lastRead.verseNumber))
                },
                onOpenHijriCalendar = {
                    onNavigate(Screen.HijriCalendar)
                }
            )
        }

        // Prayer Time & Qibla Quick Summary Widget
        item {
            PrayerAndQiblaSummaryCard(
                prayerSchedule = prayerSchedule,
                cityName = settings.cityName,
                onOpenPrayerTimes = { onNavigate(Screen.PrayerTimes) },
                onOpenQibla = { onNavigate(Screen.Qibla) }
            )
        }

        // Daily Tilawah Progress Card
        item {
            DailyTilawahCard(
                target = settings.dailyAyatTarget,
                currentCount = settings.dailyAyatReadCount,
                onAddProgress = onIncrementAyatProgress
            )
        }

        // Main Navigation Grid (6 Hubs: Surah, Sholat, Kiblat, Kalender, Cari, Tersimpan)
        item {
            Text(
                text = "Fitur Utama",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // Row 1: 114 Surah & Jadwal Sholat
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                QuickNavButton(
                    icon = Icons.Filled.MenuBook,
                    title = "114 Surah",
                    subtitle = "Membaca Al-Qur'an",
                    color = EmeraldPrimary,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate(Screen.SurahList) }
                )
                QuickNavButton(
                    icon = Icons.Filled.Mosque,
                    title = "Jadwal Sholat",
                    subtitle = "Waktu Ibadah & Adzan",
                    color = ForestDark,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate(Screen.PrayerTimes) }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Row 2: Arah Kiblat & Kalender Hijriah
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                QuickNavButton(
                    icon = Icons.Filled.Explore,
                    title = "Arah Kiblat",
                    subtitle = "Kompas Ka'bah",
                    color = Color(0xFF047857),
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate(Screen.Qibla) }
                )
                QuickNavButton(
                    icon = Icons.Filled.CalendarMonth,
                    title = "Kalender Islam",
                    subtitle = "Hijriah & Hari Besar",
                    color = Color(0xFF854D0E),
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate(Screen.HijriCalendar) }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Row 3: Cari Ayat & Bookmark / Catatan
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                QuickNavButton(
                    icon = Icons.Filled.Search,
                    title = "Cari Ayat",
                    subtitle = "Pencarian Kata",
                    color = Color(0xFF0284C7),
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate(Screen.Search) }
                )
                QuickNavButton(
                    icon = Icons.Filled.Bookmark,
                    title = "Tersimpan",
                    subtitle = "Bookmark & Catatan",
                    color = Color(0xFF7C3AED),
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate(Screen.Bookmarks) }
                )
            }
        }

        // Surah Pilihan (Recommended Surahs: Al-Kahf, Ya-Sin, Al-Mulk)
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Surah Pilihan",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                RecommendedSurahChip(
                    name = "Al-Kahf",
                    number = 18,
                    verses = "110 Ayat",
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate(Screen.Reader(18)) }
                )
                RecommendedSurahChip(
                    name = "Ya-Sin",
                    number = 36,
                    verses = "83 Ayat",
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate(Screen.Reader(36)) }
                )
                RecommendedSurahChip(
                    name = "Al-Mulk",
                    number = 67,
                    verses = "30 Ayat",
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate(Screen.Reader(67)) }
                )
            }
        }

        // Recent Reading History Section
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Riwayat Tilawah",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Daftar Surah",
                    style = MaterialTheme.typography.labelSmall,
                    color = EmeraldPrimary,
                    modifier = Modifier.clickable { onNavigate(Screen.SurahList) }
                )
            }
        }

        if (history.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Belum ada riwayat bacaan. Buka surah untuk mulai bertilawah.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(history.take(4)) { item ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .clickable { onNavigate(Screen.Reader(item.surahNumber, item.verseNumber)) },
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        GoldenMedallion(number = item.surahNumber)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.surahName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Ayat ${item.verseNumber}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(
                            imageVector = Icons.Filled.ChevronRight,
                            contentDescription = "Buka",
                            tint = EmeraldPrimary
                        )
                    }
                }
            }
        }
    }
}
}

@Composable
private fun HeroHeaderSection(
    lastRead: LastReadPosition,
    todayHijri: HijriDate,
    onContinueReading: () -> Unit,
    onOpenHijriCalendar: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    listOf(
                        ForestDark,
                        EmeraldPrimary
                    )
                ),
                shape = RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp)
            )
            .padding(top = 24.dp, bottom = 28.dp, start = 18.dp, end = 18.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    // Hijri Date Pill
                    Surface(
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.15f),
                        modifier = Modifier.clickable(onClick = onOpenHijriCalendar)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                        ) {
                            Text("🌙", fontSize = 11.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = todayHijri.toFormattedString(),
                                color = GoldLight,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = AppConfig.APP_NAME,
                        color = Color.White,
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = AppConfig.TAGLINE,
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 13.sp
                    )
                }

                // App Brand Icon
                Image(
                    painter = painterResource(id = R.drawable.ic_app_icon),
                    contentDescription = "DD Qur'an Logo",
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.5.dp, GoldAccent, RoundedCornerShape(16.dp))
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // "Lanjut Membaca" Hero Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("last_read_hero_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.12f)
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, GoldAccent.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.MenuBook,
                                contentDescription = null,
                                tint = GoldAccent,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Terakhir Dibaca",
                                color = GoldLight,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = lastRead.surahName,
                            color = Color.White,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Surah ke-${lastRead.surahNumber} • Ayat ${lastRead.verseNumber}",
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 13.sp
                        )
                    }

                    Button(
                        onClick = onContinueReading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GoldAccent,
                            contentColor = ForestDark
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Lanjut",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PrayerAndQiblaSummaryCard(
    prayerSchedule: DailyPrayerSchedule?,
    cityName: String,
    onOpenPrayerTimes: () -> Unit,
    onOpenQibla: () -> Unit
) {
    if (prayerSchedule == null) return
    val next = prayerSchedule.nextPrayer ?: prayerSchedule.subuh

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Next Prayer Info
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable(onClick = onOpenPrayerTimes)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(EmeraldPrimary.copy(alpha = 0.12f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Filled.AccessTime, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Sholat Berikutnya: ${next.prayer.displayName}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${next.timeFormatted} (${cityName}) • -${PrayerTimesCalculator.formatCountdown(prayerSchedule.countdownMillis)}",
                            fontSize = 11.sp,
                            color = EmeraldPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Qibla Shortcut Button
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = GoldLight.copy(alpha = 0.5f),
                    modifier = Modifier.clickable(onClick = onOpenQibla)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Icon(imageVector = Icons.Filled.Explore, contentDescription = null, tint = ForestDark, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Kiblat", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ForestDark)
                    }
                }
            }
        }
    }
}

@Composable
private fun DailyTilawahCard(
    target: Int,
    currentCount: Int,
    onAddProgress: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Target Tilawah Harian",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (currentCount >= target) "Alhamdulillah! Target hari ini tercapai." else "Tetap istiqomah membaca Al-Qur'an.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = CircleShape,
                    color = if (currentCount >= target) EmeraldPrimary else GoldLight.copy(alpha = 0.5f)
                ) {
                    Text(
                        text = "$currentCount / $target Ayat",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        color = if (currentCount >= target) Color.White else ForestDark,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            val progress = (currentCount.toFloat() / target.coerceAtLeast(1).toFloat()).coerceIn(0f, 1f)
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = if (progress >= 1f) EmeraldPrimary else GoldAccent,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                OutlinedButton(
                    onClick = onAddProgress,
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Icon(imageVector = Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("+1 Ayat Dibaca", fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun QuickNavButton(
    icon: ImageVector,
    title: String,
    subtitle: String,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(color.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun RecommendedSurahChip(
    name: String,
    number: Int,
    verses: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = EmeraldPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = verses,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
