package com.chart5893.sprinter

import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.content.Context
import android.os.SystemClock
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.chart5893.sprinter.data.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
internal fun WorkoutScreen(repository: SprinterRepository, onExit: () -> Unit) {
    val sessions by repository.sessions.collectAsState(initial = emptyList())
    val workoutExercises by repository.workoutExercises.collectAsState(initial = emptyList())
    val allSets by repository.setEntries.collectAsState(initial = emptyList())
    val exercises by repository.exercises.collectAsState(initial = emptyList())
    val routines by repository.routines.collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val active = sessions.firstOrNull { it.status == "ACTIVE" }
    var restSeconds by remember { mutableIntStateOf(90) }
    var deadline by remember { mutableLongStateOf(0L) }
    var remaining by remember { mutableIntStateOf(0) }
    var timerVisible by remember { mutableStateOf(true) }
    var nowMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var activeRestSetId by remember { mutableStateOf<String?>(null) }
    val restDurations = remember { mutableStateMapOf<String, Int>() }
    var lastPulse by remember { mutableIntStateOf(-1) }
    var finishConfirm by remember { mutableStateOf(false) }
    var abandonConfirm by remember { mutableStateOf(false) }
    var guideExercise by remember { mutableStateOf<ExerciseEntity?>(null) }

    LaunchedEffect(active?.id) {
        while (true) { nowMillis = System.currentTimeMillis(); delay(1000) }
    }
    LaunchedEffect(deadline) {
        while (deadline > 0L) {
            val millis = deadline - SystemClock.elapsedRealtime()
            val seconds = ((millis + 999) / 1000).toInt().coerceAtLeast(0)
            remaining = seconds
            if (seconds in 1..3 && seconds != lastPulse) {
                lastPulse = seconds
                vibratePulse(context)
            }
            if (millis <= 0L) {
                playCompletionTone(context)
                deadline = 0L
                activeRestSetId = null
                break
            }
            delay(100)
        }
    }
    val ordered = workoutExercises.filter { it.sessionId == active?.id }.sortedBy { it.displayOrder }

    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(routines.firstOrNull { it.id == active?.routineId }?.name ?: "Workout",
                style = MaterialTheme.typography.titleLarge)
            val elapsed = ((nowMillis - (active?.startedAt ?: nowMillis)) / 1000).coerceAtLeast(0)
            Text("%02d:%02d:%02d".format(elapsed / 3600, (elapsed / 60) % 60, elapsed % 60),
                style = MaterialTheme.typography.titleMedium)
        }
        if (active == null) {
            Text("No workout in progress.")
            Button(onClick = onExit) { Text("Back") }
        } else {
            if (deadline > 0L) {
                TextButton(onClick = { timerVisible = !timerVisible }) {
                    Text(if (timerVisible) "Hide rest controls" else "Show rest controls")
                }
                if (timerVisible) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = {
                            deadline = (deadline - 15000).coerceAtLeast(SystemClock.elapsedRealtime())
                        }) { Text("-15s") }
                        OutlinedButton(onClick = { deadline = 0L; activeRestSetId = null; remaining = 0 }) { Text("Stop") }
                        OutlinedButton(onClick = { deadline += 15000 }) { Text("+15s") }
                    }
                }
            }
            LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                items(ordered, key = { it.id }) { workoutExercise ->
                    val exercise = exercises.firstOrNull { it.id == workoutExercise.exerciseId }
                    val exerciseSets = allSets.filter { it.workoutExerciseId == workoutExercise.id }
                        .sortedBy { it.setOrder }
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(exercise?.name ?: "Exercise",
                            modifier = Modifier.clickable { guideExercise = exercise },
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.primary)
                        Text(listOfNotNull(exercise?.muscleGroup, exercise?.equipment).joinToString(" • "),
                            style = MaterialTheme.typography.bodySmall)
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("SET", modifier = Modifier.width(38.dp), style = MaterialTheme.typography.labelSmall)
                            Text("LB", modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelSmall)
                            Text("REPS", modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelSmall)
                            Text("DONE", style = MaterialTheme.typography.labelSmall)
                        }
                        exerciseSets.forEach { set ->
                            val duration = restDurations[set.id] ?: restSeconds
                            SetEditor(set, onSave = { weight, reps, action ->
                                scope.launch {
                                    repository.saveSet(set.copy(
                                        weightGrams = weight?.let { (it * 453.59237).toLong() },
                                        reps = reps,
                                        completedAt = when (action) {
                                            "COMPLETE" -> set.completedAt ?: System.currentTimeMillis()
                                            "UNDO" -> null
                                            else -> set.completedAt
                                        }
                                    ))
                                    if (action == "COMPLETE" && set.completedAt == null) {
                                        timerVisible = true
                                        activeRestSetId = set.id
                                        lastPulse = -1
                                        deadline = SystemClock.elapsedRealtime() + duration * 1000L
                                    }
                                }
                            })
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center,
                                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                                HorizontalDivider(modifier = Modifier.weight(1f))
                                val isRunning = activeRestSetId == set.id && deadline > 0L
                                TextButton(onClick = {
                                    restDurations[set.id] = when (duration) {
                                        60 -> 90
                                        90 -> 120
                                        120 -> 180
                                        else -> 60
                                    }
                                }) {
                                    Text(if (isRunning)
                                        "%d:%02d".format(remaining / 60, remaining % 60)
                                        else "%d:%02d".format(duration / 60, duration % 60))
                                }
                                HorizontalDivider(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                    HorizontalDivider()
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { abandonConfirm = true }, modifier = Modifier.weight(1f)) { Text("Discard") }
                Button(onClick = { finishConfirm = true }, modifier = Modifier.weight(1f)) { Text("Finish workout") }
            }
        }
    }
    guideExercise?.let { ExerciseGuideDialog(exercise = it, onDismiss = { guideExercise = null }) }
    if (finishConfirm && active != null) {
        AlertDialog(onDismissRequest = { finishConfirm = false }, title = { Text("Finish workout?") },
            text = { Text("Save all recorded sets to workout history?") },
            confirmButton = { TextButton(onClick = {
                finishConfirm = false
                deadline = 0L
                scope.launch { repository.finishWorkout(active); onExit() }
            }) { Text("Save workout") } },
            dismissButton = { TextButton(onClick = { finishConfirm = false }) { Text("Cancel") } })
    }
    if (abandonConfirm && active != null) {
        AlertDialog(onDismissRequest = { abandonConfirm = false }, title = { Text("Discard workout?") },
            text = { Text("This will mark the session abandoned, not delete completed history.") },
            confirmButton = { TextButton(onClick = {
                abandonConfirm = false
                deadline = 0L
                scope.launch { repository.abandonWorkout(active); onExit() }
            }) { Text("Discard") } },
            dismissButton = { TextButton(onClick = { abandonConfirm = false }) { Text("Cancel") } })
    }
}

