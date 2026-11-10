package com.chart5893.sprinter.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "measurement_entries")
data class MeasurementEntryEntity(
    @PrimaryKey val id: String,
    val type: String,
    val value: Double,
    val date: String
)
