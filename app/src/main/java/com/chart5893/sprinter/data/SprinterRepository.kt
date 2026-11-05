package com.chart5893.sprinter.data

import kotlinx.coroutines.flow.Flow

class SprinterRepository(private val dao: SprinterDao) {
    val exercises: Flow<List<ExerciseEntity>> = dao.observeExercises()
    val routines: Flow<List<RoutineEntity>> = dao.observeRoutines()
    val sessions: Flow<List<WorkoutSessionEntity>> = dao.observeSessions()

    suspend fun saveExercise(exercise: ExerciseEntity) = dao.upsertExercise(exercise)
    suspend fun saveRoutine(routine: RoutineEntity) = dao.upsertRoutine(routine)
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
