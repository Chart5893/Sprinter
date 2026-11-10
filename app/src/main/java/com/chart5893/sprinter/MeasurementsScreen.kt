package com.chart5893.sprinter

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.chart5893.sprinter.data.MeasurementEntryEntity
import com.chart5893.sprinter.data.SprinterRepository
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeParseException
import java.util.UUID

private data class MeasurementType(val key: String, val label: String, val unit: String)
private val types = listOf(
    MeasurementType("weight", "Body weight", "lb"),
    MeasurementType("calories", "Calorie intake", "kcal"),
    MeasurementType("waist", "Waist", "in"),
    MeasurementType("chest", "Chest", "in"),
    MeasurementType("arms", "Arms", "in"),
    MeasurementType("hips", "Hips", "in"),
    MeasurementType("bodyfat", "Body fat", "%")
)

@Composable
fun MeasurementsScreen(repository: SprinterRepository) {
    val entries by repository.measurements.collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()
    var selected by remember { mutableStateOf<MeasurementType?>(null) }
    var editing by remember { mutableStateOf<MeasurementEntryEntity?>(null) }
    var showEditor by remember { mutableStateOf(false) }
    var deleteTarget by remember { mutableStateOf<MeasurementEntryEntity?>(null) }

    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(if (selected == null) "Measurements" else selected!!.label, style = MaterialTheme.typography.headlineMedium)
        if (selected == null) {
            Text("Select a measurement to view the last six months or add a new entry.")
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(types) { type ->
                    val latest = entries.filter { it.type == type.key }.maxWithOrNull(compareBy<MeasurementEntryEntity> { it.date }.thenBy { it.id })
                    Card(Modifier.fillMaxWidth().clickable { selected = type }) {
                        Column(Modifier.padding(16.dp)) {
                            Text(type.label, style = MaterialTheme.typography.titleMedium)
                            Text(if (latest == null) "No entries yet" else "${latest.value} ${type.unit} • ${latest.date}")
                        }
                    }
                }
            }
        } else {
            val type = selected!!
            val cutoff = LocalDate.now().minusMonths(6).toString()
            val filtered = entries.filter { it.type == type.key && it.date >= cutoff && it.date <= LocalDate.now().toString() }.sortedWith(compareBy({ it.date }, { it.id }))
            TextButton(onClick = { selected = null }) { Text("← All measurements") }
            Button(onClick = { editing = null; showEditor = true }, modifier = Modifier.fillMaxWidth()) { Text("Add ${type.label.lowercase()} entry") }
            Text("Last 6 months", style = MaterialTheme.typography.titleMedium)
            if (filtered.isEmpty()) Text("No measurements recorded in the last six months.")
            else MeasurementLineGraph(filtered, type.unit)
            Text("Entries", style = MaterialTheme.typography.titleMedium)
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(entries.filter { it.type == type.key }.sortedByDescending { it.date }, key = { it.id }) { entry ->
                    Card(Modifier.fillMaxWidth()) {
                        Row(Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("${entry.value} ${type.unit}")
                                Text(entry.date, style = MaterialTheme.typography.bodySmall)
                            }
                            Row {
                                TextButton(onClick = { editing = entry; showEditor = true }) { Text("Edit") }
                                TextButton(onClick = { deleteTarget = entry }) { Text("Delete") }
                            }
                        }
                    }
                }
            }
        }
    }
    if (showEditor && selected != null) {
        val type = selected!!
        var value by remember(editing, showEditor) { mutableStateOf(editing?.value?.toString() ?: "") }
        var date by remember(editing, showEditor) { mutableStateOf(editing?.date ?: LocalDate.now().toString()) }
        val parsedDate = try { LocalDate.parse(date) } catch (_: DateTimeParseException) { null }
        val number = value.toDoubleOrNull()
        AlertDialog(
            onDismissRequest = { showEditor = false },
            title = { Text(if (editing == null) "Add ${type.label}" else "Edit ${type.label}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = value, onValueChange = { value = it }, label = { Text("Value (${type.unit})") }, singleLine = true)
                    OutlinedTextField(value = date, onValueChange = { date = it }, label = { Text("Date (YYYY-MM-DD)") }, singleLine = true)
                    if (parsedDate == null) Text("Enter a valid date, for example 2026-11-09.")
                }
            },
            confirmButton = {
                TextButton(enabled = number != null && number.isFinite() && number >= 0 && parsedDate != null && parsedDate <= LocalDate.now(), onClick = {
                    val entry = MeasurementEntryEntity(editing?.id ?: UUID.randomUUID().toString(), type.key, number!!, date)
                    scope.launch { repository.saveMeasurement(entry) }
                    showEditor = false
                }) { Text("Save") }
            },
            dismissButton = { TextButton(onClick = { showEditor = false }) { Text("Cancel") } }
        )
    }
    deleteTarget?.let { target ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text("Delete measurement?") },
            text = { Text("This entry will be permanently deleted.") },
            confirmButton = { TextButton(onClick = { scope.launch { repository.deleteMeasurement(target.id) }; deleteTarget = null }) { Text("Delete") } },
            dismissButton = { TextButton(onClick = { deleteTarget = null }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun MeasurementLineGraph(entries: List<MeasurementEntryEntity>, unit: String) {
    val points = entries.map { LocalDate.parse(it.date).toEpochDay().toFloat() to it.value.toFloat() }
    val minX = points.minOf { it.first }
    val maxX = points.maxOf { it.first }
    val minY = points.minOf { it.second }
    val maxY = points.maxOf { it.second }
    val color = MaterialTheme.colorScheme.primary
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text("Range: ${minY}–${maxY} $unit", style = MaterialTheme.typography.bodySmall)
        Canvas(Modifier.fillMaxWidth().height(180.dp)) {
            val margin = 14.dp.toPx()
            val w = (size.width - margin * 2).coerceAtLeast(1f)
            val h = (size.height - margin * 2).coerceAtLeast(1f)
            fun position(p: Pair<Float, Float>): Offset {
                val x = if (maxX == minX) 0.5f else (p.first - minX) / (maxX - minX)
                val y = if (maxY == minY) 0.5f else (p.second - minY) / (maxY - minY)
                return Offset(margin + x * w, margin + (1f - y) * h)
            }
            val path = Path()
            points.forEachIndexed { index, point ->
                val pos = position(point)
                if (index == 0) path.moveTo(pos.x, pos.y) else path.lineTo(pos.x, pos.y)
            }
            drawPath(path, color, style = Stroke(width = 3.dp.toPx()))
            points.forEach { drawCircle(color, radius = 4.dp.toPx(), center = position(it)) }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(entries.first().date, style = MaterialTheme.typography.labelSmall)
            Text(entries.last().date, style = MaterialTheme.typography.labelSmall)
        }
    }
}
