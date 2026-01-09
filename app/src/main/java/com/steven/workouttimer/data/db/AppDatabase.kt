package com.steven.workouttimer.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [TimerEntity::class],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun timerDao(): TimerDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE timers ADD COLUMN initialCountdownSeconds INTEGER NOT NULL DEFAULT 0")
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Add timer mode column - default to WEIGHTLIFT for existing timers
                db.execSQL("ALTER TABLE timers ADD COLUMN timerMode TEXT NOT NULL DEFAULT 'WEIGHTLIFT'")
                // Add climbing mode specific columns
                db.execSQL("ALTER TABLE timers ADD COLUMN holdSeconds INTEGER NOT NULL DEFAULT 7")
                db.execSQL("ALTER TABLE timers ADD COLUMN restSeconds INTEGER NOT NULL DEFAULT 3")
                db.execSQL("ALTER TABLE timers ADD COLUMN totalRepetitions INTEGER NOT NULL DEFAULT 6")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "workout_timer_database"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
