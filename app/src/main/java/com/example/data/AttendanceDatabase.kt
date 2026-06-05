package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [Project::class, Member::class, AttendanceRecord::class],
    version = 1,
    exportSchema = false
)
abstract class AttendanceDatabase : RoomDatabase() {
    abstract fun projectDao(): ProjectDao
    abstract fun memberDao(): MemberDao
    abstract fun attendanceRecordDao(): AttendanceRecordDao

    companion object {
        @Volatile
        private var INSTANCE: AttendanceDatabase? = null

        fun getDatabase(context: Context): AttendanceDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AttendanceDatabase::class.java,
                    "attendance_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
