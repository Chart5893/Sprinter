package com.chart5893.sprinter

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.ui.Alignment
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.*
import androidx.room.Room
import com.chart5893.sprinter.data.*
import kotlinx.coroutines.launch
import java.util.UUID

private enum class Screen(val route: String, val title: String) {
    Exercises("exercises", "Exercises"),
    Routines("routines", "Routines"),
    History("history", "History")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val database = Room.databaseBuilder(
            applicationContext, SprinterDatabase::class.java, "sprinter.db"
        ).build()
        val repository = SprinterRepository(database.sprinterDao())
        setContent {
            MaterialTheme {
                SprinterApp(repository)
            }
        }
    }
}

@Composable
private fun SprinterApp(repository: SprinterRepository) {
    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    Scaffold(
        bottomBar = {
            NavigationBar {
                Screen.entries.forEach { screen ->
                    NavigationBarItem(
                        selected = backStack?.destination?.route == screen.route,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Text("•") },
                        label = { Text(screen.title) }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Exercises.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(Screen.Exercises.route) { ExerciseLibrary(repository) }
            composable(Screen.Routines.route) { RoutineScreen(repository) }
            composable(Screen.History.route) {
                PlaceholderScreen("Workout history", "Completed sessions will appear here.")
            }
        }
    }
}

private val muscleGroups = listOf(
    "All", "Chest", "Back", "Shoulders", "Arms", "Legs", "Core", "Cardio", "Other"
)
private val equipmentTypes = listOf(
    "None", "Barbell", "Dumbbell", "Machine", "Cable", "Bodyweight", "Other"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExerciseLibrary(repository: SprinterRepository) {
    val exercises by repository.exercises.collectAsState(initial = emptyList())
    val routines by repository.routines.collectAsState(initial = emptyList())
    val routineEntries by repository.routineExercises.collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()
    var search by remember { mutableStateOf("") }
    var group by remember { mutableStateOf("All") }
    var showAdd by remember { mutableStateOf(false) }
    var pendingDelete by remember { mutableStateOf<ExerciseEntity?>(null) }
    var addingToRoutine by remember { mutableStateOf<ExerciseEntity?>(null) }
    var newRoutineName by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(repository) { repository.seedExercises() }

    val visible = remember(exercises, search, group) {
        exercises.filter {
            (group == "All" || it.muscleGroup == group) &&
                it.name.contains(search.trim(), ignoreCase = true)
        }
    }
    Box(modifier = Modifier.fillMaxSize()) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Exercises", style = MaterialTheme.typography.headlineMedium)
        OutlinedTextField(
            value = search,
            onValueChange = { search = it },
            label = { Text("Search exercises") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        var groupExpanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = groupExpanded,
            onExpandedChange = { groupExpanded = it }
        ) {
            OutlinedTextField(
                value = group,
                onValueChange = {},
                readOnly = true,
                label = { Text("Muscle group") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = groupExpanded) },
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = groupExpanded,
                onDismissRequest = { groupExpanded = false }
            ) {
                muscleGroups.forEach { item ->
                    DropdownMenuItem(
                        text = { Text(item) },
                        onClick = { group = item; groupExpanded = false }
                    )
                }
            }
        }
        Button(onClick = { showAdd = true }, modifier = Modifier.fillMaxWidth()) {
            Text("Add custom exercise")
        }
        Text("${visible.size} exercises", style = MaterialTheme.typography.labelMedium)
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(visible, key = { it.id }) { exercise ->
                val isCustom = !exercise.id.startsWith("starter-")
                if (isCustom) {
                    val dismissState = rememberSwipeToDismissBoxState(
                        confirmValueChange = { true }
                    )
                    SwipeToDismissBox(
                        state = dismissState,
                        enableDismissFromStartToEnd = false,
                        backgroundContent = {
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Button(
                                    onClick = { pendingDelete = exercise },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.error
                                    )
                                ) { Text("Delete") }
                            }
                        }
                    ) {
                        ExerciseCard(exercise, onClick = { addingToRoutine = exercise })
                    }
                } else {
                    ExerciseCard(exercise, onClick = { addingToRoutine = exercise })
                }
            }
        }
    }
    SnackbarHost(hostState = snackbarHostState, modifier = Modifier.align(Alignment.BottomCenter))
    }
    addingToRoutine?.let { exercise ->
        AlertDialog(
            onDismissRequest = { addingToRoutine = null; newRoutineName = "" },
            title = { Text("Add ${exercise.name} to a routine") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (routines.isEmpty()) Text("No routines yet. Create one below.")
                    routines.forEach { routine ->
                        val included = routineEntries.any {
                            it.routineId == routine.id && it.exerciseId == exercise.id
                        }
                        OutlinedButton(
                            onClick = {
                                scope.launch {
                                    val current = routineEntries.filter { it.routineId == routine.id }
                                        .sortedBy { it.displayOrder }
                                    if (!included) {
                                        repository.saveRoutineWithExercises(
                                            routine.copy(updatedAt = System.currentTimeMillis()),
                                            current + RoutineExerciseEntity(
                                                routineId = routine.id,
                                                exerciseId = exercise.id,
                                                displayOrder = current.size,
                                                targetSets = 3,
                                                targetReps = 10
                                            )
                                        )
                                    }
                                    addingToRoutine = null
                                }
                            },
                            enabled = !included,
                            modifier = Modifier.fillMaxWidth()
                        ) { Text(if (included) "${routine.name} (already added)" else routine.name) }
                    }
                    HorizontalDivider()
                    OutlinedTextField(
                        value = newRoutineName,
                        onValueChange = { newRoutineName = it },
                        label = { Text("New routine name") },
                        singleLine = true
                    )
                    Button(
                        enabled = newRoutineName.isNotBlank(),
                        onClick = {
                            val name = newRoutineName.trim()
                            scope.launch {
                                val now = System.currentTimeMillis()
                                val id = UUID.randomUUID().toString()
                                repository.saveRoutineWithExercises(
                                    RoutineEntity(id = id, name = name, createdAt = now, updatedAt = now),
                                    listOf(RoutineExerciseEntity(
                                        routineId = id, exerciseId = exercise.id,
                                        displayOrder = 0, targetSets = 3, targetReps = 10
                                    ))
                                )
                                addingToRoutine = null
                                newRoutineName = ""
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Create routine with exercise") }
                }
            },
            confirmButton = {
                TextButton(onClick = { addingToRoutine = null; newRoutineName = "" }) {
                    Text("Close")
                }
            }
        )
    }
    pendingDelete?.let { exercise ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("Delete custom exercise?") },
            text = { Text("Remove ${exercise.name} from your library? This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    pendingDelete = null
                    scope.launch {
                        if (!repository.deleteCustomExercise(exercise.id)) {
                            snackbarHostState.showSnackbar("Cannot delete an exercise used by a routine or workout.")
                        }
                    }
                }) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { pendingDelete = null }) { Text("Cancel") } }
        )
    }
    if (showAdd) {
        AddExerciseDialog(
            onDismiss = { showAdd = false },
            onSave = { name, muscle, equipment, notes ->
                scope.launch {
                    repository.saveExercise(
                        ExerciseEntity(
                            id = UUID.randomUUID().toString(),
                            name = name.trim(),
                            muscleGroup = muscle,
                            equipment = equipment,
                            notes = notes.trim().ifBlank { null }
                        )
                    )
                }
                group = "All"
                search = ""
                showAdd = false
            }
        )
    }
}

