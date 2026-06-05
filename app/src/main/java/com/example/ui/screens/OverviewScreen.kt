package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
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
import androidx.compose.ui.unit.sp
import com.example.data.AttendanceRecord
import com.example.data.Member
import com.example.ui.AttendanceViewModel
import com.example.ui.components.AgentMarker
import com.example.ui.components.getAvatarColor
import com.example.ui.components.getInitials
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OverviewScreen(
    viewModel: AttendanceViewModel,
    modifier: Modifier = Modifier
) {
    val selectedProjectId by viewModel.selectedProjectId.collectAsState()
    val members by viewModel.membersForSelectedProject.collectAsState()
    val allRecords by viewModel.projectAllAttendance.collectAsState()

    // Local state for the heatmap viewed month
    var heatmapMonth by remember { mutableStateOf(Calendar.getInstance()) }

    var expandedDateString by remember { mutableStateOf<String?>(null) }

    val monthNameFormat = remember { SimpleDateFormat("MMMM yyyy", Locale.US) }
    val dayKeyFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US) }
    val dateDisplayFormat = remember { SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.US) }

    // Navigation handles
    fun navigateMonth(delta: Int) {
        val cal = heatmapMonth.clone() as Calendar
        cal.add(Calendar.MONTH, delta)
        heatmapMonth = cal
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
                            text = "Project Overview",
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
                        imageVector = Icons.Default.Assessment,
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
        } else {
            // Group records by Date to calculate density and expandable logs
            val recordsByDate = remember(allRecords) {
                allRecords.groupBy { it.dateString }
            }

            // Calculations for viewed month heatmap grid
            val startCal = heatmapMonth.clone() as Calendar
            startCal.set(Calendar.DAY_OF_MONTH, 1)
            val firstDayOfWeek = startCal.get(Calendar.DAY_OF_WEEK)
            val maxDays = startCal.getActualMaximum(Calendar.DAY_OF_MONTH)

            val monthDaysKeys = remember(heatmapMonth, maxDays) {
                (1..maxDays).map { day ->
                    val c = startCal.clone() as Calendar
                    c.set(Calendar.DAY_OF_MONTH, day)
                    dayKeyFormat.format(c.time)
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // Heatmap Title Block
                Text(
                    text = "Density Heatmap",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Lighter means fewer members present, Indigo/Blue signifies max presence.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Heatmap Calendar Card (Bento Style)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    shape = RoundedCornerShape(28.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        // Month Selector Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = { navigateMonth(-1) }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                    contentDescription = "Previous Month",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }

                            Text(
                                text = monthNameFormat.format(startCal.time),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            IconButton(onClick = { navigateMonth(1) }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                    contentDescription = "Next Month",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Weekdays
                        val weekdays = listOf("S", "M", "T", "W", "T", "F", "S")
                        Row(modifier = Modifier.fillMaxWidth()) {
                            weekdays.forEach { day ->
                                Text(
                                    text = day,
                                    modifier = Modifier.weight(1f),
                                    textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Days layout
                        val totalCells = (firstDayOfWeek - 1) + maxDays
                        val rows = (totalCells + 6) / 7

                        for (r in 0 until rows) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp)
                            ) {
                                for (c in 0 until 7) {
                                    val cellIndex = r * 7 + c
                                    val dayIndex = cellIndex - (firstDayOfWeek - 2)

                                    if (dayIndex in 1..maxDays) {
                                        val dateKey = monthDaysKeys[dayIndex - 1]
                                        val dayRecords = recordsByDate[dateKey] ?: emptyList()
                                        
                                        // Calculate ratio
                                        val totalInProject = members.size
                                        val presentCount = dayRecords.count { it.isPresent }
                                        val ratio = if (totalInProject > 0) {
                                            presentCount.toFloat() / totalInProject
                                        } else 0f

                                        // Density shade color
                                        val cellColor = when {
                                            dayRecords.isEmpty() -> MaterialTheme.colorScheme.surfaceVariant
                                            ratio == 0f -> MaterialTheme.colorScheme.error.copy(alpha = 0.1f) // very light red for completely checked but empty
                                            ratio > 0f && ratio <= 0.33f -> MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                            ratio > 0.33f && ratio <= 0.66f -> MaterialTheme.colorScheme.primary.copy(alpha = 0.55f)
                                            else -> MaterialTheme.colorScheme.primary
                                        }

                                        val textColor = when {
                                            dayRecords.isNotEmpty() && ratio > 0.66f -> MaterialTheme.colorScheme.onPrimary
                                            else -> MaterialTheme.colorScheme.onSurface
                                        }

                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .aspectRatio(1f)
                                                .padding(2.dp)
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(cellColor)
                                                .testTag("heatmap_day_$dateKey"),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = dayIndex.toString(),
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.Bold,
                                                color = textColor
                                            )
                                        }
                                    } else {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Timeline headers
                Text(
                    text = "Attendance Records History",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(8.dp))

                val sortedHistoryDates = remember(recordsByDate) {
                    recordsByDate.keys.sortedDescending()
                }

                if (sortedHistoryDates.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No history available.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(bottom = 100.dp)
                    ) {
                        items(sortedHistoryDates) { dateStr ->
                            val isExpanded = expandedDateString == dateStr
                            val dateRecords = recordsByDate[dateStr] ?: emptyList()
                            val presentCount = dateRecords.count { it.isPresent }
                            
                            // Load members associated with present records on this date
                            val presentMembers = remember(dateRecords, members) {
                                dateRecords.filter { it.isPresent }.mapNotNull { rec ->
                                    members.find { it.id == rec.memberId }
                                }
                            }

                            val formattedDisplayDate = remember(dateStr) {
                                try {
                                    val parsed = dayKeyFormat.parse(dateStr)
                                    dateDisplayFormat.format(parsed!!)
                                } catch (e: Exception) {
                                    dateStr
                                }
                            }

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("overview_row_$dateStr"),
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                ),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            expandedDateString = if (isExpanded) null else dateStr
                                        }
                                        .padding(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = formattedDisplayDate,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = "$presentCount / ${members.size} members present",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }

                                        Icon(
                                            imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                            contentDescription = if (isExpanded) "Collapse" else "Expand"
                                        )
                                    }

                                    AnimatedVisibility(
                                        visible = isExpanded,
                                        enter = expandVertically() + fadeIn(),
                                        exit = shrinkVertically() + fadeOut()
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(top = 12.dp, start = 4.dp),
                                            verticalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))

                                            Text(
                                                text = "Present Members:",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )

                                            if (presentMembers.isEmpty()) {
                                                Text(
                                                    text = "No members marked present.",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                                )
                                            } else {
                                                // Minimalist scannable vertical list of names
                                                presentMembers.forEach { member ->
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                                        modifier = Modifier.padding(vertical = 4.dp)
                                                    ) {
                                                        Box(
                                                            modifier = Modifier
                                                                .size(24.dp)
                                                                .background(getAvatarColor(member.name), CircleShape),
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            Text(
                                                                text = getInitials(member.name),
                                                                color = Color.White,
                                                                style = MaterialTheme.typography.labelSmall,
                                                                fontWeight = FontWeight.Bold
                                                            )
                                                        }

                                                        Text(
                                                            text = member.name,
                                                            style = MaterialTheme.typography.bodySmall,
                                                            fontWeight = FontWeight.Medium
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
                }
            }
        }
    }
}
