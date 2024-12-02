package com.voxplanapp.data

import androidx.room.TypeConverter
import java.time.LocalDate
import java.time.LocalTime


class Converters {
    // For LocalTime
    @TypeConverter
    fun fromTimeString(value: String?): LocalTime? = value?.let { LocalTime.parse(it) }

    @TypeConverter
    fun timeToString(time: LocalTime?): String? = time?.toString()

    // For LocalDate
    @TypeConverter
    fun fromDateLong(value: Long?): LocalDate? = value?.let { LocalDate.ofEpochDay(it) }

    @TypeConverter
    fun dateToLong(date: LocalDate?): Long? = date?.toEpochDay()
}
