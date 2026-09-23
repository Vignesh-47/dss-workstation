package com.dss.workstation.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.dss.workstation.data.dao.ScheduleDao
import com.dss.workstation.data.dao.TaskDao
import com.dss.workstation.data.model.*

@Database(
    entities = [
        TaskTemplateEntity::class,
        TaskStepEntity::class,
        ChecklistItemEntity::class,
        DailyScheduleEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun taskDao(): TaskDao
    abstract fun scheduleDao(): ScheduleDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "dss_workstation_db"
                )
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Seeding on first database creation
                            INSTANCE?.let { database ->
                                DatabasePrepopulator.populateInitialData(
                                    database.taskDao(),
                                    database.scheduleDao()
                                )
                            }
                        }

                        override fun onOpen(db: SupportSQLiteDatabase) {
                            super.onOpen(db)
                            // Ensure seeded data exists
                            INSTANCE?.let { database ->
                                DatabasePrepopulator.populateInitialData(
                                    database.taskDao(),
                                    database.scheduleDao()
                                )
                            }
                        }
                    })
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
