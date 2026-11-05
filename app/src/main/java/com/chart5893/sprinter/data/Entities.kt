package com.chart5893.sprinter.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "exercises")
data class ExerciseEntity(
    @PrimaryKey val id: String,
    val name: String,
    val equipment: String? = null,
    val muscleGroup: String? = null,
    val notes: String? = null
)

@Entity(tableName = "routines")
data class RoutineEntity(
    @PrimaryKey val id: String,
    val name: String,
    val createdAt: Long,
    val updatedAt: Long
)

@Entity(
    tableName = "routine_exercises",
    primaryKeys = ["routineId", "exerciseId"],
    foreignKeys = [
        ForeignKey(entity = RoutineEntity::class, parentColumns = ["id"], childColumns = ["routineId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = ExerciseEntity::class, parentColumns = ["id"], childColumns = ["exerciseId"])
    ],
    indices = [Index("exerciseId")]
)
data class RoutineExerciseEntity(
    val routineId: String,
    val exerciseId: String,
    val displayOrder: Int,
    val targetSets: Int? = null,
    val targetReps: Int? = null
)

@Entity(
    tableName = "workout_sessions",
    foreignKeys = [ForeignKey(entity = RoutineEntity::class, parentColumns = ["id"], childColumns = ["routineId"], onDelete = ForeignKey.SET_NULL)],
    indices = [Index("routineId")]
)
data class WorkoutSessionEntity(
    @PrimaryKey val id: String,
    val startedAt: Long,
    val finishedAt: Long? = null,
    val status: String = "ACTIVE",
    val routineId: String? = null
)

@Entity(
    tableName = "workout_exercises",
    foreignKeys = [
        ForeignKey(entity = WorkoutSessionEntity::class, parentColumns = ["id"], childColumns = ["sessionId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = ExerciseEntity::class, parentColumns = ["id"], childColumns = ["exerciseId"])
    ],
    indices = [Index("sessionId"), Index("exerciseId")]
)
data class WorkoutExerciseEntity(
    @PrimaryKey val id: String,
    val sessionId: String,
    val exerciseId: String,
    val displayOrder: Int
)

@Entity(
    tableName = "set_entries",
    foreignKeys = [ForeignKey(entity = WorkoutExerciseEntity::class, parentColumns = ["id"], childColumns = ["workoutExerciseId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index("workoutExerciseId")]
)
data class SetEntryEntity(
    @PrimaryKey val id: String,
    val workoutExerciseId: String,
    val setOrder: Int,
    val weightGrams: Long? = null,
    val reps: Int? = null,
    val setType: String = "NORMAL",
    val completedAt: Long? = null
)
