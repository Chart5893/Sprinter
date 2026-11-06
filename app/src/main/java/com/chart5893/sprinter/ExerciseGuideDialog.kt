package com.chart5893.sprinter

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import com.chart5893.sprinter.data.ExerciseEntity

/** Offline exercise guidance. Images are displayed only when a reviewed local drawable exists. */
@Composable
internal fun ExerciseGuideDialog(exercise: ExerciseEntity, onDismiss: () -> Unit, onAddToRoutine: (() -> Unit)? = null) {
    val context = LocalContext.current
    val imageName = "exercise_" + exercise.name.lowercase()
        .replace(Regex("[^a-z0-9]+"), "_").trim('_')
    val drawableId = context.resources.getIdentifier(imageName, "drawable", context.packageName)
    val instructions = exerciseGuidance(exercise)
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(exercise.name) },
        text = {
            Column(
                Modifier.fillMaxWidth().heightIn(max = 510.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (drawableId != 0) {
                    Image(painterResource(drawableId), contentDescription = "Form demonstration for ${exercise.name}",
                        modifier = Modifier.fillMaxWidth().height(190.dp), contentScale = ContentScale.Fit)
                } else {
                    Text("Illustrated form guide coming soon", style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text(listOfNotNull(exercise.muscleGroup, exercise.equipment).joinToString(" • "),
                    style = MaterialTheme.typography.labelMedium)
                Text("How to perform", style = MaterialTheme.typography.titleMedium)
                instructions.forEachIndexed { index, step -> Text("${index + 1}. $step") }
                Text("Safety", style = MaterialTheme.typography.titleSmall)
                Text("Use a controlled range of motion and a manageable load. Stop if you feel sharp pain. Ask a qualified trainer to check your technique when learning a new movement.")
                if (!exercise.notes.isNullOrBlank()) {
                    Text("Your notes", style = MaterialTheme.typography.titleSmall)
                    Text(exercise.notes)
                }
            }
        },
        confirmButton = {
            if (onAddToRoutine != null) Button(onClick = onAddToRoutine) { Text("Add to routine") }
            else TextButton(onClick = onDismiss) { Text("Close") }
        },
        dismissButton = if (onAddToRoutine != null) ({ TextButton(onClick = onDismiss) { Text("Cancel") } }) else null
    )
}

/** Conservative general instructions; do not misrepresent generic cues as exercise-specific technique. */
private fun exerciseGuidance(exercise: ExerciseEntity): List<String> {
    val name = exercise.name.lowercase()
    return when {
        "squat" in name || "leg press" in name -> listOf(
            "Set your feet in a comfortable, stable position and brace your torso.",
            "Bend at the hips and knees through a controlled, pain-free range.",
            "Press through your feet to return, keeping your knees tracking with your toes."
        )
        "deadlift" in name || "romanian" in name -> listOf(
            "Start with a light load, brace your abdomen, and keep the weight close to your body.",
            "Hinge at your hips while maintaining a stable back position.",
            "Stand tall by extending the hips without leaning backward."
        )
        "press" in name || "push-up" in name -> listOf(
            "Set your shoulders and wrists in a stable, comfortable position.",
            "Lower the resistance under control without bouncing.",
            "Press back through a comfortable range without losing your torso position."
        )
        "row" in name || "pulldown" in name || "pull-up" in name -> listOf(
            "Start from a stable position with your torso braced.",
            "Pull using your back and arms without jerking or swinging.",
            "Return the resistance slowly and maintain control."
        )
        "curl" in name -> listOf(
            "Set up with a stable torso and keep the working joint aligned.",
            "Move the weight smoothly without using momentum.",
            "Lower the resistance slowly through a comfortable range."
        )
        "raise" in name || "fly" in name -> listOf(
            "Choose a light resistance and a comfortable joint position.",
            "Lift through a controlled range without swinging.",
            "Return slowly; avoid forcing the joint beyond its comfortable range."
        )
        "plank" in name -> listOf(
            "Position your hands or forearms securely beneath your shoulders.",
            "Brace your abdomen and keep your body aligned.",
            "Breathe normally and stop when you can no longer maintain position."
        )
        else -> listOf(
            "Check the equipment setup and choose a manageable resistance.",
            "Perform the movement slowly through a comfortable range without momentum.",
            "Maintain control throughout; seek exercise-specific instruction before attempting an unfamiliar movement."
        )
    }
}
