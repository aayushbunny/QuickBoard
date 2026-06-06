@file:OptIn(ExperimentalMaterial3Api::class)

package com.aayush.smartticket

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue





@OptIn(ExperimentalMaterial3Api::class)

@Composable
fun ConcessionSection(
    enabled: Boolean,
    onEnabledChange: (Boolean) -> Unit,
    selected: ConcessionType,
    onSelect: (ConcessionType) -> Unit
) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = enabled,
                onCheckedChange = onEnabledChange
            )
            Spacer(Modifier.width(8.dp))
            Text("Avail concession")
        }

        if (enabled) {
            Spacer(Modifier.height(12.dp))
            ConcessionDropdown(selected, onSelect)
        }
    }
}

@Composable
fun ConcessionDropdown(
    selected: ConcessionType,
    onSelect: (ConcessionType) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selected.label,
            onValueChange = {},
            readOnly = true,
            label = { Text("Select concession type") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            ConcessionType.values()
                .filter { it != ConcessionType.NONE }
                .forEach {
                    DropdownMenuItem(
                        text = { Text(it.label) },
                        onClick = {
                            onSelect(it)
                            expanded = false
                        }
                    )
                }
        }
    }
}
