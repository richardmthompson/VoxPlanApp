package com.voxplanapp.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@TypeConverters(Converters::class)
@Database(entities = [TodoItem::class, Event::class, TimeBank::class, Quota::class, FocusSession::class], version = 14)
abstract class AppDatabase : RoomDatabase() {
    abstract fun todoDao(): TodoDao
    abstract fun eventDao(): EventDao
    abstract fun timeBankDao(): TimeBankDao
    abstract fun quotaDao(): QuotaDao
    abstract fun focusSessionDao(): FocusSessionDao

    companion object {
        val MIGRATION_2_3 = object : Migration(2,3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE TodoItem ADD COLUMN 'order' INTEGER NOT NULL DEFAULT 0")
            }
        }

        val MIGRATION_3_4 = object : Migration(3,4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE TodoItem ADD COLUMN notes TEXT")
            }
        }

        val MIGRATION_4_5 = object : Migration(4,5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS Event (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        goalId INTEGER NOT NULL,
                        title TEXT NOT NULL,
                        startTime TEXT NOT NULL,
                        endTime TEXT NOT NULL,
                        startDate INTEGER NOT NULL,
                        recurrenceType TEXT NOT NULL,
                        recurrenceInterval INTEGER,
                        recurrenceEndDate INTEGER,
                        color INTEGER
                    )
                """)
                database.execSQL("ALTER TABLE TodoItem ADD COLUMN preferredTime TEXT")
                database.execSQL("ALTER TABLE TodoItem ADD COLUMN estDurationMins INTEGER")
                database.execSQL("ALTER TABLE TodoItem ADD COLUMN frequency TEXT NOT NULL DEFAULT 'NONE'")
            }
        }

        val MIGRATION_5_6 = object : Migration(5,6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE TodoItem ADD COLUMN expanded INTEGER NOT NULL DEFAULT 1")
            }
        }

        val MIGRATION_6_7 = object : Migration(6,7) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                   CREATE TABLE IF NOT EXISTS TimeBank (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    goal_id INTEGER NOT NULL,
                    date INTEGER NOT NULL,
                    duration INTEGER NOT NULL
                    )
                """)
            }
        }
        val MIGRATION_7_8 = object : Migration(7,8) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                   CREATE TABLE IF NOT EXISTS TimeBank (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    goal_id INTEGER NOT NULL,
                    date INTEGER NOT NULL,
                    duration INTEGER NOT NULL
                    )
                """)
            }
        }

        val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add the new column
                database.execSQL("ALTER TABLE TodoItem ADD COLUMN completedDate INTEGER")
            }
        }

        val MIGRATION_9_10 = object : Migration(9,10) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS Quota (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    goalId INTEGER NOT NULL,
                    dailyMinutes INTEGER NOT NULL,
                    activeDays TEXT NOT NULL,
                    FOREIGN KEY (goalId) REFERENCES TodoItem(id) ON DELETE CASCADE
                    )
                """)
            }
        }

        val MIGRATION_10_11 = object : Migration(10, 11) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Make startTime and endTime nullable
                database.execSQL("""CREATE TABLE IF NOT EXISTS Event_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    goalId INTEGER NOT NULL,
                    title TEXT NOT NULL,
                    startTime TEXT,
                    endTime TEXT,
                    startDate INTEGER NOT NULL,
                    recurrenceType TEXT NOT NULL,
                    recurrenceInterval INTEGER,
                    recurrenceEndDate INTEGER,
                    color INTEGER,
                    scheduled INTEGER NOT NULL DEFAULT 0,
                    `order` INTEGER NOT NULL DEFAULT 0
                )""")
                database.execSQL("INSERT INTO Event_new SELECT *, 0, 0 FROM Event")
                database.execSQL("DROP TABLE Event")
                database.execSQL("ALTER TABLE Event_new RENAME TO Event")
            }
        }

        val MIGRATION_11_12 = object : Migration(11, 12) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
            ALTER TABLE Event 
            ADD COLUMN quotaDuration INTEGER
        """)
                database.execSQL("""
            ALTER TABLE Event 
            ADD COLUMN scheduledDuration INTEGER
        """)
                database.execSQL("""
            ALTER TABLE Event 
            ADD COLUMN completedDuration INTEGER
        """)
            }
        }

        val MIGRATION_12_13 = object : Migration(12, 13) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add new column
                database.execSQL(
                    "ALTER TABLE Event ADD COLUMN parentDailyId INTEGER"
                )

                // Remove scheduled column
                database.execSQL("""
            CREATE TABLE IF NOT EXISTS Event_new (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                goalId INTEGER NOT NULL,
                title TEXT NOT NULL,
                startTime TEXT,
                endTime TEXT,
                startDate INTEGER NOT NULL,
                recurrenceType TEXT NOT NULL,
                recurrenceInterval INTEGER,
                recurrenceEndDate INTEGER,
                color INTEGER,
                `order` INTEGER NOT NULL DEFAULT 0,
                quotaDuration INTEGER,
                scheduledDuration INTEGER,
                completedDuration INTEGER,
                parentDailyId INTEGER
            )
        """)

                // Copy data, excluding scheduled column
                database.execSQL("""
            INSERT INTO Event_new
            SELECT id, goalId, title, startTime, endTime, startDate,
                   recurrenceType, recurrenceInterval, recurrenceEndDate,
                   color, `order`, quotaDuration, scheduledDuration,
                   completedDuration, NULL
            FROM Event
        """)

                // Drop old table and rename new one
                database.execSQL("DROP TABLE Event")
                database.execSQL("ALTER TABLE Event_new RENAME TO Event")
            }
        }

        val MIGRATION_13_14 = object : Migration(13, 14) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Create FocusSession table for persisting focus mode state across process death
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS FocusSession (
                        id INTEGER PRIMARY KEY NOT NULL,
                        currentTime INTEGER NOT NULL,
                        timerState INTEGER NOT NULL,
                        timerStarted INTEGER NOT NULL,
                        medalsValues TEXT NOT NULL,
                        medalsTypes TEXT NOT NULL,
                        clockFaceMins REAL NOT NULL,
                        isDiscreteMode INTEGER NOT NULL,
                        discreteTaskLevel INTEGER NOT NULL,
                        lastUpdated INTEGER NOT NULL
                    )
                """)
            }
        }

    }
}
