package com.chart5893.sprinter.data

import kotlinx.coroutines.flow.Flow

class SprinterRepository(private val dao: SprinterDao) {
    val exercises: Flow<List<ExerciseEntity>> = dao.observeExercises()
    val routines: Flow<List<RoutineEntity>> = dao.observeRoutines()
    val sessions: Flow<List<WorkoutSessionEntity>> = dao.observeSessions()

    suspend fun saveExercise(exercise: ExerciseEntity) = dao.upsertExercise(exercise)
    suspend fun saveRoutine(routine: RoutineEntity) = dao.upsertRoutine(routine)
}
