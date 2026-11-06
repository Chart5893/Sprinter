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
    var exerciseIndex by remember { mutableIntStateOf(0) }
    var restSeconds by remember { mutableIntStateOf(90) }
    var deadline by remember { mutableLongStateOf(0L) }
    var remaining by remember { mutableIntStateOf(0) }
    var timerVisible by remember { mutableStateOf(true) }
    var lastPulse by remember { mutableIntStateOf(-1) }
    var finishConfirm by remember { mutableStateOf(false) }
    var abandonConfirm by remember { mutableStateOf(false) }

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
                break
            }
            delay(100)
        }
    }
    val ordered = workoutExercises.filter { it.sessionId == active?.id }.sortedBy { it.displayOrder }
    val current = ordered.getOrNull(exerciseIndex.coerceIn(0, (ordered.size - 1).coerceAtLeast(0)))
    val sets = allSets.filter { it.workoutExerciseId == current?.id }.sortedBy { it.setOrder }

    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Active workout", style = MaterialTheme.typography.headlineMedium)
        Text(routines.firstOrNull { it.id == active?.routineId }?.name ?: "Workout")
        if (active == null) {
            Text("No workout in progress.")
            Button(onClick = onExit) { Text("Back") }
        } else {
            if (deadline > 0L || !timerVisible) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Rest: ${remaining / 60}:${(remaining % 60).toString().padStart(2, '0')}")
                    TextButton(onClick = { timerVisible = !timerVisible }) {
                        Text(if (timerVisible) "Hide" else "Show")
                    }
                }
            }
            if (timerVisible) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = {
                        deadline = (deadline - 15000).coerceAtLeast(SystemClock.elapsedRealtime())
                    }, enabled = deadline > 0) { Text("-15s") }
                    OutlinedButton(onClick = { deadline = 0L; remaining = 0 }) { Text("Stop") }
                    OutlinedButton(onClick = {
                        deadline = (if (deadline > 0) deadline else SystemClock.elapsedRealtime()) + 15000
                    }) { Text("+15s") }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(60, 90, 120).forEach { seconds ->
                        FilterChip(selected = restSeconds == seconds, onClick = { restSeconds = seconds },
                            label = { Text("${seconds}s") })
                    }
                }
            }
            if (current != null) {
                Text(exercises.firstOrNull { it.id == current.exerciseId }?.name ?: "Exercise",
                    style = MaterialTheme.typography.titleLarge)
                Text("Exercise ${exerciseIndex + 1} of ${ordered.size}")
                LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(sets, key = { it.id }) { set ->
                        SetEditor(set, onSave = { weight, reps, complete ->
                            scope.launch {
                                repository.saveSet(set.copy(
                                    weightGrams = (weight * 453.59237).toLong(),
                                    reps = reps,
                                    completedAt = if (complete) System.currentTimeMillis() else null
                                ))
                                if (complete) {
                                    timerVisible = true
                                    lastPulse = -1
                                    deadline = SystemClock.elapsedRealtime() + restSeconds * 1000L
                                }
                            }
                        })
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { exerciseIndex-- }, enabled = exerciseIndex > 0) { Text("Previous") }
                    OutlinedButton(onClick = { exerciseIndex++ }, enabled = exerciseIndex < ordered.lastIndex) { Text("Next") }
                }
            } else {
                Spacer(Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { abandonConfirm = true }, modifier = Modifier.weight(1f)) { Text("Discard") }
                Button(onClick = { finishConfirm = true }, modifier = Modifier.weight(1f)) { Text("Finish workout") }
            }
        }
    }
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
private fun SetEditor(set: SetEntryEntity, onSave: (Double, Int, Boolean) -> Unit) {
    var weight by remember(set.id) { mutableStateOf(set.weightGrams?.let {
        String.format(Locale.US, "%.1f", it / 453.59237)
    } ?: "") }
    var reps by remember(set.id) { mutableStateOf(set.reps?.toString() ?: "") }
    val complete = set.completedAt != null
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Set ${set.setOrder + 1}" + if (complete) " • Completed" else "")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(weight, { weight = it }, label = { Text("Weight (lb)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true, modifier = Modifier.weight(1f))
                OutlinedTextField(reps, { reps = it }, label = { Text("Reps") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true, modifier = Modifier.weight(1f))
            }
            Button(onClick = {
                val w = weight.toDoubleOrNull() ?: return@Button
                val r = reps.toIntOrNull() ?: return@Button
                onSave(w, r, !complete)
            }, enabled = (weight.toDoubleOrNull() ?: -1.0) >= 0.0 &&
                (reps.toIntOrNull() ?: 0) > 0, modifier = Modifier.fillMaxWidth()) {
                Text(if (complete) "Update / Undo completion" else "Complete set")
            }
        }
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
