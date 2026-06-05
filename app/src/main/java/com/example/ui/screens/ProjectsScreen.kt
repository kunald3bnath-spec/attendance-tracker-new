package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.Project
import com.example.data.ProjectWithMemberCount
import com.example.ui.AttendanceViewModel
import com.example.ui.components.AgentMarker

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectsScreen(
    viewModel: AttendanceViewModel,
    modifier: Modifier = Modifier
) {
    val projects by viewModel.projects.collectAsState()
    val selectedProjectId by viewModel.selectedProjectId.collectAsState()
    val isDarkTheme by viewModel.isDarkTheme.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var projectName by remember { mutableStateOf("") }
    var projectDesc by remember { mutableStateOf("") }
    var inputError by remember { mutableStateOf<String?>(null) }

    var searchQuery by remember { mutableStateOf("") }
    val filteredProjects = remember(projects, searchQuery) {
        if (searchQuery.isBlank()) {
            projects
        } else {
            projects.filter { it.name.contains(searchQuery, ignoreCase = true) }
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
                            text = "Attendance Tracker",
                            fontWeight = FontWeight.Black
                        )
                        AgentMarker()
                    }
                },
                actions = {
                    isDarkTheme?.let { dark ->
                        IconButton(
                            onClick = { viewModel.toggleTheme(!dark) },
                            modifier = Modifier.testTag("theme_toggle")
                        ) {
                            Icon(
                                imageVector = if (dark) Icons.Outlined.LightMode else Icons.Outlined.DarkMode,
                                contentDescription = "Toggle Theme"
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    projectName = ""
                    projectDesc = ""
                    inputError = null
                    showAddDialog = true
                },
                modifier = Modifier
                    .testTag("add_project_fab")
                    .padding(bottom = 80.dp), // Height clearance for bottom navigation
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Project")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Projects",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Create and manage your class cohorts or project workspaces.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (projects.isEmpty()) {
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
                            imageVector = Icons.Default.Folder,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                        )
                        Text(
                            text = "No projects found",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Tap the + button to create a workspace.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            } else {
                // Elegant Material 3 Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search projects...") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search"
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear search"
                                )
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("project_search_input"),
                    shape = RoundedCornerShape(28.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (filteredProjects.isEmpty()) {
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
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                            )
                            Text(
                                text = "No matching projects",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Try typing a different word or name.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 100.dp)
                    ) {
                        items(filteredProjects, key = { it.id }) { item ->
                            val isSelected = selectedProjectId == item.id
                            ProjectRowItem(
                                project = item,
                                isSelected = isSelected,
                                onSelect = { viewModel.selectProject(item.id) },
                                onDelete = {
                                    viewModel.deleteProject(
                                        Project(
                                            id = item.id,
                                            name = item.name,
                                            description = item.description
                                        )
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = {
                Text(
                    text = "New Project",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = projectName,
                        onValueChange = {
                            projectName = it
                            if (it.trim().isNotEmpty()) inputError = null
                        },
                        label = { Text("Project Name") },
                        isError = inputError != null,
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("project_name_input")
                    )

                    if (inputError != null) {
                        Text(
                            text = inputError!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    OutlinedTextField(
                        value = projectDesc,
                        onValueChange = { projectDesc = it },
                        label = { Text("Description (Optional)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("project_desc_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (projectName.trim().isEmpty()) {
                            inputError = "Project name cannot be empty"
                        } else {
                            viewModel.addProject(projectName.trim(), projectDesc.trim())
                            showAddDialog = false
                        }
                    },
                    modifier = Modifier.testTag("submit_project_button")
                ) {
                    Text("Create")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showAddDialog = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun ProjectRowItem(
    project: ProjectWithMemberCount,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .clickable { onSelect() }
            .testTag("project_card_${project.id}"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = if (isSelected) {
            BorderStroke(2.5.dp, MaterialTheme.colorScheme.primary)
        } else {
            BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f))
        },
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 3.dp else 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        },
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Folder,
                    contentDescription = null,
                    tint = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        MaterialTheme.colorScheme.primary
                    }
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = project.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Active Workspace",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                if (project.description.isNotEmpty()) {
                    Text(
                        text = project.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "${project.memberCount} " + if (project.memberCount == 1) "member" else "members",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = onDelete,
                modifier = Modifier.testTag("delete_project_${project.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Workspace",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                )
            }
        }
    }
}
