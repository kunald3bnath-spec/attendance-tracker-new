package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.People
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
import com.example.ui.AttendanceViewModel
import com.example.ui.components.InlineCalendar
import com.example.ui.components.MemberAvatarCard
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarkAttendanceScreen(
    viewModel: AttendanceViewModel,
    modifier: Modifier = Modifier
) {
    val selectedProjectId by viewModel.selectedProjectId.collectAsState()
    val activeProject = viewModel.projects.collectAsState().value.find { it.id == selectedProjectId }

    val members by viewModel.membersForSelectedProject.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val inlineCalendarMonth by viewModel.inlineCalendarMonth.collectAsState()
    val uncommittedAttendance by viewModel.uncommittedAttendance.collectAsState()

    var showAddMemberDialog by remember { mutableStateOf(false) }
    var memberName by remember { mutableStateOf("") }
    var selectedColorHex by remember { mutableStateOf("#5C6BC0") } // Default Indigo
    var memberError by remember { mutableStateOf<String?>(null) }

    val avatarColors = listOf(
        "#EF5350", // Red
        "#42A5F5", // Blue
        "#66BB6A", // Green
        "#FFA726", // Orange
        "#5C6BC0", // Indigo
        "#26A69A", // Teal
        "#EC407A", // Pink
        "#AB47BC"  // Purple
    )

    // Parse date for displaying human readable
    val displayDate = remember(selectedDate) {
        try {
            val sdfStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(selectedDate)
            SimpleDateFormat("MMMM d, yyyy", Locale.US).format(sdfStr!!)
        } catch (e: Exception) {
            selectedDate
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Mark Attendance",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = activeProject?.let { "Project: ${it.name}" } ?: "No project selected",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)
                )
            )
        },
        floatingActionButton = {
            if (selectedProjectId != null) {
                FloatingActionButton(
                    onClick = {
                        memberName = ""
                        selectedColorHex = avatarColors.random()
                        memberError = null
                        showAddMemberDialog = true
                    },
                    modifier = Modifier
                        .testTag("add_member_fab")
                        .padding(bottom = 140.dp), // Height clearance above submit bar & bottom nav
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add Member")
                }
            }
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
                        imageVector = Icons.Default.People,
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
                        text = "Please go to the Projects tab and select or create a project cohort to run attendance tracking.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                ) {
                    // Custom Month Calendar
                    InlineCalendar(
                        currentMonth = inlineCalendarMonth,
                        selectedDateString = selectedDate,
                        onNavigateMonth = { viewModel.navigateInlineCalendarMonth(it) },
                        onDateSelected = { viewModel.selectDate(it) }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Date Context Heading & beautiful Bento Section Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp, horizontal = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "TEAM MEMBERS",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                letterSpacing = 1.2.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = displayDate,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Box(
                            modifier = Modifier
                                .background(
                                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "${members.size} Total",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (members.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.People,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                                )
                                Text(
                                    text = "No members in this cohort",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Tap the secondary + FAB to add new members.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                            }
                        }
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            contentPadding = PaddingValues(bottom = 150.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(members, key = { it.id }) { member ->
                                val isPresent = uncommittedAttendance[member.id] ?: false
                                MemberAvatarCard(
                                    member = member,
                                    isPresent = isPresent,
                                    onToggle = { viewModel.toggleMemberAttendance(member.id) }
                                )
                            }
                        }
                    }
                }

                // Pinned Submit bar standing elegantly above bottom nav (Bento-style deep dark contrast card)
                val presentCount = uncommittedAttendance.values.count { it }
                Card(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .padding(bottom = 72.dp), // Stand cleanly above bottom navigation bar
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF2B2930)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "$presentCount Marked Present",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Draft ready to save",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFFD0BCFF),
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Button(
                            onClick = { viewModel.submitAttendance() },
                            modifier = Modifier.testTag("submit_attendance_btn"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFD0BCFF),
                                contentColor = Color(0xFF381E72)
                            ),
                            shape = RoundedCornerShape(100),
                            enabled = members.isNotEmpty()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Done,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = Color(0xFF381E72)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("SUBMIT", fontWeight = FontWeight.Black)
                        }
                    }
                }
            }
        }
    }

    if (showAddMemberDialog) {
        AlertDialog(
            onDismissRequest = { showAddMemberDialog = false },
            title = {
                Text(
                    text = "Add Member",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedTextField(
                        value = memberName,
                        onValueChange = {
                            memberName = it
                            if (it.trim().isNotEmpty()) memberError = null
                        },
                        label = { Text("Full Name") },
                        isError = memberError != null,
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("member_name_input")
                    )

                    if (memberError != null) {
                        Text(
                            text = memberError!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    // Predefined avatar color picker
                    Text(
                        text = "Choose Profile Avatar Color",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        avatarColors.forEach { hex ->
                            val parsedColor = Color(android.graphics.Color.parseColor(hex))
                            val isSelected = selectedColorHex == hex
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(parsedColor, CircleShape)
                                    .border(
                                        width = if (isSelected) 3.dp else 0.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                        shape = CircleShape
                                    )
                                    .clip(CircleShape)
                                    .clickable { selectedColorHex = hex }
                                    .testTag("color_option_$hex")
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (memberName.trim().isEmpty()) {
                            memberError = "Member name cannot be empty"
                        } else {
                            viewModel.addMember(memberName.trim(), selectedColorHex)
                            showAddMemberDialog = false
                        }
                    },
                    modifier = Modifier.testTag("submit_member_button")
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showAddMemberDialog = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}
