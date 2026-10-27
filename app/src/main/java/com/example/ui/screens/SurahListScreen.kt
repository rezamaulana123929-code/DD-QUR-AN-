package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SurahDetail
import com.example.data.model.SurahItem
import com.example.data.repository.ResourceState
import com.example.ui.components.EmptyStateView
import com.example.ui.components.ErrorStateView
import com.example.ui.components.GoldenMedallion
import com.example.ui.components.LoadingStateView
import com.example.ui.theme.AmiriFontFamily
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldLight
import com.example.ui.viewmodel.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SurahListScreen(
    surahsState: ResourceState<List<SurahItem>>,
    onSelectSurah: (Int) -> Unit,
    onPlaySurahAudio: (SurahItem) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("Semua") } // "Semua", "Mekah", "Madinah"

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("surah_list_screen")
    ) {
        // Screen Header & Search Box
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "Daftar Surah",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "114 Surah Al-Qur'anul Karim",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("surah_search_input"),
                    placeholder = { Text("Cari nama surah, arti, atau nomor...") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Filled.Search, contentDescription = null, tint = EmeraldPrimary)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(imageVector = Icons.Filled.Clear, contentDescription = "Hapus")
                            }
                        }
                    },
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedIndicatorColor = EmeraldPrimary,
                        unfocusedIndicatorColor = MaterialTheme.colorScheme.outline
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Filter Chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val filters = listOf("Semua", "Mekah", "Madinah")
                    items(filters) { filter ->
                        val isSelected = selectedFilter == filter
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedFilter = filter },
                            label = {
                                Text(
                                    text = when (filter) {
                                        "Semua" -> "Semua (114)"
                                        "Mekah" -> "Makkiyah (86)"
                                        "Madinah" -> "Madaniyah (28)"
                                        else -> filter
                                    },
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = EmeraldPrimary,
                                selectedLabelColor = Color.White
                            ),
                            shape = RoundedCornerShape(20.dp)
                        )
                    }
                }
            }
        }

        // Surah Items List
        when (surahsState) {
            is ResourceState.Loading -> {
                LoadingStateView(message = "Memuat 114 Surah dari EQuran.id...")
            }
            is ResourceState.Error -> {
                ErrorStateView(
                    message = surahsState.message,
                    onRetry = onRetry
                )
            }
            is ResourceState.Success -> {
                val filteredSurahs = surahsState.data.filter { surah ->
                    val matchesQuery = if (searchQuery.isBlank()) {
                        true
                    } else {
                        val q = searchQuery.trim().lowercase()
                        surah.namaLatin.lowercase().contains(q) ||
                                surah.arti.lowercase().contains(q) ||
                                surah.nama.contains(q) ||
                                surah.nomor.toString() == q
                    }

                    val matchesPlace = when (selectedFilter) {
                        "Mekah" -> surah.tempatTurun.equals("Mekah", ignoreCase = true)
                        "Madinah" -> surah.tempatTurun.equals("Madinah", ignoreCase = true)
                        else -> true
                    }

                    matchesQuery && matchesPlace
                }

                if (filteredSurahs.isEmpty()) {
                    EmptyStateView(
                        title = "Surah Tidak Ditemukan",
                        subtitle = "Tidak ada surah yang sesuai dengan kata pencarian '$searchQuery'."
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredSurahs, key = { it.nomor }) { surah ->
                            SurahCardItem(
                                surah = surah,
                                onClick = { onSelectSurah(surah.nomor) },
                                onPlayAudio = { onPlaySurahAudio(surah) }
                            )
                        }
                    }
                }
            }
            else -> {}
        }
    }
}

@Composable
private fun SurahCardItem(
    surah: SurahItem,
    onClick: () -> Unit,
    onPlayAudio: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("surah_card_${surah.nomor}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Surah Number Medallion
            GoldenMedallion(number = surah.nomor)

            Spacer(modifier = Modifier.width(14.dp))

            // Latin Name, Revelation, Verse count
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = surah.namaLatin,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${surah.tempatTurun} • ${surah.jumlahAyat} Ayat • ${surah.arti}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Arabic Name
            Text(
                text = surah.nama,
                fontFamily = AmiriFontFamily,
                fontSize = 22.sp,
                color = EmeraldPrimary,
                modifier = Modifier.padding(end = 8.dp)
            )

            // Play Surah Audio Button
            IconButton(
                onClick = onPlayAudio,
                modifier = Modifier
                    .size(36.dp)
                    .background(GoldLight.copy(alpha = 0.5f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Filled.PlayArrow,
                    contentDescription = "Putar Audio ${surah.namaLatin}",
                    tint = EmeraldPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
