package com.example.data

import kotlinx.coroutines.flow.Flow

class AttendanceRepository(private val database: AttendanceDatabase) {
    val projectDao = database.projectDao()
    val memberDao = database.memberDao()
    val attendanceRecordDao = database.attendanceRecordDao()

    val projectsWithMemberCount: Flow<List<ProjectWithMemberCount>> =
        projectDao.getProjectsWithMemberCount()

    suspend fun insertProject(project: Project): Long {
        return projectDao.insertProject(project)
    }

    suspend fun deleteProject(project: Project) {
        projectDao.deleteProject(project)
    }

    fun getMembersForProject(projectId: Int): Flow<List<Member>> {
        return memberDao.getMembersForProject(projectId)
    }

    suspend fun insertMember(member: Member): Long {
        return memberDao.insertMember(member)
    }

    suspend fun deleteMember(member: Member) {
        memberDao.deleteMember(member)
    }

    suspend fun submitRecords(records: List<AttendanceRecord>) {
        attendanceRecordDao.insertRecords(records)
    }

    fun getAttendanceForProjectAndDate(projectId: Int, dateString: String): Flow<List<AttendanceRecord>> {
        return attendanceRecordDao.getAttendanceForProjectAndDate(projectId, dateString)
    }

    fun getAttendanceForMember(memberId: Int): Flow<List<AttendanceRecord>> {
        return attendanceRecordDao.getAttendanceForMember(memberId)
    }

    fun getAllAttendanceForProject(projectId: Int): Flow<List<AttendanceRecord>> {
        return attendanceRecordDao.getAllAttendanceForProject(projectId)
    }
}
