package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

data class ProjectWithMemberCount(
    val id: Int,
    val name: String,
    val description: String,
    val memberCount: Int
)

@Dao
interface ProjectDao {
    @Query("""
        SELECT p.id, p.name, p.description, COUNT(m.id) as memberCount 
        FROM projects p 
        LEFT JOIN members m ON p.id = m.projectId 
        GROUP BY p.id
        ORDER BY p.id DESC
    """)
    fun getProjectsWithMemberCount(): Flow<List<ProjectWithMemberCount>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: Project): Long

    @Delete
    suspend fun deleteProject(project: Project)
}

@Dao
interface MemberDao {
    @Query("SELECT * FROM members WHERE projectId = :projectId ORDER BY name ASC")
    fun getMembersForProject(projectId: Int): Flow<List<Member>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMember(member: Member): Long

    @Delete
    suspend fun deleteMember(member: Member)
}

@Dao
interface AttendanceRecordDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecords(records: List<AttendanceRecord>)

    @Query("""
        SELECT r.* FROM attendance_records r 
        INNER JOIN members m ON r.memberId = m.id 
        WHERE m.projectId = :projectId AND r.dateString = :dateString
    """)
    fun getAttendanceForProjectAndDate(projectId: Int, dateString: String): Flow<List<AttendanceRecord>>

    @Query("SELECT * FROM attendance_records WHERE memberId = :memberId ORDER BY dateString DESC")
    fun getAttendanceForMember(memberId: Int): Flow<List<AttendanceRecord>>

    @Query("""
        SELECT r.* FROM attendance_records r
        INNER JOIN members m ON r.memberId = m.id
        WHERE m.projectId = :projectId
        ORDER BY r.dateString DESC
    """)
    fun getAllAttendanceForProject(projectId: Int): Flow<List<AttendanceRecord>>
}
