package com.app.pixelcraft.presentation.components.dialogs

import com.app.pixelcraft.R
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PixelCraftTextEditDialog(
    initialText: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var text by remember(initialText) { mutableStateOf(initialText) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1E222B),
        title = {
            Text(
                text = stringResource(R.string.ui_edit_text),
                fontWeight = FontWeight.Bold,
                color = Color(0xFF00E5FF),
                fontSize = 18.sp
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 6,
                    placeholder = { Text(stringResource(R.string.ui_enter_text), color = Color.Gray) }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(text.ifBlank { " " }) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)),
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(stringResource(R.string.ui_ok), color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel), color = Color.White)
            }
        }
    )
}
