package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun InlineCalendar(
    currentMonth: Calendar,
    selectedDateString: String,
    onNavigateMonth: (Int) -> Unit,
    onDateSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val monthFormat = SimpleDateFormat("MMMM yyyy", Locale.US)
    val dayKeyFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    // Calculate dates
    val monthCal = currentMonth.clone() as Calendar
    monthCal.set(Calendar.DAY_OF_MONTH, 1)
    val firstDayOfWeek = monthCal.get(Calendar.DAY_OF_WEEK) // 1 = Sunday, 2 = Monday...
    val maxDays = monthCal.getActualMaximum(Calendar.DAY_OF_MONTH)

    val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Calendar.getInstance().time)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(28.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Month Heading Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { onNavigateMonth(-1) },
                    modifier = Modifier.testTag("prev_month_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = "Previous Month",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                Text(
                    text = monthFormat.format(monthCal.time),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.testTag("month_label")
                )

                IconButton(
                    onClick = { onNavigateMonth(1) },
                    modifier = Modifier.testTag("next_month_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "Next Month",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Weekday Headers
            val weekdays = listOf("S", "M", "T", "W", "T", "F", "S")
            Row(modifier = Modifier.fillMaxWidth()) {
                weekdays.forEach { day ->
                    Text(
                        text = day,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Grid Rows
            val totalCells = (firstDayOfWeek - 1) + maxDays
            val rows = (totalCells + 6) / 7

            for (r in 0 until rows) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    for (c in 0 until 7) {
                        val cellIndex = r * 7 + c
                        val dayNumber = cellIndex - (firstDayOfWeek - 2)

                        if (dayNumber in 1..maxDays) {
                            val tempCal = monthCal.clone() as Calendar
                            tempCal.set(Calendar.DAY_OF_MONTH, dayNumber)
                            val dateStr = dayKeyFormat.format(tempCal.time)
                            val isSelected = dateStr == selectedDateString
                            val isToday = dateStr == todayStr

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .padding(2.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        color = when {
                                            isSelected -> MaterialTheme.colorScheme.primary
                                            else -> Color.Transparent
                                        },
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable { onDateSelected(dateStr) }
                                    .testTag("calendar_day_$dateStr"),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = dayNumber.toString(),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                                        color = when {
                                            isSelected -> MaterialTheme.colorScheme.onPrimary
                                            isToday -> MaterialTheme.colorScheme.primary
                                            else -> MaterialTheme.colorScheme.onSurface
                                        }
                                    )
                                    if (isToday && !isSelected) {
                                        Box(
                                            modifier = Modifier
                                                .size(4.dp)
                                                .background(MaterialTheme.colorScheme.primary, CircleShape)
                                        )
                                    }
                                }
                            }
                        } else {
                            // Empty Spacer cell
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}
