package com.voxplanapp.data

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * FocusSession - Persists FocusMode state across process death
 * Only ONE active session exists at a time (id = 1)
 */
@Entity
data class FocusSession(
    @PrimaryKey val id: Int = 1, // Always 1 - single active session
    val currentTime: Long = 0L,
    val timerState: Int = 0, // TimerState.ordinal
    val timerStarted: Boolean = false,
    val medalsValues: String = "", // Comma-separated: "30,30,60"
    val medalsTypes: String = "", // Comma-separated ordinals: "0,0,1"
    val clockFaceMins: Float = 30f,
    val isDiscreteMode: Boolean = false,
    val discreteTaskLevel: Int = 0, // DiscreteTaskLevel.ordinal
    val lastUpdated: Long = System.currentTimeMillis()
)

@Dao
interface FocusSessionDao {
    @Query("SELECT * FROM FocusSession WHERE id = 1")
    fun getActiveSession(): Flow<FocusSession?>

    @Query("SELECT * FROM FocusSession WHERE id = 1")
    suspend fun getActiveSessionSnapshot(): FocusSession?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSession(session: FocusSession)

    @Query("DELETE FROM FocusSession WHERE id = 1")
    suspend fun clearActiveSession()
}

class FocusSessionRepository(private val focusSessionDao: FocusSessionDao) {
    fun getActiveSession(): Flow<FocusSession?> = focusSessionDao.getActiveSession()

    suspend fun getActiveSessionSnapshot(): FocusSession? = focusSessionDao.getActiveSessionSnapshot()

    suspend fun saveSession(session: FocusSession) {
        focusSessionDao.saveSession(session)
    }

    suspend fun clearActiveSession() {
        focusSessionDao.clearActiveSession()
    }
}
