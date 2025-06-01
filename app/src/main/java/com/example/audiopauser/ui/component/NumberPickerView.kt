package com.example.audiopauser.ui.component

import android.widget.NumberPicker
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun NumberPickerView(
    value: Int,
    range: IntRange,
    onValueChange: (Int) -> Unit
) {
    AndroidView(
        factory = { context ->
            NumberPicker(context).apply {
                minValue = range.first
                maxValue = range.last
                wrapSelectorWheel = true
                this.value = value
                setOnValueChangedListener { _, _, newVal -> onValueChange(newVal) }
            }
        },
        update = { picker ->
            if (picker.value != value) {
                picker.value = value
            }
        },
        modifier = Modifier.size(width = 80.dp, height = 120.dp)
    )
}