package com.pratatec.moneymgtapp.wear.presentation.addgasto

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.CircularProgressIndicator
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Picker
import androidx.wear.compose.material.ScalingLazyColumn
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.rememberPickerState

@Composable
fun AddGastoScreen(
    uiState: AddGastoUiState,
    events: kotlinx.coroutines.flow.Flow<AddGastoEvent>,
    onSelectCategoria: (Int) -> Unit,
    onSelectValor: (Int) -> Unit,
    onSave: () -> Unit,
    onBack: () -> Unit,
) {
    LaunchedEffect(Unit) {
        events.collect { event ->
            if (event is AddGastoEvent.Success) onBack()
        }
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        when {
            uiState.isLoading -> CircularProgressIndicator()
            uiState.isSaving -> CircularProgressIndicator()
            else -> AddGastoContent(uiState, onSelectCategoria, onSelectValor, onSave)
        }
    }
}

@Composable
private fun AddGastoContent(
    uiState: AddGastoUiState,
    onSelectCategoria: (Int) -> Unit,
    onSelectValor: (Int) -> Unit,
    onSave: () -> Unit,
) {
    val valorOptions = (1..40).map { it * 5 }
    val valorPickerState = rememberPickerState(
        initialNumberOfOptions = valorOptions.size,
        initiallySelectedOption = uiState.valorIndex,
    )

    LaunchedEffect(valorPickerState.selectedOption) {
        onSelectValor(valorPickerState.selectedOption)
    }

    ScalingLazyColumn(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        item {
            Text("Valor", style = MaterialTheme.typography.caption2)
        }
        item {
            Picker(
                state = valorPickerState,
                modifier = Modifier.fillMaxWidth(0.6f),
                contentDescription = "Valor do gasto",
            ) { index ->
                Text(
                    text = "R$ ${valorOptions[index]}",
                    style = MaterialTheme.typography.title3,
                )
            }
        }
        item {
            Text(
                text = "Categoria",
                style = MaterialTheme.typography.caption2,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
        uiState.categorias.forEachIndexed { index, categoria ->
            item {
                Chip(
                    label = { Text(categoria.nome) },
                    onClick = { onSelectCategoria(index) },
                    colors = if (index == uiState.selectedCategoriaIndex)
                        ChipDefaults.primaryChipColors()
                    else
                        ChipDefaults.secondaryChipColors(),
                    modifier = Modifier.fillMaxWidth(0.85f),
                )
            }
        }
        if (uiState.error != null) {
            item {
                Text(
                    text = uiState.error,
                    color = MaterialTheme.colors.error,
                    style = MaterialTheme.typography.caption2,
                    textAlign = TextAlign.Center,
                )
            }
        }
        item {
            Button(
                onClick = onSave,
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .padding(top = 4.dp),
                colors = ButtonDefaults.primaryButtonColors(),
            ) {
                Text("Salvar")
            }
        }
    }
}
