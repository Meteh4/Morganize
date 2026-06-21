package com.metoly.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerState
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.util.Calendar

/**
 * A dialog that sequences a DatePicker and then a TimePicker to select a future Reminder timestamp.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderDateTimePickerDialog(
    initialReminderAt: Long?,
    onDismissRequest: () -> Unit,
    onReminderSet: (Long?) -> Unit
) {
    var step by remember { mutableStateOf(if (initialReminderAt != null) Step.DATE else Step.DATE) }
    
    val initialDateMillis = initialReminderAt ?: System.currentTimeMillis()
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialDateMillis)

    val calendar = Calendar.getInstance()
    if (initialReminderAt != null) {
        calendar.timeInMillis = initialReminderAt
    }
    val timePickerState = rememberTimePickerState(
        initialHour = calendar.get(Calendar.HOUR_OF_DAY),
        initialMinute = calendar.get(Calendar.MINUTE),
        is24Hour = true
    )

    when (step) {
        Step.DATE -> {
            DatePickerDialog(
                onDismissRequest = onDismissRequest,
                confirmButton = {
                    TextButton(onClick = { step = Step.TIME }) {
                        Text("Next")
                    }
                },
                dismissButton = {
                    if (initialReminderAt != null) {
                        TextButton(onClick = {
                            onReminderSet(null) // clear reminder
                        }) {
                            Text("Clear Reminder")
                        }
                    }
                    TextButton(onClick = onDismissRequest) {
                        Text("Cancel")
                    }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }
        Step.TIME -> {
            // Material 3 TimePickerDialog is not standard, so we build a simple AlertDialog like wrapper
            androidx.compose.material3.AlertDialog(
                onDismissRequest = onDismissRequest,
                confirmButton = {
                    TextButton(onClick = {
                        val selectedDateMillis = datePickerState.selectedDateMillis
                        if (selectedDateMillis != null) {
                            val cal = Calendar.getInstance()
                            cal.timeInMillis = selectedDateMillis
                            cal.set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                            cal.set(Calendar.MINUTE, timePickerState.minute)
                            cal.set(Calendar.SECOND, 0)
                            onReminderSet(cal.timeInMillis)
                        } else {
                            onDismissRequest()
                        }
                    }) {
                        Text("Set Reminder")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { step = Step.DATE }) {
                        Text("Back")
                    }
                },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        TimePicker(state = timePickerState)
                    }
                }
            )
        }
    }
}

private enum class Step {
    DATE, TIME
}
