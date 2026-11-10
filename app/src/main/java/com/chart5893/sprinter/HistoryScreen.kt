package com.chart5893.sprinter

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import java.io.OutputStreamWriter
import java.text.SimpleDateFormat
import java.util.Locale
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.chart5893.sprinter.data.*
import java.text.DateFormat
import java.util.Date

@Composable
internal fun HistoryScreen(repository: SprinterRepository) {
    val sessions by repository.sessions.collectAsState(initial = emptyList())
    val workoutExercises by repository.workoutExercises.collectAsState(initial = emptyList())
    val sets by repository.setEntries.collectAsState(initial = emptyList())
    val routines by repository.routines.collectAsState(initial = emptyList())
    val exercises by repository.exercises.collectAsState(initial = emptyList())
    val completed = sessions.filter { it.status == "COMPLETED" }
    val context = LocalContext.current
    var pendingCsv by remember { mutableStateOf("") }
    val saveCsv = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.openOutputStream(uri)?.use { stream ->
                    OutputStreamWriter(stream, Charsets.UTF_8).use { writer -> writer.write(pendingCsv) }
                } ?: error("Could not open the selected file")
                Toast.makeText(context, "Workout history exported", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Export failed: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Workout history", style = MaterialTheme.typography.headlineMedium)
        Button(
            onClick = {
                pendingCsv = buildWorkoutCsv(completed, workoutExercises, sets, routines, exercises)
                saveCsv.launch("sprinter_workouts_${java.time.LocalDate.now()}.csv")
            },
            enabled = completed.isNotEmpty(),
            modifier = Modifier.fillMaxWidth()
        ) { Text("Export CSV") }
        if (completed.isEmpty()) Text("No completed workouts yet.")
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(completed, key = { it.id }) { session ->
                val included = workoutExercises.filter { it.sessionId == session.id }.sortedBy { it.displayOrder }
                var expanded by remember(session.id) { mutableStateOf(false) }
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(routines.firstOrNull { it.id == session.routineId }?.name ?: "Saved workout",
                            style = MaterialTheme.typography.titleMedium)
                        Text(DateFormat.getDateTimeInstance().format(Date(session.startedAt)))
                        val completedSets = sets.count { set ->
                            included.any { it.id == set.workoutExerciseId } && set.completedAt != null
                        }
                        Text("${included.size} exercises • ${completedSets} completed sets")
                        TextButton(onClick = { expanded = !expanded }) { Text(if (expanded) "Hide details" else "View details") }
                        if (expanded) included.forEach { item ->
                            Text(exercises.firstOrNull { it.id == item.exerciseId }?.name ?: "Exercise",
                                style = MaterialTheme.typography.titleSmall)
                            sets.filter { it.workoutExerciseId == item.id && it.completedAt != null }
                                .sortedBy { it.setOrder }.forEach { set ->
                                    Text("Set ${set.setOrder + 1}: " +
                                        "${String.format("%.1f", (set.weightGrams ?: 0L) / 453.59237)} lb × ${set.reps ?: 0}")
                                }
                        }
                    }
                }
            }
        }
    }
}

private fun csvCell(value: String): String {
    // Quote every cell and escape embedded quotes. Prefix spreadsheet formulas to avoid CSV injection.
    val safe = if (value.firstOrNull() in listOf('=', '+', '-', '@', '\t', '\r')) "'$value" else value
    return "\"" + safe.replace("\"", "\"\"") + "\""
}

private fun buildWorkoutCsv(
    sessions: List<WorkoutSessionEntity>,
    workoutExercises: List<WorkoutExerciseEntity>,
    sets: List<SetEntryEntity>,
    routines: List<RoutineEntity>,
    exercises: List<ExerciseEntity>
): String {
    val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
    val rows = mutableListOf(
        listOf("workout_id", "routine", "started_at", "finished_at", "exercise", "set_number", "reps", "weight_lb", "completed_at")
    )
    sessions.sortedBy { it.startedAt }.forEach { session ->
        val routineName = routines.firstOrNull { it.id == session.routineId }?.name ?: "Saved workout"
        val included = workoutExercises.filter { it.sessionId == session.id }.sortedBy { it.displayOrder }
        var wroteSet = false
        included.forEach { item ->
            val exerciseName = exercises.firstOrNull { it.id == item.exerciseId }?.name ?: "Unknown exercise"
            sets.filter { it.workoutExerciseId == item.id && it.completedAt != null }
                .sortedBy { it.setOrder }.forEach { set ->
                    rows.add(listOf(
                        session.id, routineName, format.format(Date(session.startedAt)),
                        session.finishedAt?.let { format.format(Date(it)) } ?: "",
                        exerciseName, (set.setOrder + 1).toString(), set.reps?.toString() ?: "",
                        set.weightGrams?.let { String.format(Locale.US, "%.2f", it / 453.59237) } ?: "",
                        set.completedAt?.let { format.format(Date(it)) } ?: ""
                    ))
                    wroteSet = true
                }
        }
        if (!wroteSet) {
            rows.add(listOf(session.id, routineName, format.format(Date(session.startedAt)),
                session.finishedAt?.let { format.format(Date(it)) } ?: "", "", "", "", "", ""))
        }
    }
    return rows.joinToString("\r\n", postfix = "\r\n") { row -> row.joinToString(",") { csvCell(it) } }
}