@Composable
private fun ExerciseCard(exercise: ExerciseEntity, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Column(Modifier.padding(16.dp)) {
            Text(exercise.name, style = MaterialTheme.typography.titleMedium)
            Text(
                listOfNotNull(exercise.muscleGroup, exercise.equipment).joinToString(" • "),
                style = MaterialTheme.typography.bodyMedium
            )
            Text(if (exercise.id.startsWith("starter-")) "Built-in" else "Custom", style = MaterialTheme.typography.labelSmall)
            if (!exercise.notes.isNullOrBlank()) {
                Text(exercise.notes, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun AddExerciseDialog(
    onDismiss: () -> Unit,
    onSave: (String, String, String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var muscle by remember { mutableStateOf("Chest") }
    var equipment by remember { mutableStateOf("None") }
    var notes by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New exercise") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Exercise name") },
                    singleLine = true
                )
                SimpleDropdown("Muscle group", muscle, muscleGroups.drop(1)) { muscle = it }
                SimpleDropdown("Equipment", equipment, equipmentTypes) { equipment = it }
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (optional)") }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(name, muscle, equipment, notes) },
                enabled = name.isNotBlank()
            ) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SimpleDropdown(
    label: String,
    value: String,
    options: List<String>,
    onSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = { onSelected(option); expanded = false }
                )
            }
        }
    }
}

@Composable
private fun PlaceholderScreen(title: String, description: String) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Sprinter", style = MaterialTheme.typography.headlineLarge)
        Text(title, style = MaterialTheme.typography.titleLarge)
        Text(description)
    }
}
