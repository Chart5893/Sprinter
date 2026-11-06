package com.chart5893.sprinter

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
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Workout history", style = MaterialTheme.typography.headlineMedium)
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
