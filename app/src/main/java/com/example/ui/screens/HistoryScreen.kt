package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.data.AttendanceRecord
import com.example.ui.AttendanceViewModel
import com.example.ui.components.AgentMarker
import com.example.ui.components.getAvatarColor
import com.example.ui.components.getInitials
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: AttendanceViewModel,
    modifier: Modifier = Modifier
) {
    val selectedProjectId by viewModel.selectedProjectId.collectAsState()
    val members by viewModel.membersForSelectedProject.collectAsState()
    val selectedMemberId by viewModel.selectedMemberId.collectAsState()
    val rawHistoryRecords by viewModel.selectedMemberHistory.collectAsState()

    // Ensure first member is selected by default if nothing selected yet and list not empty
    LaunchedEffect(members, selectedMemberId) {
        if (selectedMemberId == null && members.isNotEmpty()) {
            viewModel.selectMember(members.first().id)
        }
    }

    val selectedMember = members.find { it.id == selectedMemberId }

    // Human readable parsing helpers
    val dateParseFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US) }
    val monthNameFormat = remember { SimpleDateFormat("MMMM yyyy", Locale.US) }
    val dayNameFormat = remember { SimpleDateFormat("EEEE, MMM d, yyyy", Locale.US) }

    // Compute stats
    val totalRecords = rawHistoryRecords.size
    val presentRecords = rawHistoryRecords.count { it.isPresent }
    val absentRecords = totalRecords - presentRecords
    val attendanceRate = if (totalRecords > 0) {
        (presentRecords.toFloat() / totalRecords * 100).toInt()
    } else 0

    // Group records by Month string (e.g. "June 2026")
    val groupedHistory = remember(rawHistoryRecords) {
        rawHistoryRecords.groupBy { record ->
            try {
                val date = dateParseFormat.parse(record.dateString)
                monthNameFormat.format(date!!)
            } catch (e: Exception) {
                "Unknown Month"
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "History Logs",
                            fontWeight = FontWeight.Bold
                        )
                        AgentMarker()
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)
                )
            )
        }
    ) { innerPadding ->
        if (selectedProjectId == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    )
                    Text(
                        text = "No workspace active",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Please go to the Projects tab and select or create a project cohort first.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else if (members.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    )
                    Text(
                        text = "No members to track",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Create members first under the Mark Attendance tab to browse their logs.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // Scrollable Member Selector Row
                Text(
                    text = "Select Member",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(members, key = { it.id }) { m ->
                        val isSelected = m.id == selectedMemberId
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.selectMember(m.id) },
                            label = { Text(text = m.name) },
                            modifier = Modifier.testTag("member_chip_${m.id}"),
                            leadingIcon = {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .background(getAvatarColor(m.name), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = getInitials(m.name),
                                        color = Color.White,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (selectedMember == null) {
                    // Loading or invalid
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                } else {
                    // Summary Stats Card (Bento Style Card)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        shape = RoundedCornerShape(24.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .background(getAvatarColor(selectedMember.name), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = getInitials(selectedMember.name),
                                        color = Color.White,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Text(
                                    text = selectedMember.name,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                StatItem(
                                    label = "Checked Days",
                                    value = totalRecords.toString(),
                                    modifier = Modifier.weight(1f)
                                )
                                StatItem(
                                    label = "Present",
                                    value = presentRecords.toString(),
                                    color = Color(0xFF2E7D32),
                                    modifier = Modifier.weight(1f)
                                )
                                StatItem(
                                    label = "Absent",
                                    value = absentRecords.toString(),
                                    color = Color(0xFFC62828),
                                    modifier = Modifier.weight(1f)
                                )
                                StatItem(
                                    label = "Presence Rate",
                                    value = "$attendanceRate%",
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Attendance Records",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    if (rawHistoryRecords.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No records logged yet.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            contentPadding = PaddingValues(bottom = 100.dp)
                        ) {
                            groupedHistory.forEach { (monthStr, records) ->
                                item {
                                    Text(
                                        text = monthStr,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(start = 4.dp, top = 8.dp)
                                    )
                                }

                                items(records, key = { it.id }) { record ->
                                    val formattedDate = try {
                                        val day = dateParseFormat.parse(record.dateString)
                                        dayNameFormat.format(day!!)
                                    } catch (e: Exception) {
                                        record.dateString
                                    }

                                    HistoryRecordItem(
                                        dateString = formattedDate,
                                        isPresent = record.isPresent
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Black,
            color = if (color == Color.Unspecified) MaterialTheme.colorScheme.onSurface else color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun HistoryRecordItem(
    dateString: String,
    isPresent: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = dateString,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
            }

            Box(
                modifier = Modifier
                    .background(
                        color = if (isPresent) Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = if (isPresent) "PRESENT" else "ABSENT",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isPresent) Color(0xFF2E7D32) else Color(0xFFC62828),
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}
