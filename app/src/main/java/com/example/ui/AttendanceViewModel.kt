package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalCoroutinesApi::class)
class AttendanceViewModel(
    application: Application,
    private val repository: AttendanceRepository,
    private val themePreferences: ThemePreferences
) : AndroidViewModel(application) {

    // --- Theme Settings ---
    val isDarkTheme = themePreferences.isDarkTheme.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    fun toggleTheme(enabled: Boolean) {
        viewModelScope.launch {
            themePreferences.setDarkTheme(enabled)
        }
    }

    // --- Onboarding Settings ---
    val isOnboardingCompleted = themePreferences.isOnboardingCompleted.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    fun completeOnboarding() {
        viewModelScope.launch {
            themePreferences.setOnboardingCompleted(true)
        }
    }

    fun resetOnboarding() {
        viewModelScope.launch {
            themePreferences.setOnboardingCompleted(false)
        }
    }

    // --- Projects ---
    val projects = repository.projectsWithMemberCount.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _selectedProjectId = MutableStateFlow<Int?>(null)
    val selectedProjectId: StateFlow<Int?> = _selectedProjectId.asStateFlow()

    fun selectProject(projectId: Int?) {
        _selectedProjectId.value = projectId
        projectId?.let { id ->
            // Reset selected member for history when changing projects
            viewModelScope.launch {
                val projectMembers = repository.getMembersForProject(id).firstOrNull() ?: emptyList()
                if (projectMembers.isNotEmpty()) {
                    _selectedMemberId.value = projectMembers.first().id
                } else {
                    _selectedMemberId.value = null
                }
            }
        }
    }

    fun addProject(name: String, description: String = "") {
        viewModelScope.launch {
            val id = repository.insertProject(Project(name = name, description = description))
            if (_selectedProjectId.value == null) {
                selectProject(id.toInt())
            }
        }
    }

    fun deleteProject(project: Project) {
        viewModelScope.launch {
            repository.deleteProject(project)
            if (_selectedProjectId.value == project.id) {
                val remaining = projects.value.filter { it.id != project.id }
                if (remaining.isNotEmpty()) {
                    selectProject(remaining.first().id)
                } else {
                    _selectedProjectId.value = null
                }
            }
        }
    }

    // --- Members ---
    val membersForSelectedProject: StateFlow<List<Member>> = _selectedProjectId
        .flatMapLatest { projectId ->
            if (projectId != null) {
                repository.getMembersForProject(projectId)
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addMember(name: String, avatarColorHex: String) {
        val projectId = _selectedProjectId.value ?: return
        viewModelScope.launch {
            repository.insertMember(
                Member(
                    projectId = projectId,
                    name = name,
                    avatarColorHex = avatarColorHex
                )
            )
        }
    }

    fun deleteMember(member: Member) {
        viewModelScope.launch {
            repository.deleteMember(member)
        }
    }

    // --- Calendar & Mark Attendance ---
    private val _selectedDate = MutableStateFlow(getTodayDateString())
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    // Represents the month currently viewed on the inline calendar
    private val _inlineCalendarMonth = MutableStateFlow(Calendar.getInstance())
    val inlineCalendarMonth: StateFlow<Calendar> = _inlineCalendarMonth.asStateFlow()

    fun navigateInlineCalendarMonth(delta: Int) {
        val cal = _inlineCalendarMonth.value.clone() as Calendar
        cal.add(Calendar.MONTH, delta)
        _inlineCalendarMonth.value = cal
    }

    fun selectDate(dateString: String) {
        _selectedDate.value = dateString
    }

    // Local uncommitted attendance state: memberId -> isPresent
    private val _uncommittedAttendance = MutableStateFlow<Map<Int, Boolean>>(emptyMap())
    val uncommittedAttendance: StateFlow<Map<Int, Boolean>> = _uncommittedAttendance.asStateFlow()

    // Combined or fetched state: list of saved records for the current date
    init {
        // Automatically load saved attendance when selectedDate or selectedProjectId changes
        combine(selectedProjectId, selectedDate) { projectId, dateString ->
            Pair(projectId, dateString)
        }.collectInScope { pair ->
            val projectId = pair.first
            val dateString = pair.second
            if (projectId != null) {
                // Fetch members and historical attendance in a single go
                val members = repository.getMembersForProject(projectId).firstOrNull() ?: emptyList()
                val savedRecords = repository.getAttendanceForProjectAndDate(projectId, dateString).firstOrNull() ?: emptyList()
                
                val savedMap = savedRecords.associate { it.memberId to it.isPresent }
                val initialMap = members.associate { member ->
                    member.id to (savedMap[member.id] ?: false)
                }
                _uncommittedAttendance.value = initialMap
            } else {
                _uncommittedAttendance.value = emptyMap()
            }
        }

        // Also update default project selection when project list is first populated
        viewModelScope.launch {
            projects.collect { list ->
                if (_selectedProjectId.value == null && list.isNotEmpty()) {
                    selectProject(list.first().id)
                }
            }
        }
    }

    fun toggleMemberAttendance(memberId: Int) {
        val current = _uncommittedAttendance.value
        val isPresent = current[memberId] ?: false
        _uncommittedAttendance.value = current + (memberId to !isPresent)
    }

    fun submitAttendance() {
        val projectId = _selectedProjectId.value ?: return
        val dateString = _selectedDate.value
        viewModelScope.launch {
            val records = _uncommittedAttendance.value.map { (memberId, isPresent) ->
                AttendanceRecord(
                    memberId = memberId,
                    dateString = dateString,
                    isPresent = isPresent
                )
            }
            repository.submitRecords(records)
        }
    }

    // --- History Screen ---
    private val _selectedMemberId = MutableStateFlow<Int?>(null)
    val selectedMemberId: StateFlow<Int?> = _selectedMemberId.asStateFlow()

    fun selectMember(memberId: Int?) {
        _selectedMemberId.value = memberId
    }

    val selectedMemberHistory: StateFlow<List<AttendanceRecord>> = _selectedMemberId
        .flatMapLatest { memberId ->
            if (memberId != null) {
                repository.getAttendanceForMember(memberId)
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // --- Overview Screen ---
    // List of records for the active project
    val projectAllAttendance: StateFlow<List<AttendanceRecord>> = _selectedProjectId
        .flatMapLatest { projectId ->
            if (projectId != null) {
                repository.getAllAttendanceForProject(projectId)
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Helper functions
    private fun <T> Flow<T>.collectInScope(action: suspend (T) -> Unit) {
        viewModelScope.launch {
            collect { action(it) }
        }
    }

    companion object {
        fun getTodayDateString(): String {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            return sdf.format(Calendar.getInstance().time)
        }
    }
}

class AttendanceViewModelFactory(
    private val application: Application,
    private val repository: AttendanceRepository,
    private val themePreferences: ThemePreferences
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AttendanceViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AttendanceViewModel(application, repository, themePreferences) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
