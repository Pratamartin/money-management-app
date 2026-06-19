package com.pratatec.moneymgtapp.wear.presentation.pin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import com.pratatec.moneymgtapp.wear.data.local.WearPinSession

@Composable
fun PinScreen(
    uiState: PinUiState,
    onDigit: (String) -> Unit,
    onDelete: () -> Unit,
    onUnlocked: () -> Unit,
) {
    LaunchedEffect(WearPinSession.unlocked) {
        if (WearPinSession.unlocked) onUnlocked()
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(top = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top,
    ) {
        Text(
            text = "Money Flow",
            style = MaterialTheme.typography.title3,
        )

        Spacer(Modifier.height(8.dp))

        PinDots(filled = uiState.digits.length, error = uiState.error)

        Spacer(Modifier.height(4.dp))

        when {
            uiState.locked -> Text(
                text = "App bloqueado.\nRedefina o PIN no celular.",
                style = MaterialTheme.typography.caption2,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colors.error,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
            uiState.error -> Text(
                text = "PIN incorreto. ${uiState.attemptsLeft} tentativa(s) restante(s).",
                style = MaterialTheme.typography.caption2,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colors.error,
                modifier = Modifier.padding(horizontal = 12.dp),
            )
            else -> Text(
                text = "Digite seu PIN",
                style = MaterialTheme.typography.caption2,
                color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f),
            )
        }

        Spacer(Modifier.height(8.dp))

        if (!uiState.locked) {
            Numpad(onDigit = onDigit, onDelete = onDelete)
        }
    }
}

@Composable
private fun PinDots(filled: Int, error: Boolean) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        repeat(4) { index ->
            val active = index < filled
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            error && active -> MaterialTheme.colors.error
                            active -> MaterialTheme.colors.primary
                            else -> MaterialTheme.colors.onSurface.copy(alpha = 0.3f)
                        }
                    )
            )
        }
    }
}

@Composable
private fun Numpad(onDigit: (String) -> Unit, onDelete: () -> Unit) {
    val rows = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf("⌫", "0", ""),
    )

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        rows.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                row.forEach { label ->
                    when (label) {
                        "" -> Spacer(Modifier.size(44.dp))
                        "⌫" -> Button(
                            onClick = onDelete,
                            modifier = Modifier.size(44.dp),
                            colors = ButtonDefaults.secondaryButtonColors(),
                        ) {
                            Text(label, style = MaterialTheme.typography.caption1)
                        }
                        else -> Button(
                            onClick = { onDigit(label) },
                            modifier = Modifier.size(44.dp),
                            colors = ButtonDefaults.secondaryButtonColors(),
                        ) {
                            Text(label, style = MaterialTheme.typography.title3)
                        }
                    }
                }
            }
        }
    }
}
