package com.voxplanapp.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow


// QuotaDao.kt
@Dao
interface QuotaDao {
    @Query("SELECT * FROM Quota WHERE goalId = :goalId")
    fun getQuotaForGoal(goalId: Int): Flow<Quota?>

    @Query("SELECT * FROM Quota")
    fun getAllQuotas(): Flow<List<Quota>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuota(quota: Quota)

    @Update
    suspend fun updateQuota(quota: Quota)

    @Delete
    suspend fun deleteQuota(quota: Quota)

    @Query("DELETE FROM Quota WHERE goalId =:goalId")
    suspend fun deleteQuotaForGoal(goalId: Int)

    @Query("SELECT * FROM Quota WHERE goalId IN (:goalIds)")
    fun getQuotasForGoals(goalIds: List<Int>): Flow<List<Quota>>
}
