package com.voxplanapp.data

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Entity
data class TimeBank(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "goal_id") val goalId: Int,
    val date: LocalDate,
    val duration: Int
)

@Dao
interface TimeBankDao {
    @Insert
    suspend fun insert(entry: TimeBank)

    @Query("SELECT * FROM TimeBank WHERE goal_id = :goalId")
    fun getEntriesForGoal(goalId: Int): Flow<List<TimeBank>>

    @Query("SELECT * FROM TimeBank WHERE date = :date")
    fun getEntriesForDate(date: LocalDate): Flow<List<TimeBank>>

    @Query("SELECT SUM(duration) FROM TimeBank WHERE goal_id = :goalId")
    fun getTotalTimeForGoal(goalId: Int): Flow<Int?>

    @Query("SELECT SUM(duration) FROM TimeBank WHERE date = :date")
    fun getTotalTimeForDate(date: LocalDate): Flow<Int?>

    @Query("DELETE FROM TimeBank WHERE goal_id =:goalId AND duration = :bonusAmount")
    suspend fun deleteCompletionBonus(goalId: Int, bonusAmount: Int)
}

class TimeBankRepository(private val timeBankDao: TimeBankDao) {
    suspend fun addTimeBankEntry(goalId: Int, duration: Int) {
        timeBankDao.insert(TimeBank(goalId = goalId, date = LocalDate.now(), duration = duration))
    }

    suspend fun deleteCompletionBonus(goalId: Int, bonusAmount: Int) {
        timeBankDao.deleteCompletionBonus(goalId, bonusAmount)
    }

    fun getEntriesForGoal(goal: Int) = timeBankDao.getEntriesForGoal(goal)

    fun getEntriesForDate(date: LocalDate) = timeBankDao.getEntriesForDate(date)

    fun getTotalTimeForGoal(goalId: Int) = timeBankDao.getTotalTimeForGoal(goalId)

    fun getTotalTimeForDate(date: LocalDate) = timeBankDao.getTotalTimeForDate(date)

}
