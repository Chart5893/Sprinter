package com.chart5893.sprinter.data

import kotlinx.coroutines.flow.Flow
import java.util.UUID

class SprinterRepository(private val dao: SprinterDao) {
    val exercises: Flow<List<ExerciseEntity>> = dao.observeExercises()
    val routines: Flow<List<RoutineEntity>> = dao.observeRoutines()
    val routineExercises: Flow<List<RoutineExerciseEntity>> = dao.observeRoutineExercises()
    val sessions: Flow<List<WorkoutSessionEntity>> = dao.observeSessions()


    val workoutExercises: Flow<List<WorkoutExerciseEntity>> = dao.observeWorkoutExercises()
    val setEntries: Flow<List<SetEntryEntity>> = dao.observeSetEntries()

    suspend fun startWorkout(routineId: String, entries: List<RoutineExerciseEntity>): String {
        dao.findActiveSession()?.let { return it.id }
        val id = UUID.randomUUID().toString()
        val sorted = entries.sortedBy { it.displayOrder }
        val workoutExercises = sorted.mapIndexed { index, item ->
            WorkoutExerciseEntity(UUID.randomUUID().toString(), id, item.exerciseId, index)
        }
        val sets = workoutExercises.flatMapIndexed { index, exercise ->
            (0 until (sorted[index].targetSets ?: 3).coerceIn(1, 99)).map { setIndex ->
                SetEntryEntity(UUID.randomUUID().toString(), exercise.id, setIndex)
            }
        }
        dao.startWorkout(WorkoutSessionEntity(id, System.currentTimeMillis(), routineId = routineId), workoutExercises, sets)
        return id
    }

    suspend fun saveSet(set: SetEntryEntity) = dao.upsertSetEntry(set)
    suspend fun finishWorkout(session: WorkoutSessionEntity) =
        dao.upsertSession(session.copy(status = "COMPLETED", finishedAt = System.currentTimeMillis()))
    suspend fun abandonWorkout(session: WorkoutSessionEntity) =
        dao.upsertSession(session.copy(status = "ABANDONED", finishedAt = System.currentTimeMillis()))
    suspend fun cleanupTemporarySessions(): Int =
        dao.purgeOldAbandonedSessions(System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000)

    suspend fun saveExercise(exercise: ExerciseEntity) = dao.upsertExercise(exercise)
    suspend fun saveRoutine(routine: RoutineEntity) = dao.upsertRoutine(routine)
    suspend fun saveRoutineWithExercises(routine: RoutineEntity, entries: List<RoutineExerciseEntity>) =
        dao.saveRoutineWithExercises(routine, entries)
    suspend fun deleteRoutine(id: String) = dao.deleteRoutine(id)
    suspend fun deleteCustomExercise(id: String): Boolean = dao.deleteUnusedCustomExercise(id) > 0

    suspend fun seedExercises() {
        val starters = listOf(
            Triple("Bench Press", "Chest", "Barbell"),
            Triple("Incline Dumbbell Press", "Chest", "Dumbbell"),
            Triple("Chest Fly", "Chest", "Machine"),
            Triple("Push-Up", "Chest", "Bodyweight"),
            Triple("Deadlift", "Back", "Barbell"),
            Triple("Lat Pulldown", "Back", "Cable"),
            Triple("Incline Row", "Back", "Dumbbell"),
            Triple("Seated Cable Row", "Back", "Cable"),
            Triple("Overhead Press", "Shoulders", "Barbell"),
            Triple("Lateral Raise", "Shoulders", "Dumbbell"),
            Triple("Biceps Curl", "Arms", "Dumbbell"),
            Triple("Triceps Pushdown", "Arms", "Cable"),
            Triple("Squat", "Legs", "Barbell"),
            Triple("Leg Press", "Legs", "Machine"),
            Triple("Romanian Deadlift", "Legs", "Barbell"),
            Triple("Leg Curl", "Legs", "Machine"),
            Triple("Calf Raise", "Legs", "Machine"),
            Triple("Plank", "Core", "Bodyweight"),
            Triple("Crunch", "Core", "Bodyweight"),
            Triple("Treadmill Run", "Cardio", "Machine")
        )
        dao.insertSeedExercises(
            starters.mapIndexed { index, (name, muscle, equipment) ->
                ExerciseEntity(
                    id = "starter-${index + 1}",
                    name = name,
                    muscleGroup = muscle,
                    equipment = equipment
                )
            }
        )
    }
}
