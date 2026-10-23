package com.example.nepalcalendar.ui.events

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nepalcalendar.calendar.engine.NepaliDate

@Composable
fun AddEditEventDialog(
    initialDate: NepaliDate,
    useNepaliLanguage: Boolean,
    useNepaliNumerals: Boolean,
    onDismiss: () -> Unit,
    onSave: (title: String, description: String, category: String, colorHex: String, isAllDay: Boolean) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Personal") }
    var selectedColorHex by remember { mutableStateOf("#B3192B") }
    var titleError by remember { mutableStateOf(false) }

    val categories = listOf("Personal", "Birthday", "Anniversary", "Meeting", "Deadline")
    val colors = listOf("#B3192B", "#0F3D69", "#2E7D32", "#7B1FA2", "#E65100", "#00838F")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (useNepaliLanguage) "नयाँ कार्यक्रम थप्नुहोस्" else "Add New Event",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Display targeted date
                Text(
                    text = "${if (useNepaliLanguage) "मिति: " else "Date: "} ${initialDate.formatDisplay(useNepaliLanguage, useNepaliNumerals)}",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )

                // Title input
                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        if (it.isNotBlank()) titleError = false
                    },
                    label = { Text(if (useNepaliLanguage) "कार्यक्रमको शीर्षक *" else "Event Title *") },
                    isError = titleError,
                    supportingText = if (titleError) {
                        { Text(if (useNepaliLanguage) "शीर्षक आवश्यक छ" else "Title is required") }
                    } else null,
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("event_title_input")
                )

                // Description input
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text(if (useNepaliLanguage) "विवरण (वैकल्पिक)" else "Description (optional)") },
                    maxLines = 3,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("event_desc_input")
                )

                // Category selection
                Text(
                    text = if (useNepaliLanguage) "वर्ग (Category)" else "Category",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    categories.take(3).forEach { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat, fontSize = 12.sp) }
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    categories.drop(3).forEach { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat, fontSize = 12.sp) }
                        )
                    }
                }

                // Color picker row
                Text(
                    text = if (useNepaliLanguage) "रङ (Color)" else "Color Tag",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    colors.forEach { hex ->
                        val color = Color(android.graphics.Color.parseColor(hex))
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(color)
                                .then(
                                    if (selectedColorHex == hex) {
                                        Modifier.border(2.5.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                                    } else Modifier
                                )
                                .clickable { selectedColorHex = hex }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isBlank()) {
                        titleError = true
                    } else {
                        onSave(title, description, selectedCategory, selectedColorHex, true)
                        onDismiss()
                    }
                },
                modifier = Modifier.testTag("save_event_confirm_button")
            ) {
                Text(if (useNepaliLanguage) "सुरक्षित गर्नुहोस्" else "Save")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("cancel_event_button")
            ) {
                Text(if (useNepaliLanguage) "रद्द" else "Cancel")
            }
        }
    )
}
