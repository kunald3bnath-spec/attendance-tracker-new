package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.Member

@Composable
fun MemberAvatarCard(
    member: Member,
    isPresent: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val avatarBgColor = getAvatarColor(member.name)

    val initials = getInitials(member.name)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("member_card_${member.id}")
            .clip(RoundedCornerShape(24.dp))
            .clickable { onToggle() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = if (isPresent) {
            BorderStroke(2.5.dp, MaterialTheme.colorScheme.primary)
        } else {
            BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f))
        },
        elevation = CardDefaults.cardElevation(defaultElevation = if (isPresent) 3.dp else 1.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp, horizontal = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Avatar Circle with clean double border
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .background(avatarBgColor, CircleShape)
                        .padding(2.dp)
                        .then(
                            if (isPresent) Modifier.border(2.dp, MaterialTheme.colorScheme.primary, CircleShape) else Modifier
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(avatarBgColor, CircleShape)
                            .border(2.dp, Color.White, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = initials,
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Member Name
                Text(
                    text = member.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)
                )

                // Label Chip Badge
                Box(
                    modifier = Modifier
                        .background(
                            color = if (isPresent) Color(0xFFC4EED0) else MaterialTheme.colorScheme.outline.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(10.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = if (isPresent) "PRESENT" else "ABSENT",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isPresent) Color(0xFF006E1C) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                }
            }

            // Top-right indicator check mark if checked
            if (isPresent) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(18.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Done,
                        contentDescription = "Selected",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }
    }
}

fun getAvatarColor(name: String): Color {
    val bentoColors = listOf(
        Color(0xFF6750A4), // Deep Purple/Indigo
        Color(0xFF00796B), // Teal
        Color(0xFF1976D2), // Blue
        Color(0xFFD32F2F), // Slate Red
        Color(0xFF388E3C), // Emerald Green
        Color(0xFFF57C00), // Dark Orange
        Color(0xFFE91E63), // Pink
        Color(0xFF7B1FA2), // Purple/Violet
        Color(0xFF455A64)  // Blue Grey
    )
    val trimmed = name.trim()
    if (trimmed.isEmpty()) return bentoColors[0]
    val hash = trimmed.hashCode()
    val index = Math.abs(hash) % bentoColors.size
    return bentoColors[index]
}

fun getInitials(name: String): String {
    val trimmed = name.trim()
    if (trimmed.isEmpty()) return "?"
    val parts = trimmed.split("\\s+".toRegex())
    return if (parts.size >= 2) {
        (parts[0].take(1) + parts[parts.size - 1].take(1)).uppercase()
    } else {
        trimmed.take(1).uppercase()
    }
}