@Composable
private fun SetEditor(set: SetEntryEntity, onSave: (Double?, Int, String) -> Unit) {
    var weight by remember(set.id) { mutableStateOf(set.weightGrams?.let {
        String.format(Locale.US, "%.1f", it / 453.59237)
    } ?: "") }
    var reps by remember(set.id) { mutableStateOf(set.reps?.toString() ?: "") }
    val complete = set.completedAt != null
    val valid = (weight.isBlank() || (weight.toDoubleOrNull() ?: -1.0) >= 0.0) &&
        (reps.toIntOrNull() ?: 0) > 0
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Text("${set.setOrder + 1}", modifier = Modifier.width(38.dp).padding(top = 14.dp))
        OutlinedTextField(
            value = weight,
            onValueChange = { weight = it },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.weight(1f)
        )
        OutlinedTextField(
            value = reps,
            onValueChange = { reps = it },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.weight(1f)
        )
        Button(
            onClick = {
                if (valid) onSave(weight.toDoubleOrNull(), reps.toInt(), if (complete) "UNDO" else "COMPLETE")
            },
            enabled = valid,
            contentPadding = PaddingValues(horizontal = 10.dp),
            modifier = Modifier.padding(top = 4.dp)
        ) { Text(if (complete) "✓" else "○") }
    }
    if (complete) {
        TextButton(onClick = {
            if (valid) onSave(weight.toDoubleOrNull(), reps.toInt(), "SAVE")
        }, enabled = valid) { Text("Save edited values") }
    } else {
        TextButton(onClick = {
            if (valid) onSave(weight.toDoubleOrNull(), reps.toInt(), "SAVE")
        }, enabled = valid) { Text("Save changes") }
    }
}

private fun vibratePulse(context: Context) {
    val vibrator = if (Build.VERSION.SDK_INT >= 31)
        context.getSystemService(VibratorManager::class.java)?.defaultVibrator
    else context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    vibrator?.vibrate(VibrationEffect.createOneShot(70, VibrationEffect.DEFAULT_AMPLITUDE))
}

private fun playCompletionTone(context: Context) {
    val audio = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    val attributes = AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_NOTIFICATION_EVENT)
        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build()
    val request = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
        .setAudioAttributes(attributes).setOnAudioFocusChangeListener { }.build()
    val granted = audio.requestAudioFocus(request) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
    val tone = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 90)
    tone.startTone(ToneGenerator.TONE_PROP_BEEP2, 450)
    android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
        tone.release()
        if (granted) audio.abandonAudioFocusRequest(request)
    }, 550)
}
