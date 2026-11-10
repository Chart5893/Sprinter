package com.chart5893.sprinter.data

import kotlinx.coroutines.flow.Flow
import java.util.UUID

class SprinterRepository(private val dao: SprinterDao) {
    val measurements: Flow<List<MeasurementEntryEntity>> = dao.observeMeasurements()
    suspend fun saveMeasurement(entry: MeasurementEntryEntity) = dao.upsertMeasurement(entry)
    suspend fun deleteMeasurement(id: String) = dao.deleteMeasurement(id)

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
                SetEntryEntity(UUID.randomUUID().toString(), exercise.id, setIndex,
                    weightGrams = sorted[index].targetWeightGrams,
                    reps = sorted[index].targetReps)
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
            Triple("Treadmill Run", "Cardio", "Machine"),
            Triple("Incline Bench Press", "Chest", "Barbell"),
            Triple("Decline Bench Press", "Chest", "Barbell"),
            Triple("Close-Grip Bench Press", "Chest", "Barbell"),
            Triple("Barbell Floor Press", "Chest", "Barbell"),
            Triple("Flat Dumbbell Press", "Chest", "Dumbbell"),
            Triple("Decline Dumbbell Press", "Chest", "Dumbbell"),
            Triple("Dumbbell Fly", "Chest", "Dumbbell"),
            Triple("Incline Dumbbell Fly", "Chest", "Dumbbell"),
            Triple("Dumbbell Pullover", "Chest", "Dumbbell"),
            Triple("Dumbbell Floor Press", "Chest", "Dumbbell"),
            Triple("Cable Chest Fly", "Chest", "Cable"),
            Triple("Low-to-High Cable Fly", "Chest", "Cable"),
            Triple("High-to-Low Cable Fly", "Chest", "Cable"),
            Triple("Cable Chest Press", "Chest", "Cable"),
            Triple("Chest Press Machine", "Chest", "Machine"),
            Triple("Incline Chest Press Machine", "Chest", "Machine"),
            Triple("Pec Deck Fly", "Chest", "Machine"),
            Triple("Smith Machine Bench Press", "Chest", "Machine"),
            Triple("Smith Machine Incline Press", "Chest", "Machine"),
            Triple("Incline Push-Up", "Chest", "Bodyweight"),
            Triple("Decline Push-Up", "Chest", "Bodyweight"),
            Triple("Diamond Push-Up", "Chest", "Bodyweight"),
            Triple("Chest Dip", "Chest", "Bodyweight"),
            Triple("Bent-Over Barbell Row", "Back", "Barbell"),
            Triple("Pendlay Row", "Back", "Barbell"),
            Triple("T-Bar Row", "Back", "Barbell"),
            Triple("Rack Pull", "Back", "Barbell"),
            Triple("Barbell Shrug", "Back", "Barbell"),
            Triple("One-Arm Dumbbell Row", "Back", "Dumbbell"),
            Triple("Chest-Supported Dumbbell Row", "Back", "Dumbbell"),
            Triple("Dumbbell Shrug", "Back", "Dumbbell"),
            Triple("Renegade Row", "Back", "Dumbbell"),
            Triple("Reverse Fly", "Back", "Dumbbell"),
            Triple("Close-Grip Lat Pulldown", "Back", "Cable"),
            Triple("Wide-Grip Lat Pulldown", "Back", "Cable"),
            Triple("Straight-Arm Pulldown", "Back", "Cable"),
            Triple("Single-Arm Cable Row", "Back", "Cable"),
            Triple("Face Pull", "Back", "Cable"),
            Triple("Cable Pullover", "Back", "Cable"),
            Triple("Chest-Supported Row Machine", "Back", "Machine"),
            Triple("High Row Machine", "Back", "Machine"),
            Triple("Assisted Pull-Up", "Back", "Machine"),
            Triple("Reverse Pec Deck", "Back", "Machine"),
            Triple("Machine Pullover", "Back", "Machine"),
            Triple("Pull-Up", "Back", "Bodyweight"),
            Triple("Chin-Up", "Back", "Bodyweight"),
            Triple("Inverted Row", "Back", "Bodyweight"),
            Triple("Back Extension", "Back", "Bodyweight"),
            Triple("Superman Hold", "Back", "Bodyweight"),
            Triple("Push Press", "Shoulders", "Barbell"),
            Triple("Behind-the-Neck Press", "Shoulders", "Barbell"),
            Triple("Upright Row", "Shoulders", "Barbell"),
            Triple("Seated Dumbbell Shoulder Press", "Shoulders", "Dumbbell"),
            Triple("Arnold Press", "Shoulders", "Dumbbell"),
            Triple("Front Raise", "Shoulders", "Dumbbell"),
            Triple("Bent-Over Rear Delt Raise", "Shoulders", "Dumbbell"),
            Triple("Dumbbell Upright Row", "Shoulders", "Dumbbell"),
            Triple("Cable Lateral Raise", "Shoulders", "Cable"),
            Triple("Cable Front Raise", "Shoulders", "Cable"),
            Triple("Cable Rear Delt Fly", "Shoulders", "Cable"),
            Triple("Cable Upright Row", "Shoulders", "Cable"),
            Triple("Shoulder Press Machine", "Shoulders", "Machine"),
            Triple("Lateral Raise Machine", "Shoulders", "Machine"),
            Triple("Rear Delt Machine", "Shoulders", "Machine"),
            Triple("Pike Push-Up", "Shoulders", "Bodyweight"),
            Triple("Handstand Push-Up", "Shoulders", "Bodyweight"),
            Triple("Wall Handstand Hold", "Shoulders", "Bodyweight"),
            Triple("Barbell Curl", "Arms", "Barbell"),
            Triple("EZ-Bar Curl", "Arms", "Barbell"),
            Triple("Preacher Curl (EZ-Bar)", "Arms", "Barbell"),
            Triple("Skull Crusher", "Arms", "Barbell"),
            Triple("Close-Grip EZ-Bar Press", "Arms", "Barbell"),
            Triple("Hammer Curl", "Arms", "Dumbbell"),
            Triple("Incline Dumbbell Curl", "Arms", "Dumbbell"),
            Triple("Concentration Curl", "Arms", "Dumbbell"),
            Triple("Alternating Dumbbell Curl", "Arms", "Dumbbell"),
            Triple("Overhead Dumbbell Triceps Extension", "Arms", "Dumbbell"),
            Triple("Triceps Kickback", "Arms", "Dumbbell"),
            Triple("Zottman Curl", "Arms", "Dumbbell"),
            Triple("Rope Hammer Curl", "Arms", "Cable"),
            Triple("Cable Biceps Curl", "Arms", "Cable"),
            Triple("Overhead Cable Triceps Extension", "Arms", "Cable"),
            Triple("Single-Arm Triceps Pushdown", "Arms", "Cable"),
            Triple("Rope Triceps Pushdown", "Arms", "Cable"),
            Triple("Preacher Curl Machine", "Arms", "Machine"),
            Triple("Triceps Extension Machine", "Arms", "Machine"),
            Triple("Assisted Dip Machine", "Arms", "Machine"),
            Triple("Bench Dip", "Arms", "Bodyweight"),
            Triple("Bodyweight Triceps Dip", "Arms", "Bodyweight"),
            Triple("Front Squat", "Legs", "Barbell"),
            Triple("Sumo Deadlift", "Legs", "Barbell"),
            Triple("Hip Thrust", "Legs", "Barbell"),
            Triple("Good Morning", "Legs", "Barbell"),
            Triple("Barbell Lunge", "Legs", "Barbell"),
            Triple("Bulgarian Split Squat (Barbell)", "Legs", "Barbell"),
            Triple("Goblet Squat", "Legs", "Dumbbell"),
            Triple("Dumbbell Walking Lunge", "Legs", "Dumbbell"),
            Triple("Dumbbell Step-Up", "Legs", "Dumbbell"),
            Triple("Dumbbell Romanian Deadlift", "Legs", "Dumbbell"),
            Triple("Bulgarian Split Squat (Dumbbell)", "Legs", "Dumbbell"),
            Triple("Standing Calf Raise (Dumbbell)", "Legs", "Dumbbell"),
            Triple("Leg Extension", "Legs", "Machine"),
            Triple("Seated Leg Curl", "Legs", "Machine"),
            Triple("Lying Leg Curl", "Legs", "Machine"),
            Triple("Hack Squat", "Legs", "Machine"),
            Triple("Smith Machine Squat", "Legs", "Machine"),
            Triple("Hip Abduction Machine", "Legs", "Machine"),
            Triple("Hip Adduction Machine", "Legs", "Machine"),
            Triple("Seated Calf Raise", "Legs", "Machine"),
            Triple("Standing Calf Raise Machine", "Legs", "Machine"),
            Triple("Glute Kickback Machine", "Legs", "Machine"),
            Triple("Horizontal Leg Press", "Legs", "Machine"),
            Triple("Cable Pull-Through", "Legs", "Cable"),
            Triple("Cable Glute Kickback", "Legs", "Cable"),
            Triple("Cable Hip Abduction", "Legs", "Cable"),
            Triple("Bodyweight Squat", "Legs", "Bodyweight"),
            Triple("Walking Lunge", "Legs", "Bodyweight"),
            Triple("Reverse Lunge", "Legs", "Bodyweight"),
            Triple("Step-Up", "Legs", "Bodyweight"),
            Triple("Single-Leg Glute Bridge", "Legs", "Bodyweight"),
            Triple("Glute Bridge", "Legs", "Bodyweight"),
            Triple("Wall Sit", "Legs", "Bodyweight"),
            Triple("Single-Leg Calf Raise", "Legs", "Bodyweight"),
            Triple("Reverse Crunch", "Core", "Bodyweight"),
            Triple("Bicycle Crunch", "Core", "Bodyweight"),
            Triple("Russian Twist", "Core", "Bodyweight"),
            Triple("Hanging Knee Raise", "Core", "Bodyweight"),
            Triple("Hanging Leg Raise", "Core", "Bodyweight"),
            Triple("Side Plank", "Core", "Bodyweight"),
            Triple("Dead Bug", "Core", "Bodyweight"),
            Triple("Bird Dog", "Core", "Bodyweight"),
            Triple("Mountain Climber", "Core", "Bodyweight"),
            Triple("Hollow Body Hold", "Core", "Bodyweight"),
            Triple("Flutter Kick", "Core", "Bodyweight"),
            Triple("V-Up", "Core", "Bodyweight"),
            Triple("Ab Wheel Rollout", "Core", "Bodyweight"),
            Triple("Cable Crunch", "Core", "Cable"),
            Triple("Pallof Press", "Core", "Cable"),
            Triple("Cable Woodchop", "Core", "Cable"),
            Triple("Kneeling Cable Crunch", "Core", "Cable"),
            Triple("Abdominal Crunch Machine", "Core", "Machine"),
            Triple("Rotary Torso Machine", "Core", "Machine"),
            Triple("Weighted Sit-Up", "Core", "Dumbbell"),
            Triple("Dumbbell Side Bend", "Core", "Dumbbell"),
            Triple("Treadmill Walk", "Cardio", "Machine"),
            Triple("Incline Treadmill Walk", "Cardio", "Machine"),
            Triple("Stationary Bike", "Cardio", "Machine"),
            Triple("Recumbent Bike", "Cardio", "Machine"),
            Triple("Elliptical", "Cardio", "Machine"),
            Triple("Rowing Machine", "Cardio", "Machine"),
            Triple("Stair Climber", "Cardio", "Machine"),
            Triple("Ski Erg", "Cardio", "Machine"),
            Triple("Air Bike", "Cardio", "Machine"),
            Triple("Outdoor Run", "Cardio", "Bodyweight"),
            Triple("Outdoor Walk", "Cardio", "Bodyweight"),
            Triple("Jumping Jacks", "Cardio", "Bodyweight"),
            Triple("High Knees", "Cardio", "Bodyweight"),
            Triple("Burpee", "Cardio", "Bodyweight"),
            Triple("Jump Rope", "Cardio", "Bodyweight"),
            Triple("Farmer Carry", "Other", "Dumbbell"),
            Triple("Dumbbell Thruster", "Other", "Dumbbell"),
            Triple("Dumbbell Snatch", "Other", "Dumbbell"),
            Triple("Dumbbell Clean", "Other", "Dumbbell"),
            Triple("Power Clean", "Other", "Barbell"),
            Triple("Hang Clean", "Other", "Barbell"),
            Triple("Clean and Press", "Other", "Barbell"),
            Triple("Barbell Thruster", "Other", "Barbell"),
            Triple("Box Jump", "Other", "Bodyweight"),
            Triple("Bear Crawl", "Other", "Bodyweight"),
            Triple("Broad Jump", "Other", "Bodyweight"),
            Triple("Sled Push", "Other", "Bodyweight"),
            Triple("Sled Pull", "Other", "Bodyweight"),
            Triple("Battle Ropes", "Other", "Bodyweight"),
            Triple("Kettlebell Swing", "Other", "Bodyweight"),
            Triple("Kettlebell Goblet Squat", "Other", "Bodyweight"),
            Triple("Turkish Get-Up", "Other", "Bodyweight"),
            Triple("Medicine Ball Slam", "Other", "Bodyweight")
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
