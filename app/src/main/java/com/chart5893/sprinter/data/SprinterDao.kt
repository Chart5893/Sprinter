package com.chart5893.sprinter.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface SprinterDao {
    @Query("SELECT * FROM measurement_entries ORDER BY date DESC")
    fun observeMeasurements(): Flow<List<MeasurementEntryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertMeasurement(entry: MeasurementEntryEntity)

    @Query("DELETE FROM measurement_entries WHERE id = :id")
    suspend fun deleteMeasurement(id: String)

    @Query("SELECT * FROM exercises ORDER BY name")
    fun observeExercises(): Flow<List<ExerciseEntity>>

    @Query("SELECT * FROM routines ORDER BY updatedAt DESC")
    fun observeRoutines(): Flow<List<RoutineEntity>>

    @Query("SELECT * FROM routine_exercises ORDER BY displayOrder")
    fun observeRoutineExercises(): Flow<List<RoutineExerciseEntity>>

    @Query("DELETE FROM routine_exercises WHERE routineId = :routineId")
    suspend fun clearRoutineExercises(routineId: String)

    @Query("DELETE FROM routines WHERE id = :routineId")
    suspend fun deleteRoutine(routineId: String)

    @Transaction
    suspend fun saveRoutineWithExercises(
        routine: RoutineEntity,
        entries: List<RoutineExerciseEntity>
    ) {
        upsertRoutine(routine)
        clearRoutineExercises(routine.id)
        entries.forEach { upsertRoutineExercise(it) }
    }

    @Query("SELECT * FROM workout_sessions ORDER BY startedAt DESC")
    fun observeSessions(): Flow<List<WorkoutSessionEntity>>

    @Query("SELECT * FROM workout_exercises ORDER BY displayOrder")
    fun observeWorkoutExercises(): Flow<List<WorkoutExerciseEntity>>

    @Query("SELECT * FROM set_entries ORDER BY setOrder")
    fun observeSetEntries(): Flow<List<SetEntryEntity>>

    @Query("SELECT * FROM workout_sessions WHERE status = 'ACTIVE' ORDER BY startedAt DESC LIMIT 1")
    suspend fun findActiveSession(): WorkoutSessionEntity?

    @Transaction
    suspend fun startWorkout(session: WorkoutSessionEntity, exercises: List<WorkoutExerciseEntity>,
                             sets: List<SetEntryEntity>) {
        upsertSession(session)
        exercises.forEach { upsertWorkoutExercise(it) }
        sets.forEach { upsertSetEntry(it) }
    }

    @Query("DELETE FROM workout_sessions WHERE status = 'ABANDONED' AND startedAt < :cutoff")
    suspend fun purgeOldAbandonedSessions(cutoff: Long): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertExercise(exercise: ExerciseEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertSeedExercises(exercises: List<ExerciseEntity>)

    @Query("""
        DELETE FROM exercises
        WHERE id = :id
          AND id NOT LIKE 'starter-%'
          AND NOT EXISTS (SELECT 1 FROM routine_exercises WHERE exerciseId = :id)
          AND NOT EXISTS (SELECT 1 FROM workout_exercises WHERE exerciseId = :id)
    """)
    suspend fun deleteUnusedCustomExercise(id: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertRoutine(routine: RoutineEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertRoutineExercise(routineExercise: RoutineExerciseEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSession(session: WorkoutSessionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertWorkoutExercise(workoutExercise: WorkoutExerciseEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSetEntry(set: SetEntryEntity)
}
