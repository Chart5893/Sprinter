package com.chart5893.sprinter

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.chart5893.sprinter.data.*
import kotlinx.coroutines.launch
import java.util.UUID

private data class RoutineDraftExercise(
    val exerciseId: String,
    val sets: String = "3",
    val reps: String = "10"
)

@Composable
internal fun RoutineScreen(repository: SprinterRepository) {
    val routines by repository.routines.collectAsState(initial = emptyList())
    val entries by repository.routineExercises.collectAsState(initial = emptyList())
    val exercises by repository.exercises.collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()
    var editing by remember { mutableStateOf<RoutineEntity?>(null) }
    var creating by remember { mutableStateOf(false) }
    var deleting by remember { mutableStateOf<RoutineEntity?>(null) }

    LaunchedEffect(repository) { repository.seedExercises() }

    if (creating || editing != null) {
        val routine = editing
        // Keying by routine ID resets the draft when switching between routines.
        key(routine?.id ?: "new") {
            RoutineEditor(
                existing = routine,
                initialEntries = entries.filter { it.routineId == routine?.id }
                    .sortedBy { it.displayOrder },
                exercises = exercises,
                onCancel = { editing = null; creating = false },
                onSave = { name, draft ->
                    val now = System.currentTimeMillis()
                    val id = routine?.id ?: UUID.randomUUID().toString()
                    val saved = RoutineEntity(
                        id = id,
                        name = name.trim(),
                        createdAt = routine?.createdAt ?: now,
                        updatedAt = now
                    )
                    val selected = draft.mapIndexed { index, item ->
                        RoutineExerciseEntity(
                            routineId = id,
                            exerciseId = item.exerciseId,
                            displayOrder = index,
                            targetSets = item.sets.toIntOrNull(),
                            targetReps = item.reps.toIntOrNull()
                        )
                    }
                    scope.launch {
                        repository.saveRoutineWithExercises(saved, selected)
                        editing = null
                        creating = false
                    }
                }
            )
        }
    } else {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Your routines", style = MaterialTheme.typography.headlineMedium)
            Button(onClick = { creating = true }, modifier = Modifier.fillMaxWidth()) {
                Text("Create routine")
            }
            if (routines.isEmpty()) {
                Text("No routines yet. Create one for each workout day.")
            }
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(routines, key = { it.id }) { routine ->
                    val selected = entries.filter { it.routineId == routine.id }
                        .sortedBy { it.displayOrder }
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(routine.name, style = MaterialTheme.typography.titleLarge)
                            Text("${selected.size} exercises")
                            selected.forEach { item ->
                                val name = exercises.firstOrNull { it.id == item.exerciseId }?.name
                                    ?: "Unknown exercise"
                                Text("• $name — ${item.targetSets ?: 0} × ${item.targetReps ?: 0}")
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(onClick = { editing = routine }) { Text("Edit") }
                                TextButton(onClick = { deleting = routine }) { Text("Delete") }
                            }
                        }
                    }
                }
            }
        }
    }

    deleting?.let { routine ->
        AlertDialog(
            onDismissRequest = { deleting = null },
            title = { Text("Delete routine?") },
            text = { Text("Delete ${routine.name}? Completed workout history will be kept.") },
            confirmButton = {
                TextButton(onClick = {
                    deleting = null
                    scope.launch { repository.deleteRoutine(routine.id) }
                }) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { deleting = null }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun RoutineEditor(
    existing: RoutineEntity?,
    initialEntries: List<RoutineExerciseEntity>,
    exercises: List<ExerciseEntity>,
    onCancel: () -> Unit,
    onSave: (String, List<RoutineDraftExercise>) -> Unit
) {
    var name by remember { mutableStateOf(existing?.name ?: "") }
    val selected = remember {
        mutableStateListOf<RoutineDraftExercise>().apply {
            addAll(initialEntries.map {
                RoutineDraftExercise(
                    exerciseId = it.exerciseId,
                    sets = it.targetSets?.toString() ?: "3",
                    reps = it.targetReps?.toString() ?: "10"
                )
            })
        }
    }
    var choosing by remember { mutableStateOf(false) }
    val canSave = name.isNotBlank() && selected.isNotEmpty() &&
        selected.all {
            (it.sets.toIntOrNull() ?: 0) in 1..99 &&
                (it.reps.toIntOrNull() ?: 0) in 1..999
        }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            if (existing == null) "Create routine" else "Edit routine",
            style = MaterialTheme.typography.headlineMedium
        )
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Routine name") },
            placeholder = { Text("e.g. Push Day") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Button(onClick = { choosing = true }, modifier = Modifier.fillMaxWidth()) {
            Text("Add exercise")
        }
        Text("Exercises in this routine: ${selected.size}")
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(selected.size) { index ->
                val item = selected[index]
                val exercise = exercises.firstOrNull { it.id == item.exerciseId }
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(exercise?.name ?: "Unknown exercise",
                            style = MaterialTheme.typography.titleMedium)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = item.sets,
                                onValueChange = { selected[index] = item.copy(sets = it) },
                                label = { Text("Sets") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = item.reps,
                                onValueChange = { selected[index] = item.copy(reps = it) },
                                label = { Text("Reps") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            TextButton(
                                onClick = {
                                    if (index > 0) {
                                        val previous = selected[index - 1]
                                        selected[index - 1] = item
                                        selected[index] = previous
                                    }
                                },
                                enabled = index > 0
                            ) { Text("Up") }
                            TextButton(
                                onClick = {
                                    if (index < selected.lastIndex) {
                                        val next = selected[index + 1]
                                        selected[index + 1] = item
                                        selected[index] = next
                                    }
                                },
                                enabled = index < selected.lastIndex
                            ) { Text("Down") }
                            TextButton(onClick = { selected.removeAt(index) }) { Text("Remove") }
                        }
                    }
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = onCancel, modifier = Modifier.weight(1f)) {
                Text("Cancel")
            }
            Button(
                onClick = { onSave(name, selected.toList()) },
                enabled = canSave,
                modifier = Modifier.weight(1f)
            ) { Text("Save routine") }
        }
    }

    if (choosing) {
        var query by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { choosing = false },
            title = { Text("Choose an exercise") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        label = { Text("Search") },
                        singleLine = true
                    )
                    LazyColumn(modifier = Modifier.heightIn(max = 360.dp)) {
                        items(exercises.filter {
                            it.name.contains(query, ignoreCase = true) &&
                                selected.none { draft -> draft.exerciseId == it.id }
                        }, key = { it.id }) { exercise ->
                            Text(
                                exercise.name,
                                modifier = Modifier.fillMaxWidth()
                                    .clickable {
                                        selected.add(RoutineDraftExercise(exercise.id))
                                        choosing = false
                                    }
                                    .padding(vertical = 12.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { choosing = false }) { Text("Done") }
            }
        )
    }
}
