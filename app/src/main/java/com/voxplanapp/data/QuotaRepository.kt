package com.voxplanapp.data

import java.time.DayOfWeek
import java.time.LocalDate


// QuotaRepository.kt
class QuotaRepository(private val quotaDao: QuotaDao) {
    fun getQuotaForGoal(goalId: Int) = quotaDao.getQuotaForGoal(goalId)

    fun getAllQuotas() = quotaDao.getAllQuotas()

    suspend fun insertQuota(quota: Quota) = quotaDao.insertQuota(quota)

    suspend fun updateQuota(quota: Quota) = quotaDao.updateQuota(quota)

    suspend fun deleteQuota(quota: Quota) = quotaDao.deleteQuota(quota)

    suspend fun deleteQuotaForGoal(goalId: Int) = quotaDao.deleteQuotaForGoal(goalId)

    fun getQuotasForGoals(goalIds: List<Int>) = quotaDao.getQuotasForGoals(goalIds)

    fun isQuotaActiveForDate(quota: Quota, date: LocalDate): Boolean {
        val dayIndex = date.dayOfWeek.value - 1 // 0-6 for Mon-Sun
        return quota.activeDays[dayIndex] == '1'
    }

    fun getActiveDays(quota: Quota): List<DayOfWeek> {
        return quota.activeDays.mapIndexedNotNull { index, active ->
            if (active == '1') DayOfWeek.of(index + 1) else null
        }
    }
}