package com.example.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "projects")
data class Project(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val description: String = ""
)

@Entity(
    tableName = "members",
    foreignKeys = [
        ForeignKey(
            entity = Project::class,
            parentColumns = ["id"],
            childColumns = ["projectId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["projectId"])]
)
data class Member(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val projectId: Int,
    val name: String,
    val avatarColorHex: String
)

@Entity(
    tableName = "attendance_records",
    foreignKeys = [
        ForeignKey(
            entity = Member::class,
            parentColumns = ["id"],
            childColumns = ["memberId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["memberId"]),
        Index(value = ["memberId", "dateString"], unique = true)
    ]
)
data class AttendanceRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val memberId: Int,
    val dateString: String, // "yyyy-MM-dd"
    val isPresent: Boolean
)
