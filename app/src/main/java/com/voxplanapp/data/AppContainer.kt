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
}

class AppDataContainer(private val context: Context) : AppContainer {

    override val database: AppDatabase by lazy {
        try {
            Room
                .databaseBuilder(context, AppDatabase::class.java, "todo-db")
                .addMigrations(AppDatabase.MIGRATION_2_3, AppDatabase.MIGRATION_3_4)
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
            Log.e("Appdatabase", "FUKKKKKKKED getting version")
        }
    }

    override val todoRepository: TodoRepository by lazy {
        TodoRepository(database.todoDao())
    }

}