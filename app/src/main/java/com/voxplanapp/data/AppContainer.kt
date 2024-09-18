package com.voxplanapp.data

import android.content.Context
import android.util.Log
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

interface AppContainer {
    val database: AppDatabase
    val todoRepository: TodoRepository
    val eventRepository: EventRepository
    val timeBankRepository: TimeBankRepository
}

class AppDataContainer(private val context: Context) : AppContainer {

    override val database: AppDatabase by lazy {
        try {
            Room
                .databaseBuilder(context, AppDatabase::class.java, "todo-db")
                .addMigrations(
                    AppDatabase.MIGRATION_2_3, AppDatabase.MIGRATION_3_4,
                    AppDatabase.MIGRATION_4_5, AppDatabase.MIGRATION_5_6,
                    AppDatabase.MIGRATION_6_7, AppDatabase.MIGRATION_7_8
                    )
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        Log.d("Appdatabase", "database created")
                    }

                    override fun onOpen(db: SupportSQLiteDatabase) {
                        super.onOpen(db)
                        Log.d("Appdatabase", "database opened")
                    }
                })
                .build()
        } catch (e: Exception) {
            Log.e("Appdatabase", "Error Building database")
            throw e
        }
    }

    init {
        try {
            val version = database.openHelper.readableDatabase.version
            Log.d("AppDatabase", "Current database version: $version")
        } catch (e: Exception) {
            Log.e("Appdatabase", "Error getting version", e)
        }
    }

    override val todoRepository: TodoRepository by lazy {
        TodoRepository(database.todoDao())
    }
    override val eventRepository: EventRepository by lazy {
        EventRepository(database.eventDao())
    }
    override val timeBankRepository: TimeBankRepository by lazy {
        TimeBankRepository(database.timeBankDao())
    }
}
