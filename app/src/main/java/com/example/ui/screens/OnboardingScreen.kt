package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.example.ui.components.getAvatarColor
import com.example.ui.components.getInitials

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun OnboardingScreen(
    viewModel: AttendanceViewModel,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var currentStep by remember { mutableStateOf(0) }
    val stepsCount = 4

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            Surface(
                tonalElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 24.dp)
                        .navigationBarsPadding(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Skip or Back Button
                    if (currentStep > 0) {
                        TextButton(
                            onClick = { currentStep-- },
                            modifier = Modifier.testTag("onboarding_back_btn")
                        ) {
                            Text(
                                "BACK",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        TextButton(
                            onClick = { onDismiss() },
                            modifier = Modifier.testTag("onboarding_skip_btn")
                        ) {
                            Text(
                                "SKIP",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        }
                    }

                    // Dots Indicators
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        for (i in 0 until stepsCount) {
                            val active = i == currentStep
                            Box(
                                modifier = Modifier
                                    .size(if (active) 12.dp else 8.dp)
                                    .background(
                                        color = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                                        shape = CircleShape
                                    )
                            )
                        }
                    }

                    // Next / Get Started Button
                    Button(
                        onClick = {
                            if (currentStep < stepsCount - 1) {
                                currentStep++
                            } else {
                                onDismiss()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier.testTag("onboarding_next_btn")
                    ) {
                        Text(
                            text = if (currentStep == stepsCount - 1) "GET STARTED" else "NEXT",
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        AnimatedContent(
            targetState = currentStep,
            transitionSpec = {
                if (targetState > initialState) {
                    slideInHorizontally { width -> width } + fadeIn() togetherWith
                            slideOutHorizontally { width -> -width } + fadeOut()
                } else {
                    slideInHorizontally { width -> -width } + fadeIn() togetherWith
                            slideOutHorizontally { width -> width } + fadeOut()
                }.using(
                    SizeTransform(clip = false)
                )
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background),
            label = "onboarding_slider"
        ) { step ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // Heading & Description Zone
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = when (step) {
                            0 -> "Create Projects"
                            1 -> "Add Members"
                            2 -> "Mark Attendance"
                            else -> "Analyze Insights"
                        },
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = when (step) {
                            0 -> "Separate your classrooms, groups, or projects into custom workspaces. Tap '+' to create."
                            1 -> "Name your student roster or member list. Every member gets a beautiful unique circular avatar."
                            2 -> "Use the inline calendar to select dates, and tap the cards to mark members Present or Absent."
                            else -> "Generate instant metrics! Scan monthly heatmaps, history records, and individual statistics."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                }

                Spacer(modifier = Modifier.weight(0.1f))

                // High fidelity bento mockup card
                Card(
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        when (step) {
                            0 -> CreateProjectMockup()
                            1 -> AddMembersMockup()
                            2 -> MarkAttendanceMockup()
                            else -> AnalyzeInsightsMockup()
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(0.1f))
            }
        }
    }
}

@Composable
fun CreateProjectMockup() {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            // Simulated Project Row
            Card(
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                RoundedCornerShape(8.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Folder,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            "Software Engineering Cohort",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Class of 2026 - MWF Mornings",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Simulated Circular Add FAB
            Box(
                modifier = Modifier
                    .align(Alignment.End)
                    .size(48.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }

        // Floating glowing tooltip overlaid
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF2B2930)),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(0.9f)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Lightbulb,
                    contentDescription = null,
                    tint = Color(0xFFD0BCFF),
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Tap the floating button (+) at the bottom right anytime to build a new cohort workspace!",
                    color = Color.White,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}

@Composable
fun AddMembersMockup() {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth().align(Alignment.TopCenter).padding(top = 8.dp)
        ) {
            // Simulated grid of circular avatars with dynamic colorful backgrounds
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                listOf("Alex Rivera", "Brenda Smith", "Chris Diaz").forEach { name ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(getAvatarColor(name), CircleShape)
                                .border(1.5.dp, Color.White, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = getInitials(name),
                                color = Color.White,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = name.split(" ")[0],
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Input field visual mockup
            Card(
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Add new member names...",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                    Icon(
                        imageVector = Icons.Default.AddCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Floating glowing tooltip overlaid
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF2B2930)),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(0.9f)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Lightbulb,
                    contentDescription = null,
                    tint = Color(0xFFD0BCFF),
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Add names inside the workspace. The app automatically computes dynamic-colored initials!",
                    color = Color.White,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}

@Composable
fun MarkAttendanceMockup() {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth().align(Alignment.TopCenter)
        ) {
            // Simulated Inline Calendar Strip
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        RoundedCornerShape(12.dp)
                    )
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                listOf("Mon 1", "Tue 2", "Wed 3", "Thu 4", "Fri 5").forEachIndexed { idx, day ->
                    val isSelected = idx == 2
                    Box(
                        modifier = Modifier
                            .background(
                                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = day,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            // Simulated Check card
            Card(
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(getAvatarColor("Alex Rivera"), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "AR",
                                color = Color.White,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Column {
                            Text("Alex Rivera", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                            Text("PRESENT", style = MaterialTheme.typography.labelSmall, color = Color(0xFF006E1C), fontWeight = FontWeight.ExtraBold)
                        }
                    }
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        // Floating glowing tooltip overlaid
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF2B2930)),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(0.9f)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Lightbulb,
                    contentDescription = null,
                    tint = Color(0xFFD0BCFF),
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Tap any grid card to flip checking states, then press SUBMIT at the bottom to lock draft logs!",
                    color = Color.White,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}

@Composable
fun AnalyzeInsightsMockup() {
    Box(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().align(Alignment.TopCenter),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Simulated Stat item 1
            Card(
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.weight(1f)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Presence", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("94.5%", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                }
            }

            // Simulated Stat item 2
            Card(
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.weight(1f)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Days Tracked", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("18", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black, color = Color(0xFF00796B))
                }
            }
        }

        // Floating glowing tooltip overlaid
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF2B2930)),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(0.9f)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Lightbulb,
                    contentDescription = null,
                    tint = Color(0xFFD0BCFF),
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Browse the History and Overview tabs to check overall trends, logs, and heatmap colors instantly!",
                    color = Color.White,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}
